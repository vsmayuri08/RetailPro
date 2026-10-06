package com.retailsystem.service;

import com.retailsystem.dto.CreateSaleReturnRequest;
import com.retailsystem.dto.CustomerDTO;
import com.retailsystem.dto.LoyaltyAdjustmentRequest;
import com.retailsystem.dto.SaleDTO;
import com.retailsystem.dto.SaleItemDTO;
import com.retailsystem.dto.SaleReturnDTO;
import com.retailsystem.dto.SaleReturnItemRequest;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import com.retailsystem.entity.SaleReturn;
import com.retailsystem.entity.SaleReturnItem;
import com.retailsystem.entity.User;
import com.retailsystem.exception.InvalidSaleException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.SaleRepository;
import com.retailsystem.repository.SaleReturnItemRepository;
import com.retailsystem.repository.SaleReturnRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sales returns — a cashier-facing workflow that undoes (all or part of)
 * a completed Sale in one {@code @Transactional} method: refund amount,
 * FIFO-safe restock via InventoryBatchService, and loyalty reversal via
 * CustomerService#adjustLoyaltyPoints. Original Sale rows are never
 * rewritten so the invoice remains an accurate record of what was sold.
 */
@Service
public class SaleReturnService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleReturnRepository saleReturnRepository;

    @Autowired
    private SaleReturnItemRepository saleReturnItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Autowired
    private CustomerService customerService;

    @Transactional
    public SaleReturnDTO processReturn(Long branchId, Long cashierUserId, Long saleId,
                                       CreateSaleReturnRequest request) {
        Sale sale = loadOwnedSale(saleId, branchId);
        User processedBy = userRepository.findById(cashierUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", cashierUserId));

        Map<Long, Integer> alreadyReturned = returnedQuantityBySaleItem(saleId);
        Map<Long, SaleItem> itemsById = sale.getItems().stream()
                .collect(Collectors.toMap(SaleItem::getId, item -> item));

        Map<Long, Integer> returningNow = new HashMap<>();
        BigDecimal goodsReturned = BigDecimal.ZERO;

        for (SaleReturnItemRequest line : request.getItems()) {
            SaleItem original = itemsById.get(line.getSaleItemId());
            if (original == null) {
                throw new InvalidSaleException("Sale line " + line.getSaleItemId() + " is not on this invoice");
            }
            int already = alreadyReturned.getOrDefault(original.getId(), 0);
            int remaining = original.getQuantity() - already;
            if (line.getQuantity() > remaining) {
                throw new InvalidSaleException(original.getProductNameSnapshot()
                        + " can only return " + remaining + " more (sold " + original.getQuantity() + ")");
            }
            returningNow.merge(original.getId(), line.getQuantity(), Integer::sum);
            goodsReturned = goodsReturned.add(original.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }

        for (Map.Entry<Long, Integer> entry : returningNow.entrySet()) {
            SaleItem original = itemsById.get(entry.getKey());
            int remaining = original.getQuantity() - alreadyReturned.getOrDefault(original.getId(), 0);
            if (entry.getValue() > remaining) {
                throw new InvalidSaleException("Duplicate return lines exceed remaining quantity for "
                        + original.getProductNameSnapshot());
            }
        }

        List<SaleReturn> previous = saleReturnRepository.findBySale_Id(saleId);
        BigDecimal alreadyRefunded = previous.stream()
                .map(SaleReturn::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int alreadyReversedPoints = previous.stream().mapToInt(SaleReturn::getLoyaltyPointsReversed).sum();

        boolean closesTheSale = isClosingReturn(sale, alreadyReturned, returningNow);
        BigDecimal refundAmount = computeRefund(sale, goodsReturned, alreadyRefunded, closesTheSale);
        int pointsToReverse = computePointsToReverse(sale, goodsReturned, alreadyReversedPoints, closesTheSale);

        SaleReturn saleReturn = new SaleReturn(sale, processedBy, request.getReason().trim(), refundAmount);

        for (Map.Entry<Long, Integer> entry : returningNow.entrySet()) {
            SaleItem original = itemsById.get(entry.getKey());
            int qty = entry.getValue();
            SaleReturnItem returnItem = new SaleReturnItem(saleReturn, original, qty);
            saleReturn.getItems().add(returnItem);

            Product product = original.getProduct();
            inventoryBatchService.restockReturned(product, qty, product.getExpiryDate());
        }

        int actuallyReversed = 0;
        if (sale.getCustomer() != null && pointsToReverse > 0) {
            CustomerDTO customer = customerService.getCustomer(sale.getCustomer().getId());
            actuallyReversed = Math.min(pointsToReverse, customer.getLoyaltyPoints());
            if (actuallyReversed > 0) {
                LoyaltyAdjustmentRequest loyaltyRequest = new LoyaltyAdjustmentRequest();
                loyaltyRequest.setDelta(-actuallyReversed);
                loyaltyRequest.setReason("Return " + sale.getInvoiceNumber());
                customerService.adjustLoyaltyPoints(customer.getId(), loyaltyRequest);
            }
        }
        saleReturn.setLoyaltyPointsReversed(actuallyReversed);

        saleReturn = saleReturnRepository.save(saleReturn);
        return SaleReturnDTO.fromEntity(saleReturn);
    }

    @Transactional(readOnly = true)
    public List<SaleReturnDTO> getReturnsForSale(Long saleId, Long branchId) {
        loadOwnedSale(saleId, branchId);
        return saleReturnRepository.findBySale_Id(saleId).stream()
                .map(SaleReturnDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SaleReturnDTO> getRecentReturnsForBranch(Long branchId) {
        return saleReturnRepository.findBySale_Branch_IdOrderByCreatedAtDesc(branchId).stream()
                .map(SaleReturnDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Fills returnedQuantity / refundedAmount on sale DTOs so the Bills page can hide fully returned invoices. */
    @Transactional(readOnly = true)
    public void annotateReturnedQuantities(List<SaleDTO> sales) {
        if (sales == null || sales.isEmpty()) {
            return;
        }
        List<Long> saleIds = sales.stream().map(SaleDTO::getId).collect(Collectors.toList());
        List<SaleReturnItem> returnItems = saleReturnItemRepository.findBySaleReturn_Sale_IdIn(saleIds);
        Map<Long, Integer> qtyBySaleItem = new HashMap<>();
        for (SaleReturnItem item : returnItems) {
            qtyBySaleItem.merge(item.getOriginalItem().getId(), item.getQuantity(), Integer::sum);
        }

        Map<Long, BigDecimal> refundBySale = new HashMap<>();
        for (SaleReturn saleReturn : saleReturnRepository.findBySale_IdIn(saleIds)) {
            refundBySale.merge(saleReturn.getSale().getId(), saleReturn.getRefundAmount(), BigDecimal::add);
        }

        for (SaleDTO sale : sales) {
            sale.setRefundedAmount(refundBySale.getOrDefault(sale.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            if (sale.getItems() == null) {
                continue;
            }
            for (SaleItemDTO item : sale.getItems()) {
                item.setReturnedQuantity(qtyBySaleItem.getOrDefault(item.getId(), 0));
            }
        }
    }

    private Sale loadOwnedSale(Long saleId, Long branchId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));
        if (!sale.getBranch().getId().equals(branchId)) {
            throw new ResourceNotFoundException("Sale", "id", saleId);
        }
        return sale;
    }

    private Map<Long, Integer> returnedQuantityBySaleItem(Long saleId) {
        Map<Long, Integer> already = new HashMap<>();
        for (SaleReturnItem item : saleReturnItemRepository.findBySaleReturn_Sale_Id(saleId)) {
            already.merge(item.getOriginalItem().getId(), item.getQuantity(), Integer::sum);
        }
        return already;
    }

    private static boolean isClosingReturn(Sale sale, Map<Long, Integer> alreadyReturned, Map<Long, Integer> returningNow) {
        for (SaleItem item : sale.getItems()) {
            int remaining = item.getQuantity() - alreadyReturned.getOrDefault(item.getId(), 0)
                    - returningNow.getOrDefault(item.getId(), 0);
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Refund is the share of the original invoice total matching goods returned
     * (so cart-level discount and tax come back in proportion). The return that
     * closes the last remaining quantity takes any leftover paise.
     */
    private static BigDecimal computeRefund(Sale sale, BigDecimal goodsReturned, BigDecimal alreadyRefunded,
                                            boolean closesTheSale) {
        if (closesTheSale) {
            return sale.getTotalAmount().subtract(alreadyRefunded).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }
        if (sale.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return goodsReturned
                .divide(sale.getSubtotal(), 6, RoundingMode.HALF_UP)
                .multiply(sale.getTotalAmount())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static int computePointsToReverse(Sale sale, BigDecimal goodsReturned, int alreadyReversed,
                                              boolean closesTheSale) {
        int remaining = Math.max(0, sale.getLoyaltyPointsEarned() - alreadyReversed);
        if (remaining == 0) {
            return 0;
        }
        if (closesTheSale) {
            return remaining;
        }
        if (sale.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        int share = goodsReturned
                .divide(sale.getSubtotal(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(sale.getLoyaltyPointsEarned()))
                .setScale(0, RoundingMode.DOWN)
                .intValue();
        return Math.min(remaining, Math.max(0, share));
    }
}
