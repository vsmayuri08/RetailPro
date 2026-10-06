package com.retailsystem.service;

import com.retailsystem.dto.CategoryDTO;
import com.retailsystem.dto.CategoryRequest;
import com.retailsystem.entity.Category;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Super Admin -> category management: add, edit, view, deactivate, and
 * prevent duplicate category names. Categories are org-wide (not
 * branch-scoped) since the same classification applies across every
 * branch's product catalog.
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public CategoryDTO createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("A category named '" + request.getName() + "' already exists");
        }
        Category category = new Category(request.getName(), request.getDescription());
        category = categoryRepository.save(category);
        return CategoryDTO.fromEntity(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("A category named '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category = categoryRepository.save(category);
        return CategoryDTO.fromEntity(category);
    }

    @Transactional
    public CategoryDTO setCategoryActive(Long id, boolean active) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setActive(active);
        category = categoryRepository.save(category);
        return CategoryDTO.fromEntity(category);
    }

    public CategoryDTO getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryDTO.fromEntity(category);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
