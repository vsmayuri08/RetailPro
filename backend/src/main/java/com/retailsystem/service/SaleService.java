package com.retailsystem.service;

import com.retailsystem.dto.CreateSaleRequest;
import com.retailsystem.dto.LoyaltyAdjustmentRequest;
import com.retailsystem.dto.SaleDTO;
import com.retailsystem.dto.SaleItemRequest;
import com.retailsystem.entity.*;
import com.retailsystem.exception.CustomerNotFoundException;
import com.retailsystem.exception.ExpiredProductException;
import com.retailsystem.exception.InsufficientStockException;
import com.retailsystem.exception.InvalidSaleException;
import com.retailsystem.exception.ProductNotFoundException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.CustomerRepository;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.SaleRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 9 -> the whole POS checkout flow, in one @Transactional method so
 * an incomplete sale (e.g. a mid-way stock failure) never leaves partial
 * rows behind. Also enforces the project-wide requirements "inventory
 * cannot become negative" and "expired products cannot be sold" at the one
 * place a sale is actually created.
 */
@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillingSettingsService billingSettingsService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ExpiryDiscountService expiryDiscountService;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Autowired
    private ReceiptTokenService receiptTokenService;

    @Autowired
    private InvoicePdfService invoicePdfService;

    @Transactional
    public SaleDTO checkout(Long branchId, Long cashierUserId, CreateSaleRequest request) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
        User processedBy = userRepository.findById(cashierUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", cashierUserId));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));
        }

        // Validate every line first (stock, active, expiry) before mutating anything, so a bad
        // item further down the cart never leaves earlier items' stock already deducted.
        List<Product> lineProducts = request.getItems().stream()
                .map(item -> validateLine(branchId, item))
                .collect(Collectors.toList());

        // Module 10 -> each line's effective price already has that product's current
        // expiry-based discount baked in (0% if none applies); subtotal is the sum of those.
        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < lineProducts.size(); i++) {
            Product product = lineProducts.get(i);
            int quantity = request.getItems().get(i).getQuantity();
            BigDecimal effectiveUnitPrice = effectivePrice(product);
            subtotal = subtotal.add(effectiveUnitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        BigDecimal discountAmount = request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount();
        if (discountAmount.compareTo(subtotal) > 0) {
            throw new InvalidSaleException("Discount cannot exceed the subtotal");
        }

        BillingSettings settings = billingSettingsService.getOrCreateSettings();
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount
                .multiply(settings.getTaxRatePercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = taxableAmount.add(taxAmount);

        if (request.getAmountPaid().compareTo(totalAmount) < 0) {
            throw new InvalidSaleException("Amount paid (" + request.getAmountPaid() + ") is less than the total due (" + totalAmount + ")");
        }
        BigDecimal changeAmount = request.getAmountPaid().subtract(totalAmount);

        int loyaltyPointsEarned = 0;
        if (customer != null && settings.getLoyaltyPointsPerAmount() > 0) {
            loyaltyPointsEarned = totalAmount
                    .divide(settings.getLoyaltyAmountThreshold(), 0, RoundingMode.DOWN)
                    .intValue() * settings.getLoyaltyPointsPerAmount();
        }

        Sale sale = new Sale(generateInvoiceNumber(branch), branch, customer, processedBy,
                subtotal, discountAmount, taxAmount, totalAmount);
        sale.setLoyaltyPointsEarned(loyaltyPointsEarned);

        for (int i = 0; i < lineProducts.size(); i++) {
            Product product = lineProducts.get(i);
            int quantity = request.getItems().get(i).getQuantity();
            BigDecimal originalUnitPrice = product.getPrice();
            BigDecimal discountPercent = expiryDiscountService.computeDiscountPercent(product.getExpiryDate());
            BigDecimal effectiveUnitPrice = effectivePrice(product);

            SaleItem saleItem = new SaleItem(sale, product, quantity, originalUnitPrice, discountPercent, effectiveUnitPrice);
            sale.getItems().add(saleItem);

            // Module 5 -> FIFO: deducts from the earliest-expiring batch(es) first and keeps
            // product.quantity/expiryDate in sync with what's actually left in stock, instead of
            // a raw decrement.
            inventoryBatchService.deductFifo(product, quantity);
        }

        Payment payment = new Payment(sale, request.getPaymentMethod(), request.getAmountPaid(), changeAmount);
        sale.setPayment(payment);

        sale = saleRepository.save(sale);

        if (customer != null && loyaltyPointsEarned > 0) {
            LoyaltyAdjustmentRequest loyaltyRequest = new LoyaltyAdjustmentRequest();
            loyaltyRequest.setDelta(loyaltyPointsEarned);
            loyaltyRequest.setReason("Purchase " + sale.getInvoiceNumber());
            customerService.adjustLoyaltyPoints(customer.getId(), loyaltyRequest);
        }

        return toDto(sale);
    }

    public SaleDTO getSale(Long id, Long branchScope) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        if (branchScope != null && !sale.getBranch().getId().equals(branchScope)) {
            throw new ResourceNotFoundException("Sale", "id", id);
        }
        return toDto(sale);
    }

    public Sale getOwnedSaleOrThrow(Long id, Long branchScope) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        if (branchScope != null && !sale.getBranch().getId().equals(branchScope)) {
            throw new ResourceNotFoundException("Sale", "id", id);
        }
        return sale;
    }

    @Transactional(readOnly = true)
    public byte[] invoicePdf(Long id, Long branchScope) {
        Sale sale = getOwnedSaleOrThrow(id, branchScope);
        return invoicePdfService.generate(sale);
    }

    @Transactional(readOnly = true)
    public byte[] publicInvoicePdf(String invoiceNumber, String token) {
        return invoicePdfService.generate(publicSale(invoiceNumber, token));
    }

    @Transactional(readOnly = true)
    public SaleDTO publicReceipt(String invoiceNumber, String token) {
        return toDto(publicSale(invoiceNumber, token));
    }

    private Sale publicSale(String invoiceNumber, String token) {
        if (!receiptTokenService.matches(invoiceNumber, token)) {
            throw new ResourceNotFoundException("Sale", "invoiceNumber", invoiceNumber);
        }
        return saleRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "invoiceNumber", invoiceNumber));
    }

    public List<SaleDTO> getRecentSalesForBranch(Long branchId) {
        return saleRepository.findByBranch_IdOrderByCreatedAtDesc(branchId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Powers the Customers page's purchase history — closes the gap left as a placeholder earlier. */
    public List<SaleDTO> getSalesForCustomer(Long customerId) {
        return saleRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SaleDTO toDto(Sale sale) {
        SaleDTO dto = SaleDTO.fromEntity(sale);
        dto.setReceiptToken(receiptTokenService.sign(sale.getInvoiceNumber()));
        return dto;
    }

    private Product validateLine(Long branchId, SaleItemRequest item) {
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
        if (!product.getBranch().getId().equals(branchId)) {
            throw new ProductNotFoundException(item.getProductId());
        }
        if (!product.isActive()) {
            throw new InvalidSaleException(product.getName() + " is not available for sale");
        }
        if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ExpiredProductException(product.getName() + " has expired and cannot be sold");
        }
        if (product.getQuantity() < item.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for " + product.getName() + " (available: " + product.getQuantity() + ")");
        }
        return product;
    }

    /** Module 10 -> the product's catalog price with its current expiry discount (if any) applied. */
    private BigDecimal effectivePrice(Product product) {
        BigDecimal discountPercent = expiryDiscountService.computeDiscountPercent(product.getExpiryDate());
        if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return product.getPrice();
        }
        return product.getPrice()
                .multiply(BigDecimal.ONE.subtract(discountPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Human-readable, per-branch-per-day sequential invoice numbers, e.g. INV-DT01-20260808-0001. */
    private String generateInvoiceNumber(Branch branch) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        String datePart = today.toString().replace("-", "");

        long baseCount = saleRepository.countByBranch_IdAndCreatedAtBetween(branch.getId(), startOfDay, endOfDay);
        for (int attempt = 1; attempt <= 5; attempt++) {
            String candidate = String.format("INV-%s-%s-%04d", branch.getBranchCode(), datePart, baseCount + attempt);
            if (!saleRepository.existsByInvoiceNumber(candidate)) {
                return candidate;
            }
        }
        // Extremely unlikely fallback if the sequential slots above all collided.
        return "INV-" + branch.getBranchCode() + "-" + System.currentTimeMillis();
    }
}
