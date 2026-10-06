package com.retailsystem.repository;

import com.retailsystem.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop200ByOrderByCreatedAtDesc();
    List<ActivityLog> findTop200ByBranchIdOrderByCreatedAtDesc(Long branchId);
}
