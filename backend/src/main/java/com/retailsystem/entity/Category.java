package com.retailsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Org-wide product category (e.g. "Groceries", "Dairy"). Managed centrally
 * by the Super Admin, same as branches — categories aren't branch-scoped
 * since the same classification is shared across every branch's catalog.
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name", columnNames = "name")
})
public class Category extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.active = true;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
