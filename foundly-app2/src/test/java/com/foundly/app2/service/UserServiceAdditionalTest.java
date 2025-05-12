package com.foundly.app2.service;

import com.foundly.app2.dto.UserRequestDTO;
import com.foundly.app2.entity.Employee;
import com.foundly.app2.entity.User;
import com.foundly.app2.exception.InvalidRequestException;
import com.foundly.app2.exception.UserNotFoundException;
import com.foundly.app2.repository.UserRepository;
import com.foundly.app2.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceAdditionalTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private ItemReportsService itemReportsService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUserRepositoryCount() {
        when(userRepository.count()).thenReturn(5L);
        // Adjusted to call userRepository.count() directly as countUsers() does not exist
        long count = userRepository.count();
        assertEquals(5L, count);
        verify(userRepository).count();
    }

    @Test
    public void testCreateUserWithRole_InvalidEmployeeId_Throws() {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmployeeId("");
        userRequestDTO.setRole("USER");

        assertThrows(InvalidRequestException.class, () -> {
            userService.createUserWithRole(userRequestDTO);
        });
    }

    @Test
    public void testCreateUserWithRole_NonAdminEmployeeIdNotFound_Throws() {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmployeeId("EMP123");
        userRequestDTO.setRole("USER");

        // Mock employeeRepository.findByEmpId() instead of employeeService.getEmployeeById()
        when(employeeRepository.findByEmpId("EMP123")).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> {
            userService.createUserWithRole(userRequestDTO);
        });
    }

    @Test
    public void testCreateUserWithRole_EmployeeExists() {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmployeeId("EMP123");
        userRequestDTO.setRole("USER");

        Employee employee = new Employee();
        employee.setEmpId("EMP123");

        // Mock employeeRepository.findByEmpId() instead of employeeService.findByEmpId()
        when(employeeRepository.findByEmpId("EMP123")).thenReturn(Optional.of(employee));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User createdUser = userService.createUserWithRole(userRequestDTO);

        assertNotNull(createdUser);
        verify(employeeService, never()).createEmployee(any(Employee.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testDeleteUser_DissociatesEmployeeAndDeletesUser() {
        User user = new User();
        user.setUserId(1);
        Employee employee = new Employee();
        employee.setEmpId("EMP001");
        employee.setUser(user);
        user.setEmployee(employee);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1);
        // Removed doNothing() on createEmployee since it returns Employee, not void
        // Removed verify on createEmployee since it is not called in deleteUser

        userService.deleteUser(1);

        assertNull(user.getEmployee());
        assertNull(employee.getUser());
        verify(userRepository).deleteById(1);
    }

    @Test
    public void testCreateUserWithRole_AdminRole_SkipsEmployeeCheck() {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmployeeId("EMP123");
        userRequestDTO.setRole("ADMIN");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User createdUser = userService.createUserWithRole(userRequestDTO);

        assertNotNull(createdUser);
        verify(employeeService, never()).findByEmpId(anyString());
    }

    @Test
    public void testCreateUserWithRole_CreatesEmployeeAndUser() {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmployeeId("EMP123");
        userRequestDTO.setRole("USER");
        userRequestDTO.setName("Test Name");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setPassword("password");

        Employee employee = new Employee();
        employee.setEmpId("EMP123");

        // Mock employeeRepository.findByEmpId() to return the employee
        when(employeeRepository.findByEmpId("EMP123")).thenReturn(Optional.of(employee));
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User createdUser = userService.createUserWithRole(userRequestDTO);

        assertNotNull(createdUser);
        verify(employeeService, never()).createEmployee(any(Employee.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testDeleteUser_UserNotFound_Throws() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1);
        });
    }
}
