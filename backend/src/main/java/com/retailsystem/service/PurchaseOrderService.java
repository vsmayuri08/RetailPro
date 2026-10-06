package com.retailsystem.service;

import com.retailsystem.dto.CreatePurchaseOrdersResultDTO;
import com.retailsystem.dto.PurchaseOrderDTO;
import com.retailsystem.dto.PurchaseOrderItemDTO;
import com.retailsystem.dto.ReceiveStockRequest;
import com.retailsystem.dto.ReorderSuggestionItemDTO;
import com.retailsystem.dto.ReorderSuggestionsSummaryDTO;
import com.retailsystem.entity.Branch;
import com.retailsystem.entity.InventoryBatch;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.PurchaseOrder;
import com.retailsystem.entity.PurchaseOrderItem;
import com.retailsystem.entity.Supplier;
import com.retailsystem.entity.User;
import com.retailsystem.enums.PurchaseOrderStatus;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.InventoryBatchRepository;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.PurchaseOrderRepository;
import com.retailsystem.repository.SupplierRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Automatic purchase orders generated from reorder suggestions. Orders stay
 * inside RetailPro — nothing is emailed or posted to a supplier. A manager
 * must approve before stock can be received.
 */
@Service
public class PurchaseOrderService {

    private static final Set<PurchaseOrderStatus> OPEN = EnumSet.of(
            PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.PENDING, PurchaseOrderStatus.APPROVED);

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ReorderSuggestionsService reorderSuggestionsService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Transactional
    public CreatePurchaseOrdersResultDTO createFromReorder(Long branchId, Long userId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ReorderSuggestionsSummaryDTO suggestions = reorderSuggestionsService.generateSuggestions(branchId, null, null);
        List<Long> openProductIds = purchaseOrderRepository.findProductIdsOnOpenOrders(branchId, OPEN);
        Set<Long> alreadyOpen = Set.copyOf(openProductIds);

        CreatePurchaseOrdersResultDTO result = new CreatePurchaseOrdersResultDTO();
        Map<Long, List<ReorderSuggestionItemDTO>> bySupplier = new LinkedHashMap<>();

        for (ReorderSuggestionItemDTO item : suggestions.getItems()) {
            if (item.getSuggestedQty() <= 0) {
                continue;
            }
            Long supplierId = item.getSuggestedSupplierId();
            if (supplierId == null) {
                Supplier fallback = defaultSupplier();
                if (fallback == null) {
                    result.getSkippedNoSupplier().add(item.getProductName());
                    continue;
                }
                supplierId = fallback.getId();
            }
            if (alreadyOpen.contains(item.getProductId())) {
                result.getSkippedOpenOrder().add(item.getProductName());
                continue;
            }
            bySupplier.computeIfAbsent(supplierId, k -> new ArrayList<>()).add(item);
        }

        addCatalogLowStock(branchId, alreadyOpen, bySupplier, result);

        if (bySupplier.isEmpty()) {
            throw new BadRequestException(result.getSkippedNoSupplier().isEmpty() && result.getSkippedOpenOrder().isEmpty()
                    ? "No products currently need a reorder."
                    : "Nothing new to order. Products either lack a supplier or already sit on a draft/pending/approved PO.");
        }

        for (Map.Entry<Long, List<ReorderSuggestionItemDTO>> entry : bySupplier.entrySet()) {
            Supplier supplier = supplierRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", entry.getKey()));
            PurchaseOrder po = new PurchaseOrder();
            po.setPoNumber(nextPoNumber(branch));
            po.setBranch(branch);
            po.setSupplier(supplier);
            po.setStatus(PurchaseOrderStatus.DRAFT);
            po.setCreatedBy(user);
            po.setNote("Generated from reorder suggestions. Not sent to the supplier.");

            int maxLead = 14;
            BigDecimal total = BigDecimal.ZERO;
            for (ReorderSuggestionItemDTO row : entry.getValue()) {
                Product product = productRepository.findById(row.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", row.getProductId()));
                if (!product.getBranch().getId().equals(branchId)) {
                    throw new BadRequestException("Product " + product.getSku() + " is not on this branch");
                }
                BigDecimal unit = row.getLastUnitCost() != null
                        ? row.getLastUnitCost()
                        : (product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                unit = unit.setScale(2, RoundingMode.HALF_UP);
                PurchaseOrderItem line = new PurchaseOrderItem(product, row.getSuggestedQty(), unit);
                po.addItem(line);
                total = total.add(line.getLineTotal());
                maxLead = Math.max(maxLead, row.getLeadTimeDays());
            }
            po.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
            po.setExpectedDeliveryDate(LocalDate.now().plusDays(maxLead));
            po = purchaseOrderRepository.save(po);
            result.getOrders().add(toDto(po));
        }

        result.setNotice("Purchase orders were saved as Draft in RetailPro. They are not sent to suppliers. Approve each order, then mark it Received to add stock.");
        return result;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> list(Long branchId) {
        return purchaseOrderRepository.findByBranch_IdOrderByCreatedAtDesc(branchId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDTO get(Long branchId, Long id) {
        return toDto(owned(branchId, id));
    }

    @Transactional
    public PurchaseOrderDTO submit(Long branchId, Long id) {
        PurchaseOrder po = owned(branchId, id);
        requireStatus(po, PurchaseOrderStatus.DRAFT, "Only a draft can be submitted for approval");
        po.setStatus(PurchaseOrderStatus.PENDING);
        return toDto(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderDTO approve(Long branchId, Long userId, Long id) {
        PurchaseOrder po = owned(branchId, id);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT && po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new BadRequestException("Only draft or pending orders can be approved");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(user);
        po.setApprovedAt(LocalDateTime.now());
        return toDto(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderDTO cancel(Long branchId, Long id) {
        PurchaseOrder po = owned(branchId, id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("This purchase order cannot be cancelled");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return toDto(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderDTO receive(Long branchId, Long id) {
        PurchaseOrder po = owned(branchId, id);
        requireStatus(po, PurchaseOrderStatus.APPROVED, "Receive stock only after the manager approves the purchase order");
        for (PurchaseOrderItem line : po.getItems()) {
            ReceiveStockRequest request = new ReceiveStockRequest();
            request.setQuantityReceived(line.getQuantity());
            request.setPurchasePrice(line.getPurchasePrice());
            request.setSupplierId(po.getSupplier().getId());
            request.setReceivedDate(LocalDate.now());
            request.setExpiryDate(line.getProduct().getExpiryDate());
            request.setBatchNumber("PO-" + po.getPoNumber() + "-" + line.getProduct().getSku());
            inventoryBatchService.receiveStock(branchId, line.getProduct().getId(), request);
        }
        po.setStatus(PurchaseOrderStatus.RECEIVED);
        po.setReceivedAt(LocalDateTime.now());
        return toDto(purchaseOrderRepository.save(po));
    }

    private void addCatalogLowStock(Long branchId, Set<Long> alreadyOpen,
                                    Map<Long, List<ReorderSuggestionItemDTO>> bySupplier,
                                    CreatePurchaseOrdersResultDTO result) {
        Set<Long> included = bySupplier.values().stream()
                .flatMap(List::stream)
                .map(ReorderSuggestionItemDTO::getProductId)
                .collect(Collectors.toSet());
        Map<Long, List<InventoryBatch>> batchesByProduct = inventoryBatchRepository.findByBranchIdWithSupplier(branchId)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getProduct().getId()));

        for (Product product : productRepository.findByBranchIdAndActiveTrue(branchId)) {
            if (product.getQuantity() > product.getReorderLevel()) {
                continue;
            }
            if (included.contains(product.getId())) {
                continue;
            }
            if (alreadyOpen.contains(product.getId())) {
                result.getSkippedOpenOrder().add(product.getName());
                continue;
            }
            InventoryBatch lastPurchase = batchesByProduct.getOrDefault(product.getId(), List.of()).stream()
                    .filter(b -> b.getSupplier() != null)
                    .max(Comparator.comparing(b -> b.getReceivedDate() != null ? b.getReceivedDate() : LocalDate.MIN))
                    .orElse(null);
            Long supplierId;
            BigDecimal unitCost = product.getPrice();
            if (lastPurchase != null) {
                supplierId = lastPurchase.getSupplier().getId();
                if (lastPurchase.getPurchasePrice() != null) {
                    unitCost = lastPurchase.getPurchasePrice();
                }
            } else {
                Supplier fallback = defaultSupplier();
                if (fallback == null) {
                    result.getSkippedNoSupplier().add(product.getName());
                    continue;
                }
                supplierId = fallback.getId();
            }
            ReorderSuggestionItemDTO row = new ReorderSuggestionItemDTO();
            row.setProductId(product.getId());
            row.setProductName(product.getName());
            row.setSuggestedQty(Math.max(1, product.getReorderLevel() - product.getQuantity()));
            row.setSuggestedSupplierId(supplierId);
            row.setLastUnitCost(unitCost);
            row.setLeadTimeDays(14);
            bySupplier.computeIfAbsent(supplierId, k -> new ArrayList<>()).add(row);
        }
    }

    private Supplier defaultSupplier() {
        return supplierRepository.findAll().stream()
                .filter(Supplier::isActive)
                .findFirst()
                .orElse(null);
    }

    private PurchaseOrder owned(Long branchId, Long id) {
        return purchaseOrderRepository.findByIdAndBranch_Id(id, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    private static void requireStatus(PurchaseOrder po, PurchaseOrderStatus expected, String message) {
        if (po.getStatus() != expected) {
            throw new BadRequestException(message);
        }
    }

    private String nextPoNumber(Branch branch) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        String datePart = today.toString().replace("-", "");
        long baseCount = purchaseOrderRepository.countByBranch_IdAndCreatedAtBetween(branch.getId(), startOfDay, endOfDay);
        for (int attempt = 1; attempt <= 8; attempt++) {
            String candidate = String.format("PO-%s-%s-%04d", branch.getBranchCode(), datePart, baseCount + attempt);
            if (!purchaseOrderRepository.existsByPoNumber(candidate)) {
                return candidate;
            }
        }
        return "PO-" + branch.getBranchCode() + "-" + System.currentTimeMillis();
    }

    private PurchaseOrderDTO toDto(PurchaseOrder po) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(po.getId());
        dto.setPoNumber(po.getPoNumber());
        dto.setBranchId(po.getBranch().getId());
        dto.setBranchName(po.getBranch().getBranchName());
        dto.setSupplierId(po.getSupplier().getId());
        dto.setSupplierName(po.getSupplier().getName());
        dto.setStatus(po.getStatus().name());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setTotalAmount(po.getTotalAmount());
        dto.setCreatedByName(po.getCreatedBy() != null ? po.getCreatedBy().getFullName() : null);
        dto.setApprovedByName(po.getApprovedBy() != null ? po.getApprovedBy().getFullName() : null);
        dto.setApprovedAt(po.getApprovedAt());
        dto.setReceivedAt(po.getReceivedAt());
        dto.setCreatedAt(po.getCreatedAt());
        dto.setNote(po.getNote());
        dto.setItems(po.getItems().stream().map(line -> {
            PurchaseOrderItemDTO item = new PurchaseOrderItemDTO();
            item.setProductId(line.getProduct().getId());
            item.setProductName(line.getProduct().getName());
            item.setSku(line.getProduct().getSku());
            item.setQuantity(line.getQuantity());
            item.setPurchasePrice(line.getPurchasePrice());
            item.setLineTotal(line.getLineTotal());
            return item;
        }).collect(Collectors.toList()));
        return dto;
    }
}
