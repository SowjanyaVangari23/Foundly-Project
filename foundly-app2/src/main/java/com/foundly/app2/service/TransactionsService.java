package com.foundly.app2.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foundly.app2.dto.ClaimRequest;
import com.foundly.app2.dto.HandoverRequest;
import com.foundly.app2.dto.NotificationDTO;
import com.foundly.app2.dto.TransactionResponse;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.entity.Transactions;
import com.foundly.app2.entity.User;
import com.foundly.app2.repository.FoundItemDetailsRepository;
import com.foundly.app2.repository.ItemReportsRepository;
import com.foundly.app2.repository.TransactionsRepository;
import com.foundly.app2.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TransactionsService {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private ItemReportsRepository itemReportsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoundItemDetailsRepository foundItemDetailsRepository;

    public List<Transactions> getAllTransactions() {
        return transactionsRepository.findAll();
    }

    public Optional<Transactions> getTransactionById(Integer transactionId) {
        return transactionsRepository.findById(transactionId);
    }

    @Transactional
    public void deleteTransactionById(Integer transactionId) {
        transactionsRepository.deleteById(transactionId);
    }

    @Transactional
    public Transactions claimItem(ClaimRequest request) {
        ItemReports item = itemReportsRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + request.getItemId()));

        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + request.getRequesterId()));

        Transactions transaction = new Transactions();
        transaction.setItem(item);
        transaction.setRequester(requester);
        transaction.setReporter(item.getUser());
        transaction.setEmployeeId(request.getEmployeeId());
        transaction.setRequesterName(request.getName());
        transaction.setPhoto(request.getPhoto());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionType(Transactions.TransactionType.CLAIM);
        transaction.setDateUpdated(LocalDateTime.now());

        if (item.getItemStatus() == ItemReports.ItemStatus.WITH_SECURITY) {
            transaction.setTransactionStatus(Transactions.TransactionStatus.PENDING_COMPLETION);
            transaction.setReporterCompleted(true);
        } else if (item.getItemStatus() == ItemReports.ItemStatus.WITH_FINDER) {
            transaction.setTransactionStatus(Transactions.TransactionStatus.REQUESTED);
            transaction.setReporterCompleted(false);
        } else {
            throw new IllegalStateException("Item is not available for claiming.");
        }

        transaction.setRequesterCompleted(false);

        return transactionsRepository.save(transaction);
    }

    @Transactional
    public Transactions handoverItem(HandoverRequest request) {
        ItemReports item = itemReportsRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + request.getItemId()));

        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + request.getRequesterId()));

        Transactions transaction = new Transactions();
        transaction.setItem(item);
        transaction.setRequester(requester);
        transaction.setReporter(item.getUser());
        transaction.setEmployeeId(requester.getEmployeeId());
        transaction.setRequesterName(requester.getName());
        transaction.setPhoto(request.getPhoto());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionType(Transactions.TransactionType.HANDOVER);
        transaction.setDateUpdated(LocalDateTime.now());

        if (request.isHandoverToSecurity()) {
            // 🔐 Security verification
            if (request.getSecurityId() == null || request.getSecurityId().trim().isEmpty()) {
                throw new IllegalArgumentException("Security ID must be provided for handover.");
            }

            Optional<User> securityUser = userRepository.findByEmployeeId(request.getSecurityId());
            if (securityUser.isEmpty() || !securityUser.get().isSecurity()) {
                throw new IllegalArgumentException("Provided Security ID does not belong to a valid security personnel.");
            }

            item.setItemStatus(ItemReports.ItemStatus.WITH_SECURITY);
            transaction.setTransactionStatus(Transactions.TransactionStatus.PENDING_COMPLETION);
            transaction.setHandedOverToSecurity(true);
            transaction.setRequesterCompleted(true);
            transaction.setSecurityId(request.getSecurityId());
            transaction.setSecurityName(request.getSecurityName());
        } else {
            item.setItemStatus(ItemReports.ItemStatus.WITH_FINDER);
            transaction.setTransactionStatus(Transactions.TransactionStatus.REQUESTED);
            transaction.setHandedOverToSecurity(false);
            transaction.setPickupMessage(request.getPickupMessage());
            transaction.setRequesterCompleted(false);
        }

        transaction.setReporterCompleted(false);

        itemReportsRepository.save(item);
        return transactionsRepository.save(transaction);
    }


    @Transactional
    public Transactions updateRequesterCompletion(Integer transactionId) {
        Transactions transaction = transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        transaction.setRequesterCompleted(true);
        transaction.setDateUpdated(LocalDateTime.now());

        checkAndMarkCompletion(transaction);

        itemReportsRepository.save(transaction.getItem());
        return transactionsRepository.save(transaction);
    }

    @Transactional
    public Transactions updateReporterCompletion(Integer transactionId) {
        Transactions transaction = transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        transaction.setReporterCompleted(true);
        transaction.setDateUpdated(LocalDateTime.now());

        checkAndMarkCompletion(transaction);

        itemReportsRepository.save(transaction.getItem());
        return transactionsRepository.save(transaction);
    }

    private void checkAndMarkCompletion(Transactions transaction) {
        boolean handedToSecurity = transaction.isHandedOverToSecurity();

        if (handedToSecurity) {
            if (transaction.isRequesterCompleted()) {
                transaction.setTransactionStatus(Transactions.TransactionStatus.COMPLETED);
                transaction.getItem().setItemStatus(ItemReports.ItemStatus.RECEIVED);
            } else {
                transaction.setTransactionStatus(Transactions.TransactionStatus.PENDING_COMPLETION);
            }
        } else {
            if (transaction.isRequesterCompleted() && transaction.isReporterCompleted()) {
                transaction.setTransactionStatus(Transactions.TransactionStatus.COMPLETED);
                transaction.getItem().setItemStatus(ItemReports.ItemStatus.RECEIVED);
            } else {
                transaction.setTransactionStatus(Transactions.TransactionStatus.PENDING_COMPLETION);
            }
        }
    }

    public List<NotificationDTO> getNotificationsForUser(Integer userId) {
        List<Transactions> reporterTransactions = transactionsRepository.findByReporterUserId(userId);
        List<NotificationDTO> notifications = new ArrayList<>();

        for (Transactions tx : reporterTransactions) {
            NotificationDTO dto = new NotificationDTO();
            dto.setTransactionId(tx.getTransactionId());
            dto.setType(tx.getTransactionType().toString());
            dto.setTime(formatTimeAgo(tx.getDateUpdated()));
            dto.setDescription(tx.getDescription());
            dto.setPhoto(tx.getPhoto());
            dto.setItemStatus(tx.getItem().getItemStatus().toString());
            dto.setReporterCompleted(tx.isReporterCompleted());
            dto.setRequesterCompleted(tx.isRequesterCompleted());
            dto.setSecurityId(tx.getSecurityId());
            dto.setSecurityName(tx.getSecurityName());

            if (tx.getTransactionType() == Transactions.TransactionType.CLAIM) {
                dto.setTitle("Claim Request: " + tx.getItem().getItemName());
                dto.setMessage("A user has claimed the item you reported. Review and confirm.");
            } else if (tx.getTransactionType() == Transactions.TransactionType.HANDOVER) {
                dto.setTitle("Found Item: " + tx.getItem().getItemName());
                dto.setMessage("Someone found your item and wants to hand it over.");
                dto.setPickupMessage(tx.getPickupMessage());
            }

            dto.setTransactionStatus(tx.getTransactionStatus().name());

            notifications.add(dto);
        }

        return notifications;
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long hours = duration.toHours();

        if (hours < 24) {
            return hours + "h ago";
        } else {
            return (hours / 24) + "d ago";
        }
    }

    public List<TransactionResponse> getClaimsByUserId(Long userId) {
        List<Transactions> transactions = transactionsRepository.findByRequesterUserIdAndTransactionType(userId, Transactions.TransactionType.CLAIM);
        return transactions.stream().map(this::mapToTransactionResponse).collect(Collectors.toList());
    }

    public List<TransactionResponse> getHandoversByUserId(Long userId) {
        List<Transactions> transactions = transactionsRepository.findByRequesterUserIdAndTransactionType(userId, Transactions.TransactionType.HANDOVER);
        return transactions.stream().map(this::mapToTransactionResponse).collect(Collectors.toList());
    }

    public TransactionResponse mapToTransactionResponse(Transactions transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getItem().getItemId(),
                transaction.getItem().getItemName(),
                transaction.getItem().getType().toString(),
                transaction.getItem().getCategory().getCategoryName(),
                transaction.getItem().getLocation(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getDescription(),
                transaction.getPhoto(),
                transaction.isHandedOverToSecurity(),
                transaction.getPickupMessage(),
                transaction.getSecurityId(),
                transaction.getSecurityName(),
                transaction.isRequesterCompleted(),
                transaction.getEmployeeId()
        );
    }

    public long countByTransactionType(Transactions.TransactionType type) {
        return transactionsRepository.countByTransactionType(type);
    }

    public Map<String, Long> countTransactionsGroupedByStatus() {
        List<Object[]> results = transactionsRepository.countGroupedByTransactionStatus();
        Map<String, Long> statusCounts = new HashMap<>();

        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }

        return statusCounts;
    }

    @Transactional
    public void deleteTransactionsByItemIds(List<Integer> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) return;
        List<Transactions> transactions = transactionsRepository.findByItem_ItemIdIn(itemIds);
        if (transactions != null && !transactions.isEmpty()) {
            transactionsRepository.deleteAll(transactions);
        }
    }

    @Transactional
    public void deleteTransactionsByRequesterUserId(Integer userId) {
        List<Transactions> transactions = transactionsRepository.findByRequesterUserId(userId.longValue());
        if (transactions != null && !transactions.isEmpty()) {
            transactionsRepository.deleteAll(transactions);
        }
    }

    // ✅ NEW METHOD: Total handovers to security
    public long getTotalHandoverToSecurityCount() {
        long fromTransactions = transactionsRepository.countByHandedOverToSecurityTrue();
        long fromFoundItemDetails = foundItemDetailsRepository.countByHandoverToSecurityTrue();
        return fromTransactions + fromFoundItemDetails;
    }
}
