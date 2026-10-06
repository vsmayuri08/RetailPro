package com.retailsystem.service;

import com.retailsystem.dto.GeneratePayrollRequest;
import com.retailsystem.dto.PayrollRecordDTO;
import com.retailsystem.entity.Employee;
import com.retailsystem.entity.PayrollRecord;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.repository.PayrollRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 8 -> "Calculate monthly payroll" + "Generate salary records".
 * Generation snapshots the employee's current salary figures into an
 * immutable {@link PayrollRecord} — see that entity's Javadoc for why.
 * Generating twice for the same employee + month + year is rejected rather
 * than silently overwriting or duplicating.
 */
@Service
public class PayrollService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollRecordRepository payrollRecordRepository;

    @Transactional
    public PayrollRecordDTO generatePayroll(Long employeeId, GeneratePayrollRequest request, Long branchScope) {
        Employee employee = employeeService.getOwnedOrThrow(employeeId, branchScope);

        if (payrollRecordRepository.existsByEmployee_IdAndPeriodMonthAndPeriodYear(
                employeeId, request.getPeriodMonth(), request.getPeriodYear())) {
            throw new BadRequestException("Payroll for " + employee.getFullName() + " has already been generated for "
                    + request.getPeriodMonth() + "/" + request.getPeriodYear());
        }

        PayrollRecord record = new PayrollRecord(
                employee,
                request.getPeriodMonth(),
                request.getPeriodYear(),
                employee.getBasicSalary(),
                employee.getAllowances(),
                employee.calculateMonthlyBonus(),
                employee.getDeductions(),
                employee.calculateNetPay()
        );
        record = payrollRecordRepository.save(record);
        return PayrollRecordDTO.fromEntity(record);
    }

    public List<PayrollRecordDTO> getPayrollHistory(Long employeeId, Long branchScope) {
        // Confirms the caller is allowed to see this employee before returning their payroll history.
        employeeService.getOwnedOrThrow(employeeId, branchScope);
        return payrollRecordRepository.findByEmployee_IdOrderByPeriodYearDescPeriodMonthDesc(employeeId).stream()
                .map(PayrollRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
