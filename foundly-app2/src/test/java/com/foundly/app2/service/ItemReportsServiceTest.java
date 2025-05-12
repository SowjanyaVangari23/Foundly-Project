package com.foundly.app2.service;

import com.foundly.app2.dto.FoundItemReportRequest;
import com.foundly.app2.dto.LostItemReportRequest;

import com.foundly.app2.dto.CategoryCountDTO;
import com.foundly.app2.entity.Category;
import com.foundly.app2.entity.FoundItemDetails;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.entity.User;
import com.foundly.app2.repository.CategoryRepository;
import com.foundly.app2.repository.FoundItemDetailsRepository;
import com.foundly.app2.repository.ItemReportsRepository;
import com.foundly.app2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemReportsServiceTest {

    @Mock
    private ItemReportsRepository itemReportsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FoundItemDetailsRepository foundItemDetailsRepository;

    @Mock
    private TransactionsService transactionsService;

    @InjectMocks
    private ItemReportsService itemReportsService;

    private User user;
    private Category category;
    private ItemReports itemReport;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);

        category = new Category();
        category.setCategoryId(1);

        itemReport = new ItemReports();
        itemReport.setItemId(1);
        itemReport.setUser(user);
        itemReport.setCategory(category);
        itemReport.setItemName("Test Item");
        itemReport.setType(ItemReports.Type.FOUND);
    }

    @Test
    public void testReportFoundItem_Success() {
        FoundItemReportRequest request = new FoundItemReportRequest();
        request.setItemName("Item1");
        request.setDescription("Description1");
        request.setLocation("Location1");
        request.setImageUrl("image.jpg");
        request.setDateLostOrFound("2025-04-15 16:21");
        request.setUserId(1);
        request.setCategoryId(1);
        request.setName("Reporter");
        request.setHandoverToSecurity(true);
        request.setSecurityId("100");
        request.setSecurityName("SecurityName");
        request.setPickupMessage("Pickup message");

        User securityUser = new User();
        securityUser.setSecurity(true);
        securityUser.setEmployeeId("100");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findByEmployeeId("100")).thenReturn(Optional.of(securityUser));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(itemReportsRepository.save(any(ItemReports.class))).thenAnswer(i -> i.getArguments()[0]);
        when(foundItemDetailsRepository.save(any(FoundItemDetails.class))).thenReturn(null);

        ItemReports saved = itemReportsService.reportFoundItem(request);

        assertNotNull(saved);
        assertEquals("Item1", saved.getItemName());
        assertEquals(ItemReports.Type.FOUND, saved.getType());
    }

    @Test
    public void testReportFoundItem_MissingSecurityId_Throws() {
        FoundItemReportRequest request = new FoundItemReportRequest();
        request.setHandoverToSecurity(true);
        request.setSecurityId(null);
        request.setCategoryId(1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            itemReportsService.reportFoundItem(request);
        });

        assertEquals("Security ID must be provided for handover.", exception.getMessage());
    }

    @Test
    public void testReportFoundItem_InvalidSecurityId_Throws() {
        FoundItemReportRequest request = new FoundItemReportRequest();
        request.setHandoverToSecurity(true);
        request.setSecurityId("invalid");
        request.setCategoryId(1);

        when(userRepository.findByEmployeeId("invalid")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            itemReportsService.reportFoundItem(request);
        });

        assertEquals("Provided security ID does not belong to a valid security personnel.", exception.getMessage());
    }

    @Test
    public void testReportFoundItem_MissingCategoryId_Throws() {
        FoundItemReportRequest request = new FoundItemReportRequest();
        request.setHandoverToSecurity(false);
        request.setCategoryId(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            itemReportsService.reportFoundItem(request);
        });

        assertEquals("Category ID must be provided.", exception.getMessage());
    }

    @Test
    public void testReportFoundItem_CategoryNotFound_Throws() {
        FoundItemReportRequest request = new FoundItemReportRequest();
        request.setHandoverToSecurity(false);
        request.setCategoryId(999);

        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            itemReportsService.reportFoundItem(request);
        });

        assertEquals("Category not found for ID: 999", exception.getMessage());
    }

    @Test
    public void testReportLostItem_Success() {
        LostItemReportRequest request = new LostItemReportRequest();
        request.setItemName("LostItem1");
        request.setDescription("Lost Description");
        request.setLocation("Lost Location");
        request.setImageUrl("lostimage.jpg");
        request.setDateLostOrFound("2025-04-15 16:21");
        request.setUserId(1);
        request.setCategoryId(1);
        request.setName("Lost Reporter");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(itemReportsRepository.save(any(ItemReports.class))).thenAnswer(i -> i.getArguments()[0]);

        ItemReports lostItem = itemReportsService.reportLostItem(request);

        assertNotNull(lostItem);
        assertEquals("LostItem1", lostItem.getItemName());
        assertEquals(ItemReports.Type.LOST, lostItem.getType());
    }

    @Test
    public void testReportLostItem_MissingDateLostOrFound_Throws() {
        LostItemReportRequest request = new LostItemReportRequest();
        request.setDateLostOrFound(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            itemReportsService.reportLostItem(request);
        });

        assertEquals("Date lost or found must be provided.", exception.getMessage());
    }

    @Test
    public void testGetTotalItemReportsCount() {
        when(itemReportsRepository.count()).thenReturn(5L);

        long count = itemReportsService.getTotalItemReportsCount();

        assertEquals(5L, count);
    }

    @Test
    public void testGetCategoryCounts() {
        List<CategoryCountDTO> counts = new ArrayList<>();
        // Create a CategoryCountDTO with dummy data using constructor with parameters
        CategoryCountDTO dto = new CategoryCountDTO("TestCategory", 10L);
        counts.add(dto);
        when(itemReportsRepository.getCategoryCounts()).thenReturn(counts);

        List<CategoryCountDTO> result = itemReportsService.getCategoryCounts();

        assertEquals(1, result.size());
        // Use Lombok-generated getter methods
        assertEquals("TestCategory", result.get(0).getCategory());
        assertEquals(10L, result.get(0).getCount());
    }
}
