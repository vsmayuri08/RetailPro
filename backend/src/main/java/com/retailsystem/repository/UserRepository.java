package com.retailsystem.repository;

import com.retailsystem.entity.User;
import com.retailsystem.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByBranchId(Long branchId);
    List<User> findByBranchIdAndRole(Long branchId, Role role);
}
