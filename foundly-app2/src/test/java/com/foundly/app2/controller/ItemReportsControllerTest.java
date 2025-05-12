package com.foundly.app2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foundly.app2.dto.FoundItemReportRequest;
import com.foundly.app2.dto.ItemReportResponse;
import com.foundly.app2.dto.LostItemReportRequest;
import com.foundly.app2.dto.CategoryCountDTO;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.service.ItemReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ItemReportsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemReportsService itemReportsService;

    @InjectMocks
    private ItemReportsController itemReportsController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemReportsController).build();
    }

    @Test
    public void testGetHandoverToSecurityCount() throws Exception {
        long count = 5L;
        when(itemReportsService.getHandoverToSecurityCount()).thenReturn(count);

        mockMvc.perform(get("/api/items/handover-to-security-count"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(count)));

        verify(itemReportsService, times(1)).getHandoverToSecurityCount();
    }

    @Test
    public void testGetCategoryCounts() throws Exception {
        List<CategoryCountDTO> categoryCounts = Arrays.asList(new CategoryCountDTO("Category1", 10L));
        when(itemReportsService.getCategoryCounts()).thenReturn(categoryCounts);

        mockMvc.perform(get("/api/items/category-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("Category1"))
                .andExpect(jsonPath("$[0].count").value(10));

        verify(itemReportsService, times(1)).getCategoryCounts();
    }

    @Test
    public void testDeleteItemReport() throws Exception {
        doNothing().when(itemReportsService).deleteItemReportById(1);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());

        verify(itemReportsService, times(1)).deleteItemReportById(1);
    }

    @Test
    public void testGetAllItemReports() throws Exception {
        ItemReports report1 = new ItemReports();
        report1.setItemId(1);
        ItemReports report2 = new ItemReports();
        report2.setItemId(2);

        when(itemReportsService.getAllItemReports()).thenReturn(Arrays.asList(report1, report2));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(itemReportsService, times(1)).getAllItemReports();
    }

    @Test
    public void testGetAllLostItems() throws Exception {
        when(itemReportsService.getItemsByType(ItemReports.Type.LOST)).thenReturn(Arrays.asList(new ItemReports()));

        mockMvc.perform(get("/api/items/lost-items"))
                .andExpect(status().isOk());

        verify(itemReportsService, times(1)).getItemsByType(ItemReports.Type.LOST);
    }

    @Test
    public void testGetAllFoundItems() throws Exception {
        when(itemReportsService.getItemsByType(ItemReports.Type.FOUND)).thenReturn(Arrays.asList(new ItemReports()));

        mockMvc.perform(get("/api/items/found-items"))
                .andExpect(status().isOk());

        verify(itemReportsService, times(1)).getItemsByType(ItemReports.Type.FOUND);
    }

    @Test
    public void testGetItemReportById_Found() throws Exception {
        ItemReports report = new ItemReports();
        report.setItemId(1);

        when(itemReportsService.getItemReportById(1)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(1));

        verify(itemReportsService, times(1)).getItemReportById(1);
    }

    @Test
    public void testGetItemReportById_NotFound() throws Exception {
        when(itemReportsService.getItemReportById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());

        verify(itemReportsService, times(1)).getItemReportById(1);
    }

    @Test
    public void testReportFoundItem_Success() throws Exception {
        FoundItemReportRequest request = new FoundItemReportRequest();
        ItemReports createdReport = new ItemReports();
        createdReport.setItemId(1);

        when(itemReportsService.reportFoundItem(any(FoundItemReportRequest.class))).thenReturn(createdReport);

        mockMvc.perform(post("/api/items/found")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(1));

        verify(itemReportsService, times(1)).reportFoundItem(any(FoundItemReportRequest.class));
    }

    @Test
    public void testReportFoundItem_BadRequest() throws Exception {
        FoundItemReportRequest request = new FoundItemReportRequest();

        when(itemReportsService.reportFoundItem(any(FoundItemReportRequest.class))).thenThrow(new IllegalArgumentException());

        mockMvc.perform(post("/api/items/found")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemReportsService, times(1)).reportFoundItem(any(FoundItemReportRequest.class));
    }

    @Test
    public void testReportLostItem_Success() throws Exception {
        LostItemReportRequest request = new LostItemReportRequest();
        ItemReports createdReport = new ItemReports();
        createdReport.setItemId(1);

        when(itemReportsService.reportLostItem(any(LostItemReportRequest.class))).thenReturn(createdReport);

        mockMvc.perform(post("/api/items/lost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(1));

        verify(itemReportsService, times(1)).reportLostItem(any(LostItemReportRequest.class));
    }

    @Test
    public void testReportLostItem_BadRequest() throws Exception {
        LostItemReportRequest request = new LostItemReportRequest();
        ItemReports createdReport = new ItemReports();
        createdReport.setItemId(1);

        when(itemReportsService.reportLostItem(any(LostItemReportRequest.class))).thenReturn(createdReport);

        mockMvc.perform(post("/api/items/lost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(1));

        verify(itemReportsService, times(1)).reportLostItem(any(LostItemReportRequest.class));
    }

    @Test
    public void testReportFoundItem_Unauthorized() throws Exception {
        FoundItemReportRequest request = new FoundItemReportRequest();

        when(itemReportsService.reportFoundItem(any(FoundItemReportRequest.class)))
                .thenThrow(new SecurityException("Unauthorized access"));

        mockMvc.perform(post("/api/items/found")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(itemReportsService, times(1)).reportFoundItem(any(FoundItemReportRequest.class));
    }

    @Test
    public void testGetItemReportById_ServerError() throws Exception {
        when(itemReportsService.getItemReportById(1)).thenThrow(new RuntimeException("Server error"));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isInternalServerError());

        verify(itemReportsService, times(1)).getItemReportById(1);
    }

    @Test
    public void testGetLostReportsByUserId() throws Exception {
        List<ItemReportResponse> responses = Arrays.asList(new ItemReportResponse());

        when(itemReportsService.getLostReportsByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/items/lost/user/1"))
                .andExpect(status().isOk());

        verify(itemReportsService, times(1)).getLostReportsByUserId(1L);
    }

    @Test
    public void testGetFoundReportsByUserId() throws Exception {
        List<ItemReportResponse> responses = Arrays.asList(new ItemReportResponse());

        when(itemReportsService.getFoundReportsByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/items/found/user/1"))
                .andExpect(status().isOk());

        verify(itemReportsService, times(1)).getFoundReportsByUserId(1L);
    }
}