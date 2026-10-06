package com.retailsystem.service;

import com.retailsystem.dto.EmployeeDTO;
import com.retailsystem.dto.EmployeeRequest;
import com.retailsystem.entity.Branch;
import com.retailsystem.entity.CashierEmployee;
import com.retailsystem.entity.Employee;
import com.retailsystem.entity.InventoryStaffEmployee;
import com.retailsystem.entity.ManagerEmployee;
import com.retailsystem.enums.EmployeeType;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Employee HR/payroll records — a different concept from {@link com.retailsystem.entity.User}
 * (login accounts). Super Admin manages every employee org-wide and can
 * assign any branch; Branch Manager can only manage employees already
 * scoped to their own branch (methods that take a {@code branchScope}
 * parameter enforce that — pass {@code null} for the unrestricted,
 * Super-Admin path).
 */
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Transactional
    public EmployeeDTO createEmployee(EmployeeRequest request, Long branchScope) {
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(request.getEmployeeCode())) {
            throw new BadRequestException("An employee with code '" + request.getEmployeeCode() + "' already exists");
        }

        Long targetBranchId = branchScope != null ? branchScope : request.getBranchId();
        if (targetBranchId == null) {
            throw new BadRequestException("A branch must be specified for this employee");
        }
        Branch branch = branchRepository.findById(targetBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", targetBranchId));

        if (branchScope != null && request.getEmployeeType() == EmployeeType.MANAGER) {
            throw new BadRequestException("Branch Managers cannot be added from the Branch Manager screen — ask a Super Admin");
        }

        Employee employee = instantiate(request, branch);
        employee = employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(employee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeRequest request, Long branchScope) {
        Employee employee = getOwnedOrThrow(id, branchScope);

        if (!employee.getEmployeeCode().equalsIgnoreCase(request.getEmployeeCode())
                && employeeRepository.existsByEmployeeCodeIgnoreCase(request.getEmployeeCode())) {
            throw new BadRequestException("An employee with code '" + request.getEmployeeCode() + "' already exists");
        }

        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setBasicSalary(request.getBasicSalary());
        employee.setAllowances(request.getAllowances());
        employee.setDeductions(request.getDeductions());

        // Only the unrestricted (Super Admin) path may reassign the branch. Employee type is
        // intentionally not changeable here — switching MANAGER/CASHIER/INVENTORY_STAFF means
        // swapping the JPA discriminator subclass, which this simplified model doesn't support;
        // deactivate and re-create the employee under the new type instead.
        if (branchScope == null && request.getBranchId() != null
                && !request.getBranchId().equals(employee.getBranch().getId())) {
            Branch newBranch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            employee.setBranch(newBranch);
        }

        employee = employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(employee);
    }

    @Transactional
    public EmployeeDTO setActive(Long id, boolean active, Long branchScope) {
        Employee employee = getOwnedOrThrow(id, branchScope);
        employee.setActive(active);
        employee = employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(employee);
    }

    public EmployeeDTO getEmployee(Long id, Long branchScope) {
        return EmployeeDTO.fromEntity(getOwnedOrThrow(id, branchScope));
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream().map(EmployeeDTO::fromEntity).collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByBranch(Long branchId) {
        return employeeRepository.findByBranchId(branchId).stream().map(EmployeeDTO::fromEntity).collect(Collectors.toList());
    }

    /** Package-visible so PayrollService can resolve an Employee under the same branch-scoping rule. */
    Employee getOwnedOrThrow(Long id, Long branchScope) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        if (branchScope != null && (employee.getBranch() == null || !employee.getBranch().getId().equals(branchScope))) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        return employee;
    }

    private Employee instantiate(EmployeeRequest request, Branch branch) {
        switch (request.getEmployeeType()) {
            case MANAGER:
                return new ManagerEmployee(request.getEmployeeCode(), request.getFullName(), request.getPhone(),
                        request.getEmail(), branch, request.getJoiningDate(), request.getBasicSalary(),
                        request.getAllowances(), request.getDeductions());
            case CASHIER:
                return new CashierEmployee(request.getEmployeeCode(), request.getFullName(), request.getPhone(),
                        request.getEmail(), branch, request.getJoiningDate(), request.getBasicSalary(),
                        request.getAllowances(), request.getDeductions());
            case INVENTORY_STAFF:
                return new InventoryStaffEmployee(request.getEmployeeCode(), request.getFullName(), request.getPhone(),
                        request.getEmail(), branch, request.getJoiningDate(), request.getBasicSalary(),
                        request.getAllowances(), request.getDeductions());
            default:
                throw new BadRequestException("Unsupported employee type: " + request.getEmployeeType());
        }
    }
}
