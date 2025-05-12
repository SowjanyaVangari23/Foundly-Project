package com.foundly.app2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foundly.app2.dto.ClaimRequest;
import com.foundly.app2.dto.HandoverRequest;
import com.foundly.app2.dto.NotificationDTO;
import com.foundly.app2.dto.TransactionResponse;
import com.foundly.app2.dto.TransactionResponseDTO;
import com.foundly.app2.entity.Transactions;
import com.foundly.app2.service.TransactionsService;
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
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransactionsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionsService transactionsService;

    @InjectMocks
    private TransactionsController transactionsController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionsController).build();
    }

    @Test
    public void testGetAllTransactions() throws Exception {
        Transactions transaction1 = new Transactions();
        transaction1.setTransactionId(1);
        Transactions transaction2 = new Transactions();
        transaction2.setTransactionId(2);

        when(transactionsService.getAllTransactions()).thenReturn(Arrays.asList(transaction1, transaction2));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(transactionsService, times(1)).getAllTransactions();
    }

    @Test
    public void testGetTransactionById_Found() throws Exception {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);

        when(transactionsService.getTransactionById(1)).thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));

        verify(transactionsService, times(1)).getTransactionById(1);
    }

    @Test
    public void testGetTransactionById_NotFound() throws Exception {
        when(transactionsService.getTransactionById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isNotFound());

        verify(transactionsService, times(1)).getTransactionById(1);
    }

    @Test
    public void testClaimItem() throws Exception {
        ClaimRequest request = new ClaimRequest();
        Transactions createdTransaction = new Transactions();
        createdTransaction.setTransactionId(1);

        when(transactionsService.claimItem(any(ClaimRequest.class))).thenReturn(createdTransaction);

        mockMvc.perform(post("/api/transactions/claim")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(1));

        verify(transactionsService, times(1)).claimItem(any(ClaimRequest.class));
    }

    @Test
    public void testHandoverItem() throws Exception {
        HandoverRequest request = new HandoverRequest();
        Transactions createdTransaction = new Transactions();
        createdTransaction.setTransactionId(1);
        createdTransaction.setRequesterName("Requester");
        createdTransaction.setDescription("Description");
        createdTransaction.setTransactionStatus(Transactions.TransactionStatus.COMPLETED);
        createdTransaction.setTransactionType(Transactions.TransactionType.HANDOVER);
        createdTransaction.setDateUpdated(java.time.LocalDateTime.now());
        createdTransaction.setItem(new com.foundly.app2.entity.ItemReports());
        createdTransaction.getItem().setItemName("ItemName");

        TransactionResponseDTO dto = new TransactionResponseDTO(
                createdTransaction.getTransactionId(),
                createdTransaction.getItem().getItemName(),
                createdTransaction.getRequesterName(),
                createdTransaction.getDescription(),
                createdTransaction.getTransactionStatus().name(),
                createdTransaction.getTransactionType().name(),
                createdTransaction.getDateUpdated()
        );

        when(transactionsService.handoverItem(any(HandoverRequest.class))).thenReturn(createdTransaction);

        mockMvc.perform(post("/api/transactions/handover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(dto.getTransactionId()));

        verify(transactionsService, times(1)).handoverItem(any(HandoverRequest.class));
    }

    @Test
    public void testGetClaimsByUserId() throws Exception {
        List<TransactionResponse> responses = Arrays.asList(new TransactionResponse());

        when(transactionsService.getClaimsByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/transactions/claims/1"))
                .andExpect(status().isOk());

        verify(transactionsService, times(1)).getClaimsByUserId(1L);
    }

    @Test
    public void testGetHandoversByUserId() throws Exception {
        List<TransactionResponse> responses = Arrays.asList(new TransactionResponse());

        when(transactionsService.getHandoversByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/transactions/handovers/1"))
                .andExpect(status().isOk());

        verify(transactionsService, times(1)).getHandoversByUserId(1L);
    }

    @Test
    public void testReporterCompleted() throws Exception {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);

        when(transactionsService.updateReporterCompletion(1)).thenReturn(transaction);

        mockMvc.perform(put("/api/transactions/reporter-completed/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));

        verify(transactionsService, times(1)).updateReporterCompletion(1);
    }

    @Test
    public void testRequesterCompleted() throws Exception {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(1);

        when(transactionsService.updateRequesterCompletion(1)).thenReturn(transaction);

        mockMvc.perform(put("/api/transactions/requester-completed/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));

        verify(transactionsService, times(1)).updateRequesterCompletion(1);
    }

    @Test
    public void testGetUserNotifications() throws Exception {
        List<NotificationDTO> notifications = Arrays.asList(new NotificationDTO());

        when(transactionsService.getNotificationsForUser(1)).thenReturn(notifications);

        mockMvc.perform(get("/api/transactions/notifications/1"))
                .andExpect(status().isOk());

        verify(transactionsService, times(1)).getNotificationsForUser(1);
    }

    @Test
    public void testGetTotalClaimsCount() throws Exception {
        long count = 10L;
        when(transactionsService.countByTransactionType(Transactions.TransactionType.CLAIM)).thenReturn(count);

        mockMvc.perform(get("/api/transactions/count/claims"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(count)));

        verify(transactionsService, times(1)).countByTransactionType(Transactions.TransactionType.CLAIM);
    }

    @Test
    public void testGetTotalHandoversCount() throws Exception {
        long count = 15L;
        when(transactionsService.countByTransactionType(Transactions.TransactionType.HANDOVER)).thenReturn(count);

        mockMvc.perform(get("/api/transactions/count/handovers"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(count)));

        verify(transactionsService, times(1)).countByTransactionType(Transactions.TransactionType.HANDOVER);
    }

    @Test
    public void testGetTransactionCountByStatus() throws Exception {
        Map<String, Long> statusCounts = Map.of("COMPLETED", 5L, "PENDING", 3L);
        when(transactionsService.countTransactionsGroupedByStatus()).thenReturn(statusCounts);

        mockMvc.perform(get("/api/transactions/count/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.COMPLETED").value(5))
                .andExpect(jsonPath("$.PENDING").value(3));

        verify(transactionsService, times(1)).countTransactionsGroupedByStatus();
    }

    @Test
    public void testGetTotalHandoverToSecurityCount() throws Exception {
        long count = 7L;
        when(transactionsService.getTotalHandoverToSecurityCount()).thenReturn(count);

        mockMvc.perform(get("/api/transactions/count/handover-to-security"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(count)));

        verify(transactionsService, times(1)).getTotalHandoverToSecurityCount();
    }

    @Test
    public void testDeleteTransaction() throws Exception {
        doNothing().when(transactionsService).deleteTransactionById(1);

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());

        verify(transactionsService, times(1)).deleteTransactionById(1);
    }
}