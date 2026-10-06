package com.retailsystem.repository;

import com.retailsystem.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    boolean existsByEmployee_IdAndPeriodMonthAndPeriodYear(Long employeeId, int periodMonth, int periodYear);
    List<PayrollRecord> findByEmployee_IdOrderByPeriodYearDescPeriodMonthDesc(Long employeeId);
    List<PayrollRecord> findByEmployee_Branch_IdAndPeriodMonthAndPeriodYear(Long branchId, int periodMonth, int periodYear);
}
