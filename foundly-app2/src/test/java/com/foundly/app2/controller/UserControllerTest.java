package com.foundly.app2.controller;

import com.foundly.app2.dto.EditUserDTO;
import com.foundly.app2.dto.UserLoginRequest;
import com.foundly.app2.dto.UserRegistrationRequest;
import com.foundly.app2.entity.User;
import com.foundly.app2.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(new User(), new User()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testGetUserById_Found() throws Exception {
        User user = new User();
        user.setUserId(1);

        when(userService.getUserById(1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    public void testGetUserByEmail_Found() throws Exception {
        User user = new User();
        user.setUserId(1);

        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    @Test
    public void testGetUserByEmail_NotFound() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    @Test
    public void testRegisterUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        User createdUser = new User();
        createdUser.setUserId(1);

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    public void testLoginUser_Success() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        User user = new User();
        user.setUserId(1);

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    public void testLoginUser_Unauthorized() throws Exception {
        UserLoginRequest request = new UserLoginRequest();

        when(userService.loginUser(any(UserLoginRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).loginUser(any(UserLoginRequest.class));
    }

    @Test
    public void testUpdateUser_Found() throws Exception {
        com.foundly.app2.dto.UserRequestDTO userRequestDTO = new com.foundly.app2.dto.UserRequestDTO();
        userRequestDTO.setUserId(1);
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setName("Test User");
        userRequestDTO.setEmployeeId("EMP001");
        userRequestDTO.setRole("USER");
        userRequestDTO.setSecurity(false);

        User updatedUser = new User();
        updatedUser.setUserId(1);
        updatedUser.setEmail("test@example.com");
        updatedUser.setUsername("testuser");
        updatedUser.setName("Test User");
        updatedUser.setEmployeeId("EMP001");
        updatedUser.setRole(User.Role.USER);
        updatedUser.setSecurity(false);

        when(userService.getUserById(1)).thenReturn(Optional.of(updatedUser));
        when(userService.updateUser(any(com.foundly.app2.dto.UserRequestDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).getUserById(1);
        verify(userService, times(1)).updateUser(any(com.foundly.app2.dto.UserRequestDTO.class));
    }

    @Test
    public void testUpdateUser_NotFound() throws Exception {
        User user = new User();

        when(userService.getUserById(1)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    public void testDeleteUser_Found() throws Exception {
        User user = new User();
        user.setUserId(1);

        when(userService.getUserById(1)).thenReturn(Optional.of(user));
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).getUserById(1);
        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    public void testDeleteUser_NotFound() throws Exception {
        when(userService.getUserById(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    public void testGetEditableUserProfile_Found() throws Exception {
        User user = new User();
        user.setUserId(1);
        user.setEmployeeId("E123");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(userService.getUserById(1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/profile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.employeeId").value("E123"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    public void testGetEditableUserProfile_NotFound() throws Exception {
        when(userService.getUserById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/profile/1"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(1);
    }

    @Test
    public void testUpdateUserProfile_Success() throws Exception {
        EditUserDTO dto = new EditUserDTO();
        dto.setUserId(1);

        when(userService.updateUserProfileById(any(EditUserDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/users/profile/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUserProfileById(any(EditUserDTO.class));
    }

    @Test
    public void testUpdateUserProfile_NotFound() throws Exception {
        EditUserDTO dto = new EditUserDTO();
        dto.setUserId(1);

        when(userService.updateUserProfileById(any(EditUserDTO.class))).thenThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/users/profile/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUserProfileById(any(EditUserDTO.class));
    }

    @Test
    public void testUpdateUserProfile_BadRequest() throws Exception {
        EditUserDTO dto = new EditUserDTO();
        dto.setUserId(1);

        when(userService.updateUserProfileById(any(EditUserDTO.class))).thenThrow(new IllegalArgumentException("Invalid"));

        mockMvc.perform(put("/api/users/profile/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUserProfileById(any(EditUserDTO.class));
    }
}