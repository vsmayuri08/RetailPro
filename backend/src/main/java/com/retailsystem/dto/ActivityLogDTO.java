package com.retailsystem.dto;

import com.retailsystem.entity.ActivityLog;

import java.time.LocalDateTime;

public class ActivityLogDTO {

    private Long id;
    private Long actorId;
    private String actorName;
    private String actorRole;
    private Long branchId;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime createdAt;

    public static ActivityLogDTO fromEntity(ActivityLog log) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.id = log.getId();
        dto.actorId = log.getActorId();
        dto.actorName = log.getActorName();
        dto.actorRole = log.getActorRole();
        dto.branchId = log.getBranchId();
        dto.action = log.getAction();
        dto.entityType = log.getEntityType();
        dto.entityId = log.getEntityId();
        dto.details = log.getDetails();
        dto.createdAt = log.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public String getActorRole() { return actorRole; }
    public Long getBranchId() { return branchId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
