package com.foundly.app2.service;

import com.foundly.app2.dto.ClaimRequest;
import com.foundly.app2.dto.HandoverRequest;
import com.foundly.app2.dto.NotificationDTO;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.entity.Transactions;
import com.foundly.app2.entity.User;
import com.foundly.app2.repository.ItemReportsRepository;
import com.foundly.app2.repository.TransactionsRepository;
import com.foundly.app2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import com.foundly.app2.dto.TransactionResponse;

import java.util.*;



public class TransactionsServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private ItemReportsRepository itemReportsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.foundly.app2.repository.FoundItemDetailsRepository foundItemDetailsRepository;

    @InjectMocks
    private TransactionsService transactionsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllTransactions() {
        Transactions t1 = new Transactions();
        t1.setTransactionId(1);
        Transactions t2 = new Transactions();
        t2.setTransactionId(2);

        when(transactionsRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Transactions> transactions = transactionsService.getAllTransactions();

        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        verify(transactionsRepository, times(1)).findAll();
    }

    @Test
    public void testGetTransactionById_Found() {
        Transactions t = new Transactions();
        t.setTransactionId(1);

        when(transactionsRepository.findById(1)).thenReturn(Optional.of(t));

        Optional<Transactions> result = transactionsService.getTransactionById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getTransactionId());
        verify(transactionsRepository, times(1)).findById(1);
    }

    @Test
    public void testGetTransactionById_NotFound() {
        when(transactionsRepository.findById(1)).thenReturn(Optional.empty());

        Optional<Transactions> result = transactionsService.getTransactionById(1);

        assertFalse(result.isPresent());
        verify(transactionsRepository, times(1)).findById(1);
    }

    @Test
    public void testClaimItem() {
        ClaimRequest request = new ClaimRequest();
        request.setItemId(1);
        request.setRequesterId(1);
        request.setEmployeeId("E123");
        request.setName("Requester");
        request.setPhoto("photo.jpg");
        request.setDescription("desc");

        ItemReports item = new ItemReports();
        item.setItemId(1);
        item.setItemStatus(ItemReports.ItemStatus.WITH_SECURITY);
        User user = new User();
        item.setUser(user);

        User requester = new User();
        requester.setUserId(1);

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);

        Transactions transaction = transactionsService.claimItem(request);

        assertNotNull(transaction);
        assertEquals(Transactions.TransactionStatus.PENDING_COMPLETION, transaction.getTransactionStatus());
        verify(transactionsRepository, times(1)).save(any(Transactions.class));
    }

    @Test
    public void testClaimItem_ItemNotFound() {
        ClaimRequest request = new ClaimRequest();
        request.setItemId(1);
        request.setRequesterId(1);

        when(itemReportsRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.claimItem(request);
        });

        assertEquals("Item not found with ID: 1", exception.getMessage());
    }

    @Test
    public void testClaimItem_RequesterNotFound() {
        ClaimRequest request = new ClaimRequest();
        request.setItemId(1);
        request.setRequesterId(1);

        ItemReports item = new ItemReports();
        item.setItemId(1);
        item.setItemStatus(ItemReports.ItemStatus.WITH_SECURITY);

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.claimItem(request);
        });

        assertEquals("Requester not found with ID: 1", exception.getMessage());
    }

    @Test
    public void testClaimItem_IllegalState() {
        ClaimRequest request = new ClaimRequest();
        request.setItemId(1);
        request.setRequesterId(1);

        ItemReports item = new ItemReports();
        item.setItemId(1);
        item.setItemStatus(null); // Invalid status

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            transactionsService.claimItem(request);
        });

        assertEquals("Item is not available for claiming.", exception.getMessage());
    }

    @Test
    public void testHandoverItem() {
        HandoverRequest request = new HandoverRequest();
        request.setItemId(1);
        request.setRequesterId(1);
        request.setPhoto("photo.jpg");
        request.setDescription("desc");
        request.setHandoverToSecurity(true);
        request.setSecurityId("100");
        request.setSecurityName("SecurityName");

        ItemReports item = new ItemReports();
        item.setItemId(1);
        User user = new User();
        item.setUser(user);

        User requester = new User();
        requester.setUserId(1);
        requester.setEmployeeId("E123");
        requester.setName("Requester");

        User securityUser = new User();
        securityUser.setSecurity(true);
        securityUser.setEmployeeId("100");

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(userRepository.findByEmployeeId("100")).thenReturn(Optional.of(securityUser));
        when(itemReportsRepository.save(any(ItemReports.class))).thenReturn(item);
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);

        Transactions transaction = transactionsService.handoverItem(request);

        assertNotNull(transaction);
        assertEquals(Transactions.TransactionType.HANDOVER, transaction.getTransactionType());
        assertTrue(transaction.isHandedOverToSecurity());
        verify(transactionsRepository, times(1)).save(any(Transactions.class));
    }

    @Test
    public void testHandoverItem_ItemNotFound() {
        HandoverRequest request = new HandoverRequest();
        request.setItemId(1);
        request.setRequesterId(1);

        when(itemReportsRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.handoverItem(request);
        });

        assertEquals("Item not found with ID: 1", exception.getMessage());
    }

    @Test
    public void testHandoverItem_RequesterNotFound() {
        HandoverRequest request = new HandoverRequest();
        request.setItemId(1);
        request.setRequesterId(1);

        ItemReports item = new ItemReports();
        item.setItemId(1);

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.handoverItem(request);
        });

        assertEquals("Requester not found with ID: 1", exception.getMessage());
    }

    @Test
    public void testUpdateReporterCompletion() {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);
        transaction.setHandedOverToSecurity(true);
        transaction.setRequesterCompleted(true);
        transaction.setReporterCompleted(false);
        transaction.setTransactionStatus(Transactions.TransactionStatus.PENDING_COMPLETION);
        ItemReports item = new ItemReports();
        item.setItemStatus(ItemReports.ItemStatus.WITH_SECURITY);
        transaction.setItem(item);

        when(transactionsRepository.findById(1)).thenReturn(Optional.of(transaction));
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);
        when(itemReportsRepository.save(any(ItemReports.class))).thenReturn(item);

        Transactions updated = transactionsService.updateReporterCompletion(1);

        assertTrue(updated.isReporterCompleted());
        assertEquals(Transactions.TransactionStatus.COMPLETED, updated.getTransactionStatus());
        verify(transactionsRepository, times(1)).save(any(Transactions.class));
    }

    @Test
    public void testUpdateReporterCompletion_TransactionNotFound() {
        when(transactionsRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.updateReporterCompletion(1);
        });

        assertEquals("Transaction not found with ID: 1", exception.getMessage());
    }

    @Test
    public void testUpdateRequesterCompletion() {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);
        transaction.setHandedOverToSecurity(false);
        transaction.setRequesterCompleted(false);
        transaction.setReporterCompleted(false);
        transaction.setTransactionStatus(Transactions.TransactionStatus.PENDING_COMPLETION);
        ItemReports item = new ItemReports();
        item.setItemStatus(ItemReports.ItemStatus.WITH_FINDER);
        transaction.setItem(item);

        when(transactionsRepository.findById(1)).thenReturn(Optional.of(transaction));
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);
        when(itemReportsRepository.save(any(ItemReports.class))).thenReturn(item);

        Transactions updated = transactionsService.updateRequesterCompletion(1);

        assertTrue(updated.isRequesterCompleted());
        assertEquals(Transactions.TransactionStatus.PENDING_COMPLETION, updated.getTransactionStatus());
        verify(transactionsRepository, times(1)).save(any(Transactions.class));
    }

    @Test
    public void testUpdateRequesterCompletion_TransactionNotFound() {
        when(transactionsRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.updateRequesterCompletion(1);
        });

        assertEquals("Transaction not found with ID: 1", exception.getMessage());
    }

    @Test
    public void testGetNotificationsForUser() {
        Transactions tx1 = new Transactions();
        tx1.setTransactionId(1);
        tx1.setTransactionType(Transactions.TransactionType.CLAIM);
        tx1.setDateUpdated(LocalDateTime.now().minusHours(2));
        tx1.setDescription("desc1");
        tx1.setPhoto("photo1");
        ItemReports item1 = new ItemReports();
        item1.setItemStatus(ItemReports.ItemStatus.WITH_SECURITY);
        item1.setItemName("Item1");
        tx1.setItem(item1);

        Transactions tx2 = new Transactions();
        tx2.setTransactionId(2);
        tx2.setTransactionType(Transactions.TransactionType.HANDOVER);
        tx2.setDateUpdated(LocalDateTime.now().minusDays(1));
        tx2.setDescription("desc2");
        tx2.setPhoto("photo2");
        tx2.setPickupMessage("Pickup");
        tx2.setSecurityId("100");
        tx2.setSecurityName("SecurityName");
        ItemReports item2 = new ItemReports();
        item2.setItemStatus(ItemReports.ItemStatus.WITH_FINDER);
        item2.setItemName("Item2");
        tx2.setItem(item2);

        when(transactionsRepository.findByReporterUserId(1)).thenReturn(Arrays.asList(tx1, tx2));
        when(transactionsRepository.findByRequesterUserId(1L)).thenReturn(Arrays.asList(tx2));

        List<NotificationDTO> notifications = transactionsService.getNotificationsForUser(1);

        assertNotNull(notifications);
        assertEquals(2, notifications.size());
    }

    @Test
    public void testDeleteTransactionsByItemIds() {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);

        when(transactionsRepository.findByItem_ItemIdIn(Arrays.asList(1, 2))).thenReturn(Arrays.asList(transaction));

        transactionsService.deleteTransactionsByItemIds(Arrays.asList(1, 2));

        verify(transactionsRepository, times(1)).deleteAll(anyList());
    }

    @Test
    public void testDeleteTransactionsByItemIds_EmptyList() {
        transactionsService.deleteTransactionsByItemIds(Collections.emptyList());

        verify(transactionsRepository, never()).deleteAll(anyList());
    }

    @Test
    public void testDeleteTransactionsByItemIds_NullList() {
        transactionsService.deleteTransactionsByItemIds(null);

        verify(transactionsRepository, never()).deleteAll(anyList());
    }

    @Test
    public void testDeleteTransactionsByRequesterUserId() {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);

        when(transactionsRepository.findByRequesterUserId(1L)).thenReturn(Arrays.asList(transaction));

        transactionsService.deleteTransactionsByRequesterUserId(1);

        verify(transactionsRepository, times(1)).deleteAll(anyList());
    }

    @Test
    public void testDeleteTransactionsByRequesterUserId_NoTransactions() {
        when(transactionsRepository.findByRequesterUserId(1L)).thenReturn(Collections.emptyList());

        transactionsService.deleteTransactionsByRequesterUserId(1);

        verify(transactionsRepository, never()).deleteAll(anyList());
    }

    @Test
    public void testClaimItem_NullRequest() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            transactionsService.claimItem(null);
        });
        assertNotNull(exception);
    }

    @Test
    public void testHandoverItem_NullRequest() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            transactionsService.handoverItem(null);
        });
        assertNotNull(exception);
    }

    @Test
    public void testUpdateReporterCompletion_InvalidTransaction() {
        when(transactionsRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.updateReporterCompletion(1);
        });
        assertNotNull(exception);
    }

    @Test
    public void testUpdateRequesterCompletion_InvalidTransaction() {
        when(transactionsRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionsService.updateRequesterCompletion(1);
        });
        assertNotNull(exception);
    }

    @Test
    public void testDeleteTransactionById() {
        doNothing().when(transactionsRepository).deleteById(1);
        transactionsService.deleteTransactionById(1);
        verify(transactionsRepository, times(1)).deleteById(1);
    }

    @Test
    public void testClaimItem_WithWithFinderStatus() {
        ClaimRequest request = new ClaimRequest();
        request.setItemId(1);
        request.setRequesterId(1);
        request.setEmployeeId("E123");
        request.setName("Requester");
        request.setPhoto("photo.jpg");
        request.setDescription("desc");

        ItemReports item = new ItemReports();
        item.setItemId(1);
        item.setItemStatus(ItemReports.ItemStatus.WITH_FINDER);
        User user = new User();
        item.setUser(user);

        User requester = new User();
        requester.setUserId(1);

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);

        Transactions transaction = transactionsService.claimItem(request);

        assertNotNull(transaction);
        assertEquals(Transactions.TransactionStatus.REQUESTED, transaction.getTransactionStatus());
        assertFalse(transaction.isReporterCompleted());
    }

    @Test
    public void testHandoverItem_WithPickupMessage() {
        HandoverRequest request = new HandoverRequest();
        request.setItemId(1);
        request.setRequesterId(1);
        request.setPhoto("photo.jpg");
        request.setDescription("desc");
        request.setHandoverToSecurity(false);
        request.setPickupMessage("Pickup message");

        ItemReports item = new ItemReports();
        item.setItemId(1);
        User user = new User();
        item.setUser(user);

        User requester = new User();
        requester.setUserId(1);
        requester.setEmployeeId("E123");
        requester.setName("Requester");

        when(itemReportsRepository.findById(1)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.of(requester));
        when(itemReportsRepository.save(any(ItemReports.class))).thenReturn(item);
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);

        Transactions transaction = transactionsService.handoverItem(request);

        assertNotNull(transaction);
        assertEquals(Transactions.TransactionStatus.REQUESTED, transaction.getTransactionStatus());
        assertFalse(transaction.isHandedOverToSecurity());
        assertEquals("Pickup message", transaction.getPickupMessage());
        assertFalse(transaction.isRequesterCompleted());
    }

    @Test
    public void testMapToTransactionResponse() {
        ItemReports item = new ItemReports();
        item.setItemId(1);
        item.setItemName("ItemName");
        item.setType(ItemReports.Type.FOUND);
        // Provide a non-null category to avoid NullPointerException
        com.foundly.app2.entity.Category category = new com.foundly.app2.entity.Category();
        category.setCategoryName("CategoryName");
        item.setCategory(category);
        item.setLocation("Location");

        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);
        transaction.setItem(item);
        transaction.setTransactionType(Transactions.TransactionType.CLAIM);
        transaction.setTransactionStatus(Transactions.TransactionStatus.REQUESTED);
        transaction.setDescription("Description");
        transaction.setPhoto("Photo");
        transaction.setHandedOverToSecurity(false);
        transaction.setPickupMessage("Pickup");
        transaction.setSecurityId("SecId");
        transaction.setSecurityName("SecName");
        transaction.setRequesterCompleted(false);
        transaction.setEmployeeId("EmpId");

        TransactionResponse response = transactionsService.mapToTransactionResponse(transaction);

        assertNotNull(response);
        assertEquals(1, response.getTransactionId());
        assertEquals("ItemName", response.getItemName());
        assertEquals(Transactions.TransactionType.CLAIM, response.getTransactionType());
        assertEquals(Transactions.TransactionStatus.REQUESTED, response.getTransactionStatus());
        assertEquals("Description", response.getDescription());
        assertEquals("Photo", response.getPhoto());
        assertFalse(response.isHandedOverToSecurity());
        assertEquals("Pickup", response.getPickupMessage());
        assertEquals("SecId", response.getSecurityId());
        assertEquals("SecName", response.getSecurityName());
        assertFalse(response.isRequesterCompleted());
        assertEquals("EmpId", response.getEmployeeId());
    }

    @Test
    public void testCountByTransactionType() {
        when(transactionsRepository.countByTransactionType(Transactions.TransactionType.CLAIM)).thenReturn(5L);
        long count = transactionsService.countByTransactionType(Transactions.TransactionType.CLAIM);
        assertEquals(5L, count);
    }

    @Test
    public void testCountTransactionsGroupedByStatus() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"REQUESTED", 3L});
        mockResults.add(new Object[]{"COMPLETED", 2L});

        when(transactionsRepository.countGroupedByTransactionStatus()).thenReturn(mockResults);

        Map<String, Long> statusCounts = transactionsService.countTransactionsGroupedByStatus();

        assertEquals(2, statusCounts.size());
        assertEquals(3L, statusCounts.get("REQUESTED"));
        assertEquals(2L, statusCounts.get("COMPLETED"));
    }

    @Test
    public void testGetTotalHandoverToSecurityCount() {
        when(transactionsRepository.countByHandedOverToSecurityTrue()).thenReturn(4L);
        when(foundItemDetailsRepository.countByHandoverToSecurityTrue()).thenReturn(6L);

        long totalCount = transactionsService.getTotalHandoverToSecurityCount();

        assertEquals(10L, totalCount);
    }
}
