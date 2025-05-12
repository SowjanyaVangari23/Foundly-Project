package com.foundly.app2.controller;

import com.foundly.app2.entity.Category;
import com.foundly.app2.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    public void testGetAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(new Category(), new Category()));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    public void testGetCategoryById_Found() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1));

        verify(categoryService, times(1)).getCategoryById(1);
    }

    @Test
    public void testGetCategoryById_NotFound() throws Exception {
        when(categoryService.getCategoryById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryById(1);
    }

    @Test
    public void testCreateCategory() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1));

        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    public void testUpdateCategory_Found() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(category));
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1));

        verify(categoryService, times(1)).getCategoryById(1);
        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    public void testUpdateCategory_NotFound() throws Exception {
        when(categoryService.getCategoryById(1)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1}"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryById(1);
    }

    @Test
    public void testDeleteCategory_Found() throws Exception {
        Category category = new Category();
        category.setCategoryId(1);

        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(category));
        doNothing().when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).getCategoryById(1);
        verify(categoryService, times(1)).deleteCategory(1);
    }

    @Test
    public void testDeleteCategory_NotFound() throws Exception {
        when(categoryService.getCategoryById(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryById(1);
    }
}