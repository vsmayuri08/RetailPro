package com.retailsystem.dto;

import com.retailsystem.entity.Category;

import java.time.LocalDateTime;

public class CategoryDTO {

    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;

    public static CategoryDTO fromEntity(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.id = category.getId();
        dto.name = category.getName();
        dto.description = category.getDescription();
        dto.active = category.isActive();
        dto.createdAt = category.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
