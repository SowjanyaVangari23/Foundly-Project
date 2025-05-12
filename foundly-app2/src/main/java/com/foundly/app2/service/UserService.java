package com.foundly.app2.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foundly.app2.dto.EditUserDTO;
//import com.foundly.app2.dto.ForgotPasswordDTO;
import com.foundly.app2.dto.UserLoginRequest;
import com.foundly.app2.dto.UserRegistrationRequest;
import com.foundly.app2.dto.UserRequestDTO;
import com.foundly.app2.entity.Employee;
import com.foundly.app2.entity.User;
import com.foundly.app2.exception.DuplicateItemException;
import com.foundly.app2.exception.InvalidRequestException;
import com.foundly.app2.exception.UserNotFoundException;
import com.foundly.app2.repository.EmployeeRepository;
import com.foundly.app2.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionsService transactionsService;

    @Autowired
    private ItemReportsService itemReportsService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Added method to fix EmployeeService error
    public Optional<User> getUserByEmployeeId(String employeeId) {
        return userRepository.findByEmployeeId(employeeId);
    }

    public User registerUser(UserRegistrationRequest registrationRequest) {
        // Since UserRegistrationRequest has no username, use employeeId as username

        String username = registrationRequest.getEmployeeId();

        // Validate employee existence and matching details for non-admin users
        boolean validEmployee = employeeService.validateEmployeeDetails(
                registrationRequest.getEmployeeId(),
                registrationRequest.getName(),
                registrationRequest.getEmail()
        );
        if (!validEmployee) {
            throw new InvalidRequestException("Employee details do not match any existing employee record.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateItemException("Username already exists");
        }
        User user = new User();
        user.setName(registrationRequest.getName());
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setEmployeeId(registrationRequest.getEmployeeId());
        user.setRole(User.Role.USER);
        user.setSecurity(registrationRequest.isSecurity());
        return userRepository.save(user);
    }

    public Optional<User> loginUser(UserLoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public User saveUser(User user) {
        Optional<User> existingUserOpt = userRepository.findById(user.getUserId());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setName(user.getName());
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            existingUser.setRole(user.getRole());
            existingUser.setSecurity(user.isSecurity());
            existingUser.setEmployeeId(user.getEmployeeId());
            return userRepository.save(existingUser);
        } else {
            throw new UserNotFoundException("User not found with id: " + user.getUserId());
        }
    }

    public User createUserWithRole(UserRequestDTO userRequestDTO) {
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new DuplicateItemException("Email already exists");
        }
        if (userRepository.findByUsername(userRequestDTO.getUsername()).isPresent()) {
            throw new DuplicateItemException("Username already exists");
        }
        if (userRequestDTO.getEmployeeId() == null || userRequestDTO.getEmployeeId().isEmpty()) {
            throw new InvalidRequestException("Employee ID is required");
        }
        // Validate employee existence only for non-admin users
        if (!userRequestDTO.getRole().equalsIgnoreCase("ADMIN")) {
            if (!employeeRepository.findByEmpId(userRequestDTO.getEmployeeId()).isPresent()) {
                throw new InvalidRequestException("Employee ID does not exist");
            }
        } else {
            // For admin users, create employee if not exists
            if (!employeeRepository.findByEmpId(userRequestDTO.getEmployeeId()).isPresent()) {
                Employee newEmployee = new Employee();
                newEmployee.setEmpId(userRequestDTO.getEmployeeId());
                newEmployee.setName(userRequestDTO.getName());
                newEmployee.setEmpEmailId(userRequestDTO.getEmail());
                employeeService.createEmployee(newEmployee);
            }
        }
        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setUsername(userRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setEmployeeId(userRequestDTO.getEmployeeId());
        user.setRole(User.Role.valueOf(userRequestDTO.getRole()));
        user.setSecurity(userRequestDTO.isSecurity());
        return userRepository.save(user);
    }

    public User updateUser(UserRequestDTO userRequestDTO) {
        Optional<User> existingUserOpt = userRepository.findById(userRequestDTO.getUserId());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setName(userRequestDTO.getName());
            existingUser.setEmail(userRequestDTO.getEmail());
            existingUser.setUsername(userRequestDTO.getUsername());
            if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
            }
            existingUser.setRole(User.Role.valueOf(userRequestDTO.getRole()));
            existingUser.setSecurity(userRequestDTO.isSecurity());
            existingUser.setEmployeeId(userRequestDTO.getEmployeeId());
            return userRepository.save(existingUser);
        } else {
            throw new UserNotFoundException("User not found with id: " + userRequestDTO.getUserId());
        }
    }

    @Transactional
    public void deleteUser(Integer userId) {
        logger.info("deleteUser called with userId: {}", userId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("User found with id: {}", userId);
            try {
                // Dissociate employee from user before deleting user
                if (user.getEmployee() != null) {
                    Employee employee = user.getEmployee();
                    logger.info("Dissociating employee with empId: {} from user with id: {}", employee.getEmpId(), userId);
                    employee.setUser(null);
                    user.setEmployee(null);
                    employeeRepository.save(employee);
                }
                logger.info("Deleting dependent transactions for userId: {}", userId);
                transactionsService.deleteTransactionsByRequesterUserId(userId);
                logger.info("Deleting dependent item reports for userId: {}", userId);
                itemReportsService.deleteItemReportsByUserId(userId);
                logger.info("Deleting user with id: {}", userId);
                userRepository.deleteById(userId);
                userRepository.flush();
                logger.info("User deleted successfully with id: {}", userId);
                boolean existsAfterDelete = userRepository.existsById(userId);
                if (existsAfterDelete) {
                    logger.warn("User still exists after delete attempt for id: {}", userId);
                } else {
                    logger.info("User confirmed deleted for id: {}", userId);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while deleting user with id: {}: {}", userId, e.getMessage());
                throw e;
            }
        } else {
            logger.warn("User not found with id: {}", userId);
            throw new UserNotFoundException("User not found with id: " + userId);
        }
    }

    public User promoteToAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setRole(User.Role.ADMIN);
        return userRepository.save(user);
    }

    public User demoteFromAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    public EditUserDTO updateUserProfileById(EditUserDTO dto) {
        User existingUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        existingUser.setName(dto.getName());
        existingUser.setEmail(dto.getEmail());
        existingUser.setUsername(dto.getUsername());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        userRepository.save(existingUser);

        // Clear sensitive info before returning
        dto.setOldPassword("");
        dto.setNewPassword("");
        return dto;
    }
//    public void resetPassword(ForgotPasswordDTO forgotPasswordDTO) {
//        // Fetch user by email from the database
//        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        // Only allow password reset for users with ACCEPTED status
//        if (!"ACCEPTED".equalsIgnoreCase(user.getStatus())) {
//            throw new IllegalStateException("Password reset is allowed only for accepted users.");
//        }
//
//        // Encrypt the new password
//        String encryptedPassword = passwordEncoder.encode(forgotPasswordDTO.getNewPassword());
//        
//        // Set the new encrypted password for the user
//        user.setPassword(encryptedPassword);
//
//        // Save the updated user information to the database
//        userRepository.save(user);
//    }

	}
