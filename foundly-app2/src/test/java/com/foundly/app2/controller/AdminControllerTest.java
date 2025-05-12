package com.foundly.app2.controller;

import com.foundly.app2.entity.Category;
import com.foundly.app2.entity.User;
import com.foundly.app2.service.CategoryService;
import com.foundly.app2.service.EmployeeService;
import com.foundly.app2.service.ItemReportsService;
import com.foundly.app2.service.TransactionsService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ItemReportsService itemReportsService;

    @Mock
    private TransactionsService transactionsService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(new User(), new User()));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testPromoteToAdmin() throws Exception {
        User user = new User();
        user.setUserId(1);

        when(userService.promoteToAdmin(1)).thenReturn(user);

        mockMvc.perform(post("/api/admin/users/1/promote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).promoteToAdmin(1);
    }

    @Test
    public void testDemoteFromAdmin() throws Exception {
        User user = new User();
        user.setUserId(1);

        when(userService.demoteFromAdmin(1)).thenReturn(user);

        mockMvc.perform(post("/api/admin/users/1/demote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).demoteFromAdmin(1);
    }

    @Test
    public void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    public void testCreateUserWithRole() throws Exception {
        User user = new User();
        user.setUserId(1);

        com.foundly.app2.dto.UserRequestDTO userRequestDTO = new com.foundly.app2.dto.UserRequestDTO();
        userRequestDTO.setUserId(1);
        userRequestDTO.setName("Test User");
        userRequestDTO.setEmail("test@example.com");
        userRequestDTO.setUsername("testuser");
        userRequestDTO.setPassword("password");
        userRequestDTO.setRole("USER");
        userRequestDTO.setSecurity(false);
        userRequestDTO.setEmployeeId("EMP001");

        when(userService.createUserWithRole(any(com.foundly.app2.dto.UserRequestDTO.class))).thenReturn(user);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).createUserWithRole(any(com.foundly.app2.dto.UserRequestDTO.class));
    }

    @Test
    public void testUpdateUser() throws Exception {
        User user = new User();
        user.setUserId(1);

        com.foundly.app2.dto.UserRequestDTO userRequestDTO = new com.foundly.app2.dto.UserRequestDTO();
        userRequestDTO.setUserId(1);
        userRequestDTO.setName("Updated Name");
        userRequestDTO.setEmail("updatedemail@example.com");
        userRequestDTO.setUsername("updatedusername");
        userRequestDTO.setPassword("newpassword");
        userRequestDTO.setRole("ADMIN");
        userRequestDTO.setSecurity(true);
        userRequestDTO.setEmployeeId("EMP002");

        when(userService.updateUser(any(com.foundly.app2.dto.UserRequestDTO.class))).thenReturn(user);

        mockMvc.perform(put("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService, times(1)).updateUser(any(com.foundly.app2.dto.UserRequestDTO.class));
    }

    @Test
    public void testGetAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(new Category(), new Category()));

        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    public void testCreateCategory() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1));

        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    public void testCreateCategory_InvalidInput() throws Exception {
        Category category = new Category();
        // Missing required fields or invalid data to simulate failure

        when(categoryService.createCategory(any(Category.class))).thenThrow(new IllegalArgumentException("Invalid category data"));

        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isBadRequest());

        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    public void testUpdateCategory() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.updateCategory(eq(1), any(Category.class))).thenReturn(category);

        mockMvc.perform(put("/api/admin/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1));

        verify(categoryService, times(1)).updateCategory(eq(1), any(Category.class));
    }

    @Test
    public void testUpdateCategory_NotFound() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.updateCategory(eq(1), any(Category.class))).thenThrow(new RuntimeException("Category not found with id: 1"));

        mockMvc.perform(put("/api/admin/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).updateCategory(eq(1), any(Category.class));
    }

    @Test
    public void testDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).deleteCategory(1);
    }

    @Test
    public void testDeleteCategory_NotFound() throws Exception {
        doThrow(new RuntimeException("Category not found with id: 1")).when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).deleteCategory(1);
    }

    @Test
    public void testGetAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(new com.foundly.app2.entity.Employee(), new com.foundly.app2.entity.Employee()));

        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        com.foundly.app2.entity.Employee employee = new com.foundly.app2.entity.Employee();
        employee.setEmpId("E123");
        employee.setName("John Doe");

        when(employeeService.getEmployeeById("E123")).thenReturn(java.util.Optional.of(employee));

        mockMvc.perform(get("/api/admin/employees/E123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empId").value("E123"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById("E123");
    }

    @Test
    public void testGetEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById("E123")).thenThrow(new RuntimeException("Employee not found with id: E123"));

        mockMvc.perform(get("/api/admin/employees/E123"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById("E123");
    }

    @Test
    public void testCreateEmployee() throws Exception {
        com.foundly.app2.entity.Employee employee = new com.foundly.app2.entity.Employee();
        employee.setEmpId("E123");
        employee.setName("John Doe");

        com.foundly.app2.dto.EmployeeCreationDTO dto = new com.foundly.app2.dto.EmployeeCreationDTO();
        dto.setEmpId("E123");
        dto.setName("John Doe");
        dto.setEmpEmailId("john.doe@example.com");

        when(employeeService.createEmployeeWithUser(any(com.foundly.app2.dto.EmployeeCreationDTO.class))).thenReturn(employee);

        mockMvc.perform(post("/api/admin/employees")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empId").value("E123"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).createEmployeeWithUser(any(com.foundly.app2.dto.EmployeeCreationDTO.class));
    }

    @Test
    public void testUpdateEmployee() throws Exception {
        com.foundly.app2.entity.Employee employee = new com.foundly.app2.entity.Employee();
        employee.setEmpId("E123");
        employee.setName("John Doe");

        when(employeeService.updateEmployee(eq("E123"), any(com.foundly.app2.entity.Employee.class))).thenReturn(employee);

        mockMvc.perform(put("/api/admin/employees/E123")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empId").value("E123"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).updateEmployee(eq("E123"), any(com.foundly.app2.entity.Employee.class));
    }

    @Test
    public void testDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee("E123");

        mockMvc.perform(delete("/api/admin/employees/E123"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).deleteEmployee("E123");
    }

    @Test
    public void testGetDashboardSummary() throws Exception {
        com.foundly.app2.dto.DashboardSummaryDTO summary = new com.foundly.app2.dto.DashboardSummaryDTO(10, 5, 20, 15);

        when(userService.getTotalUsersCount()).thenReturn(10L);
        when(employeeService.getTotalEmployeesCount()).thenReturn(5L);
        when(itemReportsService.getTotalItemReportsCount()).thenReturn(20L);
        when(transactionsService.getAllTransactions()).thenReturn(java.util.Collections.nCopies(15, new com.foundly.app2.entity.Transactions()));

        mockMvc.perform(get("/api/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalEmployees").value(5))
                .andExpect(jsonPath("$.totalItemReports").value(20))
                .andExpect(jsonPath("$.totalTransactions").value(15));

        verify(userService, times(1)).getTotalUsersCount();
        verify(employeeService, times(1)).getTotalEmployeesCount();
        verify(itemReportsService, times(1)).getTotalItemReportsCount();
        verify(transactionsService, times(1)).getAllTransactions();
    }
}
