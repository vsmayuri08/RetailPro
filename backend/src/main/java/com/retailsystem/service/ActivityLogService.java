package com.retailsystem.service;

import com.retailsystem.dto.ActivityLogDTO;
import com.retailsystem.entity.ActivityLog;
import com.retailsystem.repository.ActivityLogRepository;
import com.retailsystem.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Transactional
    public void record(CustomUserDetails principal, String action, String entityType, String entityId, String details) {
        Long actorId = principal != null ? principal.getId() : null;
        String actorName = principal != null ? principal.getFullName() : null;
        String actorRole = principal != null ? principal.getRole() : null;
        Long branchId = principal != null ? principal.getBranchId() : null;
        record(actorId, actorName, actorRole, branchId, action, entityType, entityId, details);
    }

    @Transactional
    public void record(Long actorId, String actorName, String actorRole, Long branchId,
                       String action, String entityType, String entityId, String details) {
        ActivityLog log = new ActivityLog();
        log.setActorId(actorId);
        log.setActorName(actorName);
        log.setActorRole(actorRole);
        log.setBranchId(branchId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDTO> listForAdmin() {
        return activityLogRepository.findTop200ByOrderByCreatedAtDesc().stream()
                .map(ActivityLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDTO> listForBranch(Long branchId) {
        return activityLogRepository.findTop200ByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(ActivityLogDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
