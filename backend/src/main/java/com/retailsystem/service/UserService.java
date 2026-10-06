package com.retailsystem.service;

import com.retailsystem.dto.CreateEmployeeRequest;
import com.retailsystem.dto.CreateManagerRequest;
import com.retailsystem.dto.UserDTO;
import com.retailsystem.entity.Branch;
import com.retailsystem.entity.User;
import com.retailsystem.enums.Role;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserDTO.fromEntity(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDTO.fromEntity(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByBranch(Long branchId) {
        return userRepository.findByBranchId(branchId).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Super Admin -> "Create branch managers". */
    @Transactional
    public UserDTO createBranchManager(CreateManagerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A user with email '" + request.getEmail() + "' already exists");
        }
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        User manager = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.BRANCH_MANAGER,
                true,
                branch
        );
        manager = userRepository.save(manager);
        return UserDTO.fromEntity(manager);
    }

    @Transactional
    public UserDTO setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new BadRequestException("The Super Admin account cannot be deactivated");
        }
        user.setActive(active);
        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    /** Branch Manager -> "Add employees". Always creates a CASHIER in the manager's own branch. */
    @Transactional
    public UserDTO createEmployee(Long branchId, CreateEmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A user with email '" + request.getEmail() + "' already exists");
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));

        User employee = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.CASHIER,
                true,
                branch
        );
        employee = userRepository.save(employee);
        return UserDTO.fromEntity(employee);
    }

    public List<UserDTO> getEmployeesByBranch(Long branchId) {
        return userRepository.findByBranchIdAndRole(branchId, Role.CASHIER).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Enforces that the target user belongs to the given branch before allowing a status change. */
    @Transactional
    public UserDTO setEmployeeActiveInBranch(Long branchId, Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        user.setActive(active);
        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }
}
