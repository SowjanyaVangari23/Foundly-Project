

package com.foundly.app2.controller;

import com.foundly.app2.entity.Category;
import com.foundly.app2.entity.Employee;
import com.foundly.app2.entity.User;
import com.foundly.app2.service.CategoryService;
import com.foundly.app2.service.EmployeeService;
import com.foundly.app2.service.ItemReportsService;
import com.foundly.app2.service.TransactionsService;
import com.foundly.app2.service.UserService;
import com.foundly.app2.dto.DashboardSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ItemReportsService itemReportsService;

    @Autowired
    private TransactionsService transactionsService;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummaryDTO getDashboardSummary() {
        long totalUsers = userService.getTotalUsersCount();
        long totalEmployees = employeeService.getTotalEmployeesCount();
        long totalItemReports = itemReportsService.getTotalItemReportsCount();
        long totalTransactions = transactionsService.getAllTransactions().size();

        return new DashboardSummaryDTO(totalUsers, totalEmployees, totalItemReports, totalTransactions);
    }

    @PostMapping("/users/{userId}/promote")
    public User promoteToAdmin(@PathVariable Integer userId) {
        return userService.promoteToAdmin(userId);
    }

    @PostMapping("/users/{userId}/demote")
    public User demoteFromAdmin(@PathVariable Integer userId) {
        return userService.demoteFromAdmin(userId);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }

    @PostMapping("/users")
    public User createUserWithRole(@RequestBody com.foundly.app2.dto.UserRequestDTO userRequestDTO) {
        return userService.createUserWithRole(userRequestDTO);
    }

    @PutMapping("/users/{userId}")
    public User updateUser(@PathVariable Integer userId, @RequestBody com.foundly.app2.dto.UserRequestDTO userRequestDTO) {
        userRequestDTO.setUserId(userId);
        return userService.updateUser(userRequestDTO);
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.ok(createdCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Employee CRUD endpoints
    @GetMapping("/employees")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/employees/{empId}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String empId) {
        try {
            Employee employee = employeeService.getEmployeeById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + empId));
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/employees")
    public Employee createEmployee(@RequestBody com.foundly.app2.dto.EmployeeCreationDTO employeeCreationDTO) {
        return employeeService.createEmployeeWithUser(employeeCreationDTO);
    }

    @PutMapping("/employees/{empId}")
    public Employee updateEmployee(@PathVariable String empId, @RequestBody Employee employee) {
        return employeeService.updateEmployee(empId, employee);
    }

    @DeleteMapping("/employees/{empId}")
    public void deleteEmployee(@PathVariable String empId) {
        employeeService.deleteEmployee(empId);
    }
}
