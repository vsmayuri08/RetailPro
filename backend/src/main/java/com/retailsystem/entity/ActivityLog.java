package com.retailsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Append-only activity trail for trust/demo: who did what, without changing
 * sale/checkout rows. Written from controllers after a successful action.
 */
@Entity
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity {

    private Long actorId;
    private String actorName;
    private String actorRole;
    private Long branchId;

    @Column(nullable = false)
    private String action;

    private String entityType;
    private String entityId;

    @Column(length = 1000)
    private String details;

    public ActivityLog() {
    }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
