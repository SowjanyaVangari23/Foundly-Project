package com.foundly.app2.service;

import com.foundly.app2.dto.EmployeeCreationDTO;
import com.foundly.app2.dto.UserRequestDTO;
import com.foundly.app2.entity.Employee;
import com.foundly.app2.entity.User;
import com.foundly.app2.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setEmpId("E123");
        employee.setName("John Doe");
        employee.setEmpEmailId("john.doe@example.com");
    }

    @Test
    public void testGetAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("E123", employees.get(0).getEmpId());
    }

    @Test
    public void testGetTotalEmployeesCount() {
        when(employeeRepository.count()).thenReturn(5L);

        long count = employeeService.getTotalEmployeesCount();

        assertEquals(5L, count);
    }

    @Test
    public void testGetEmployeeById_Found() {
        when(employeeRepository.findById("E123")).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployeeById("E123");

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById("E123")).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById("E123");

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee created = employeeService.createEmployee(employee);

        assertNotNull(created);
        assertEquals("E123", created.getEmpId());
    }

    @Test
    public void testCreateEmployeeWithUser() {
        EmployeeCreationDTO dto = new EmployeeCreationDTO();
        dto.setEmpId("E123");
        dto.setName("John Doe");
        dto.setEmpEmailId("john.doe@example.com");

        UserRequestDTO userDTO = new UserRequestDTO();
        userDTO.setName("John");
        userDTO.setEmail("john@example.com");
        userDTO.setPassword("password");
        userDTO.setRole("USER");
        userDTO.setUsername("johndoe");
        userDTO.setSecurity(false);

        dto.setUser(userDTO);

        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArguments()[0]);

        Employee created = employeeService.createEmployeeWithUser(dto);

        assertNotNull(created);
        assertEquals("E123", created.getEmpId());
        assertNotNull(created.getUser());
        assertEquals("johndoe", created.getUser().getUsername());
    }

    @Test
    public void testUpdateEmployee_Success() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Jane Doe");
        updatedEmployee.setEmpEmailId("jane.doe@example.com");

        when(employeeRepository.findById("E123")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArguments()[0]);

        Employee updated = employeeService.updateEmployee("E123", updatedEmployee);

        assertEquals("Jane Doe", updated.getName());
        assertEquals("jane.doe@example.com", updated.getEmpEmailId());
    }

    @Test
    public void testUpdateEmployee_NotFound() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setName("Jane Doe");
        updatedEmployee.setEmpEmailId("jane.doe@example.com");

        when(employeeRepository.findById("E123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            employeeService.updateEmployee("E123", updatedEmployee);
        });

        assertEquals("Employee not found with id: E123", exception.getMessage());
    }

    @Test
    public void testDeleteEmployee_Success() {
        when(employeeRepository.findById("E123")).thenReturn(Optional.of(employee));
        User user = new User();
        user.setUserId(1);
        when(userService.getUserByEmployeeId("E123")).thenReturn(Optional.of(user));
        doNothing().when(userService).deleteUser(1);
        doNothing().when(employeeRepository).deleteById("E123");

        assertDoesNotThrow(() -> employeeService.deleteEmployee("E123"));

        verify(userService, times(1)).deleteUser(1);
        verify(employeeRepository, times(1)).deleteById("E123");
    }

    @Test
    public void testDeleteEmployee_NotFound() {
        when(employeeRepository.findById("E123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            employeeService.deleteEmployee("E123");
        });

        assertEquals("Employee not found with id: E123", exception.getMessage());
    }

    @Test
    public void testValidateEmployeeDetails_True() {
        when(employeeRepository.findByEmpIdAndNameAndEmpEmailId("E123", "John Doe", "john.doe@example.com"))
                .thenReturn(Optional.of(employee));

        boolean valid = employeeService.validateEmployeeDetails("E123", "John Doe", "john.doe@example.com");

        assertTrue(valid);
    }

    @Test
    public void testValidateEmployeeDetails_False() {
        when(employeeRepository.findByEmpIdAndNameAndEmpEmailId("E123", "John Doe", "john.doe@example.com"))
                .thenReturn(Optional.empty());

        boolean valid = employeeService.validateEmployeeDetails("E123", "John Doe", "john.doe@example.com");

        assertFalse(valid);
    }
}
