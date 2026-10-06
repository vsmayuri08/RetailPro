package com.retailsystem.repository;

import com.retailsystem.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);
    List<Employee> findByBranchId(Long branchId);
}
