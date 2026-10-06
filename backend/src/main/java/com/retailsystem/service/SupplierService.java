package com.retailsystem.service;

import com.retailsystem.dto.SupplierDTO;
import com.retailsystem.dto.SupplierRequest;
import com.retailsystem.entity.Supplier;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Super Admin -> supplier management: add, edit, view, search, and
 * activate/deactivate suppliers. Suppliers aren't branch-scoped — the same
 * supplier can deliver to any branch. "Associate suppliers with inventory
 * batches" (from the spec) will be wired up once the InventoryBatch entity
 * exists as part of the fuller Inventory Management module.
 */
@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Transactional
    public SupplierDTO createSupplier(SupplierRequest request) {
        Supplier supplier = new Supplier(
                request.getName(),
                request.getContactPerson(),
                request.getPhone(),
                request.getEmail(),
                request.getAddress(),
                request.getGstNumber()
        );
        supplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(supplier);
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setGstNumber(request.getGstNumber());
        supplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(supplier);
    }

    @Transactional
    public SupplierDTO setSupplierActive(Long id, boolean active) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        supplier.setActive(active);
        supplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(supplier);
    }

    public SupplierDTO getSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return SupplierDTO.fromEntity(supplier);
    }

    public List<SupplierDTO> getAllSuppliers(String query) {
        List<Supplier> suppliers = (query == null || query.isBlank())
                ? supplierRepository.findAll()
                : supplierRepository.findByNameContainingIgnoreCase(query);
        return suppliers.stream().map(SupplierDTO::fromEntity).collect(Collectors.toList());
    }
}
