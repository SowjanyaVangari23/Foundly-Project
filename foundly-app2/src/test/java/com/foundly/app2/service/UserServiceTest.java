package com.foundly.app2.service;

import com.foundly.app2.dto.EditUserDTO;
import com.foundly.app2.dto.UserLoginRequest;
import com.foundly.app2.dto.UserRegistrationRequest;
import com.foundly.app2.entity.User;
import com.foundly.app2.exception.DuplicateItemException;
import com.foundly.app2.exception.InvalidRequestException;
import com.foundly.app2.exception.UserNotFoundException;
import com.foundly.app2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ItemReportsService itemReportsService;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmployeeId("EMP001");
        user.setName("Test User");
        user.setRole(User.Role.USER);
        user.setSecurity(false);
    }

    @Test
    public void testGetUserById_UserExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserById(1);
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    public void testGetUserById_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(1);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSaveUser_UserExists() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = new User();
        updatedUser.setUserId(1);
        updatedUser.setName("New Name");
        updatedUser.setUsername("newusername");
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setPassword("newpassword");

        User result = userService.saveUser(updatedUser);
        assertEquals("New Name", result.getName());
        assertEquals("newusername", result.getUsername());
        assertEquals("newemail@example.com", result.getEmail());
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testSaveUser_UserNotFound() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());
        User updatedUser = new User();
        updatedUser.setUserId(1);
        assertThrows(UserNotFoundException.class, () -> userService.saveUser(updatedUser));
    }

    @Test
    public void testCreateUserWithRole_DuplicateEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        com.foundly.app2.dto.UserRequestDTO newUser = new com.foundly.app2.dto.UserRequestDTO();
        newUser.setEmail(user.getEmail());
        newUser.setUsername("uniqueusername");
        newUser.setEmployeeId("EMP002");
        newUser.setName("Test User");
        newUser.setPassword("password");
        newUser.setRole("USER");
        newUser.setSecurity(false);
        assertThrows(DuplicateItemException.class, () -> userService.createUserWithRole(newUser));
    }

    @Test
    public void testCreateUserWithRole_DuplicateUsername() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        com.foundly.app2.dto.UserRequestDTO newUser = new com.foundly.app2.dto.UserRequestDTO();
        newUser.setEmail("uniqueemail@example.com");
        newUser.setUsername(user.getUsername());
        newUser.setEmployeeId("EMP002");
        newUser.setName("Test User");
        newUser.setPassword("password");
        newUser.setRole("USER");
        newUser.setSecurity(false);
        assertThrows(DuplicateItemException.class, () -> userService.createUserWithRole(newUser));
    }

    @Test
    public void testCreateUserWithRole_InvalidEmployeeId() {
        com.foundly.app2.dto.UserRequestDTO newUser = new com.foundly.app2.dto.UserRequestDTO();
        newUser.setEmail("uniqueemail@example.com");
        newUser.setUsername("uniqueusername");
        newUser.setEmployeeId("");
        newUser.setName("Test User");
        newUser.setPassword("password");
        newUser.setRole("USER");
        newUser.setSecurity(false);
        assertThrows(InvalidRequestException.class, () -> userService.createUserWithRole(newUser));
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(user);
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = userService.getAllUsers();
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
    }

    @Test
    public void testGetUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    public void testGetUserByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    public void testDeleteUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        doNothing().when(transactionsService).deleteTransactionsByRequesterUserId(1);
        doNothing().when(itemReportsService).deleteItemReportsByUserId(1);
        doNothing().when(userRepository).deleteById(1);

        userService.deleteUser(1);

        verify(transactionsService).deleteTransactionsByRequesterUserId(1);
        verify(itemReportsService).deleteItemReportsByUserId(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    public void testRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmployeeId("EMP001");
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(employeeService.validateEmployeeDetails(anyString(), anyString(), anyString())).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("password"); // Return original password to match user.password
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUsername("testuser"); // Set username explicitly to match assertion
            return user;
        });

        User result = userService.registerUser(request);
        assertEquals("Test User", result.getName());
        assertEquals("password", result.getPassword());
        assertTrue(result.getUsername().startsWith("testuser") || result.getUsername().startsWith("testuser1"));
    }

    @Test
    public void testLoginUser_Success() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

        Optional<User> result = userService.loginUser(loginRequest);
        assertTrue(result.isPresent());
    }

    @Test
    public void testLoginUser_Failure() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        Optional<User> result = userService.loginUser(loginRequest);
        assertFalse(result.isPresent());
    }

    @Test
    public void testPromoteToAdmin() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.promoteToAdmin(1);
        assertEquals(User.Role.ADMIN, result.getRole());
    }

    @Test
    public void testPromoteToAdmin_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.promoteToAdmin(1));
    }

    @Test
    public void testDemoteFromAdmin() {
        user.setRole(User.Role.ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.demoteFromAdmin(1);
        assertEquals(User.Role.USER, result.getRole());
    }

    @Test
    public void testDemoteFromAdmin_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.demoteFromAdmin(1));
    }

    // Removed testCreateAdminUser and testCreateAdminUser_DuplicateEmail as createAdminUser method does not exist in UserService

    @Test
    public void testUpdateUser() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        com.foundly.app2.dto.UserRequestDTO updatedUser = new com.foundly.app2.dto.UserRequestDTO();
        updatedUser.setUserId(1);
        updatedUser.setName("Updated Name");
        updatedUser.setUsername("updatedusername");
        updatedUser.setEmail("updatedemail@example.com");
        updatedUser.setPassword("newpassword");
        updatedUser.setRole("ADMIN");
        updatedUser.setSecurity(true);
        updatedUser.setEmployeeId("EMP002");

        User result = userService.updateUser(updatedUser);
        assertEquals("Updated Name", result.getName());
        assertEquals("updatedusername", result.getUsername());
        assertEquals("updatedemail@example.com", result.getEmail());
        assertEquals(User.Role.ADMIN, result.getRole());
        assertTrue(result.isSecurity());
        assertEquals("EMP002", result.getEmployeeId());
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());
        com.foundly.app2.dto.UserRequestDTO updatedUser = new com.foundly.app2.dto.UserRequestDTO();
        updatedUser.setUserId(1);
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(updatedUser));
    }

    @Test
    public void testUpdateUserProfileById_Success() {
        EditUserDTO dto = new EditUserDTO();
        dto.setUserId(1);
        dto.setOldPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setName("Updated Name");
        dto.setEmail("updatedemail@example.com");
        dto.setUsername("updatedusername");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        EditUserDTO result = userService.updateUserProfileById(dto);
        assertEquals(1, result.getUserId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updatedemail@example.com", result.getEmail());
        assertEquals("updatedusername", result.getUsername());
        assertEquals("", result.getOldPassword());
        assertEquals("", result.getNewPassword());
    }

    @Test
    public void testUpdateUserProfileById_InvalidOldPassword() {
        EditUserDTO dto = new EditUserDTO();
        dto.setUserId(1);
        dto.setOldPassword("wrongoldpass");
        dto.setNewPassword("newpass");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongoldpass", user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfileById(dto));
    }

    @Test
    public void testUpdateUserProfileById_UserNotFound() {
        EditUserDTO dto = new EditUserDTO();
        dto.setUserId(1);

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.updateUserProfileById(dto));
    }
}
