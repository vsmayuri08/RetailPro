package com.retailsystem.service;

import com.retailsystem.dto.ProductDTO;
import com.retailsystem.dto.ProductRequest;
import com.retailsystem.dto.StockAdjustmentRequest;
import com.retailsystem.entity.Branch;
import com.retailsystem.entity.Category;
import com.retailsystem.entity.Product;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.DuplicateProductException;
import com.retailsystem.exception.ProductNotFoundException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.CategoryRepository;
import com.retailsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Branch Manager -> "Add and update products" + "Manage inventory".
 * Every method is scoped to a single branchId (the caller's own branch,
 * resolved from the JWT in the controller) — a manager can never touch
 * another branch's catalog through these endpoints.
 *
 * Module 5 -> quantity and expiryDate are set directly only at creation
 * (which also opens the product's first InventoryBatch, via
 * InventoryBatchService). After that, they're batch-derived: updateProduct
 * deliberately does NOT let the edit form overwrite them anymore, since
 * that would desync the cached Product fields from real batch data. Stock
 * only changes from here on via receiveStock (new batch), adjustStock
 * (manual correction), or a completed sale (FIFO deduction) — see
 * InventoryBatchService.
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpiryDiscountService expiryDiscountService;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Transactional
    public ProductDTO createProduct(Long branchId, ProductRequest request) {
        if (productRepository.existsByBranchIdAndSkuIgnoreCase(branchId, request.getSku())) {
            throw new DuplicateProductException("A product with SKU '" + request.getSku() + "' already exists in this branch");
        }
        Branch branch = getBranchOrThrow(branchId);
        Category category = resolveCategoryOrNull(request.getCategoryId());
        Product product = new Product(
                request.getName(),
                request.getSku().toUpperCase(),
                category,
                request.getPrice(),
                request.getQuantity(),
                request.getReorderLevel(),
                request.getExpiryDate(),
                branch
        );
        product.setBarcode(request.getBarcode());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setCostPrice(request.getCostPrice());
        product = productRepository.save(product);
        inventoryBatchService.createOpeningBatch(product, request.getQuantity(), request.getExpiryDate());
        return toDto(product);
    }

    @Transactional
    public ProductDTO updateProduct(Long branchId, Long productId, ProductRequest request) {
        Product product = getOwnedProductOrThrow(branchId, productId);

        if (!product.getSku().equalsIgnoreCase(request.getSku())
                && productRepository.existsByBranchIdAndSkuIgnoreCase(branchId, request.getSku())) {
            throw new DuplicateProductException("A product with SKU '" + request.getSku() + "' already exists in this branch");
        }

        // Catalog fields only — quantity/expiryDate are batch-derived now (see class Javadoc)
        // and are intentionally not touched here, even though ProductRequest still carries them
        // (that DTO is shared with createProduct, where they set the opening batch).
        product.setName(request.getName());
        product.setSku(request.getSku().toUpperCase());
        product.setBarcode(request.getBarcode());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setCategory(resolveCategoryOrNull(request.getCategoryId()));
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setReorderLevel(request.getReorderLevel());
        product = productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public ProductDTO setProductActive(Long branchId, Long productId, boolean active) {
        Product product = getOwnedProductOrThrow(branchId, productId);
        product.setActive(active);
        product = productRepository.save(product);
        return toDto(product);
    }

    /** Branch Manager -> "Manage inventory": quick stock +/- with an optional reason (manual correction, not a receipt). */
    @Transactional
    public ProductDTO adjustStock(Long branchId, Long productId, StockAdjustmentRequest request) {
        Product product = getOwnedProductOrThrow(branchId, productId);
        int newQuantity = product.getQuantity() + request.getDelta();
        if (newQuantity < 0) {
            throw new BadRequestException("Adjustment would leave stock below zero (current: " + product.getQuantity() + ")");
        }
        product.setQuantity(newQuantity);
        product = productRepository.save(product);
        return toDto(product);
    }

    public ProductDTO getProduct(Long branchId, Long productId) {
        return toDto(getOwnedProductOrThrow(branchId, productId));
    }

    public List<ProductDTO> getAllProducts(Long branchId) {
        return productRepository.findByBranchId(branchId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getAlerts(Long branchId) {
        return productRepository.findByBranchIdAndActiveTrue(branchId).stream()
                .map(this::toDto)
                .filter(dto -> dto.isLowStock() || dto.isNearExpiry() || dto.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Module 9 -> Cashier product search: matches name, SKU, or barcode, per the spec. Also used
     * for browsing another branch's catalog when building a stock transfer request.
     */
    public List<ProductDTO> searchBranchCatalog(Long branchId, String query) {
        List<Product> products = (query == null || query.isBlank())
                ? productRepository.findByBranchIdAndActiveTrue(branchId)
                : productRepository.searchActiveByNameOrSku(branchId, query);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Webcam / handheld scan: exact barcode first, then exact SKU. */
    public ProductDTO lookupByScanCode(Long branchId, String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Scan code is required");
        }
        String trimmed = code.trim();
        Product product = productRepository.findByBranchIdAndBarcodeIgnoreCase(branchId, trimmed)
                .or(() -> productRepository.findByBranchIdAndSkuIgnoreCase(branchId, trimmed))
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", trimmed));
        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product", "barcode", trimmed);
        }
        return toDto(product);
    }

    Product getOwnedProductOrThrow(Long branchId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!product.getBranch().getId().equals(branchId)) {
            throw new ProductNotFoundException(productId);
        }
        return product;
    }

    private Branch getBranchOrThrow(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
    }

    /** Module 3+4 -> a product's category is optional; null categoryId simply means uncategorized. */
    private Category resolveCategoryOrNull(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    /** Module 10 -> enriches the base DTO with the currently-applicable expiry discount, if any. */
    private ProductDTO toDto(Product product) {
        ProductDTO dto = ProductDTO.fromEntity(product);
        BigDecimal discountPercent = expiryDiscountService.computeDiscountPercent(product.getExpiryDate());
        dto.setExpiryDiscountPercent(discountPercent);
        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discounted = product.getPrice()
                    .multiply(BigDecimal.ONE.subtract(discountPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setDiscountedPrice(discounted);
        }
        return dto;
    }
}
