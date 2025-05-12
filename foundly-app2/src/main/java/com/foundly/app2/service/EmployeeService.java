package com.foundly.app2.service;

import com.foundly.app2.entity.Employee;
import com.foundly.app2.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    @Lazy
    private UserService userService;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public long getTotalEmployeesCount() {
        return employeeRepository.count();
    }

    public Optional<Employee> getEmployeeById(String empId) {
        return employeeRepository.findById(empId);
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee createEmployeeWithUser(com.foundly.app2.dto.EmployeeCreationDTO employeeCreationDTO) {
        Employee employee = new Employee();
        employee.setEmpId(employeeCreationDTO.getEmpId());
        employee.setName(employeeCreationDTO.getName());
        employee.setEmpEmailId(employeeCreationDTO.getEmpEmailId());

        com.foundly.app2.dto.UserRequestDTO userDTO = employeeCreationDTO.getUser();
        if (userDTO != null) {
            com.foundly.app2.entity.User user = new com.foundly.app2.entity.User();
            user.setEmployeeId(employee.getEmpId());
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());
            user.setRole(userDTO.getRole() != null ? com.foundly.app2.entity.User.Role.valueOf(userDTO.getRole()) : com.foundly.app2.entity.User.Role.USER);
            user.setUsername(userDTO.getUsername());
            user.setSecurity(userDTO.isSecurity());

            employee.setUser(user);
            user.setEmployee(employee);
        }

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(String empId, Employee updatedEmployee) {
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(empId);
        if (existingEmployeeOpt.isPresent()) {
            Employee existingEmployee = existingEmployeeOpt.get();
            existingEmployee.setName(updatedEmployee.getName());
            existingEmployee.setEmpEmailId(updatedEmployee.getEmpEmailId());
            return employeeRepository.save(existingEmployee);
        } else {
            throw new RuntimeException("Employee not found with id: " + empId);
        }
    }

    @Transactional
    public void deleteEmployee(String empId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(empId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            String employeeId = employee.getEmpId();
            userService.getUserByEmployeeId(employeeId).ifPresent(user -> {
                try {
                    userService.deleteUser(user.getUserId());
                } catch (Exception e) {
                    System.err.println("Failed to delete user with employeeId " + employeeId + ": " + e.getMessage());
                }
            });
            employeeRepository.deleteById(empId);
        } else {
            throw new RuntimeException("Employee not found with id: " + empId);
        }
    }

    public boolean validateEmployeeDetails(String empId, String name, String empEmailId) {
        return employeeRepository.findByEmpIdAndNameAndEmpEmailId(empId, name, empEmailId).isPresent();
    }

    public Optional<Employee> findByEmpId(String empId) {
        return employeeRepository.findByEmpId(empId);
    }
}
