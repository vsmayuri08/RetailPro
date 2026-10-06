package com.retailsystem.repository;

import com.retailsystem.entity.Branch;
import com.retailsystem.enums.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);
    List<Branch> findByStatus(BranchStatus status);
}
