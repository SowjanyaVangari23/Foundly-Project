package com.foundly.app2.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foundly.app2.dto.CategoryCountDTO;
import com.foundly.app2.dto.FoundItemReportRequest;
import com.foundly.app2.dto.ItemReportResponse;
import com.foundly.app2.dto.LostItemPreviewDTO;
import com.foundly.app2.dto.LostItemReportRequest;
import com.foundly.app2.entity.Category;
import com.foundly.app2.entity.FoundItemDetails;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.entity.User;
import com.foundly.app2.repository.CategoryRepository;
import com.foundly.app2.repository.FoundItemDetailsRepository;
import com.foundly.app2.repository.ItemReportsRepository;
import com.foundly.app2.repository.UserRepository;

@Service
public class ItemReportsService {

    @Autowired
    private ItemReportsRepository itemReportsRepository;
    
    @Autowired
    private TransactionsService transactionsService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FoundItemDetailsRepository foundItemDetailsRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Get all item reports
    public List<ItemReports> getAllItemReports() {
        return itemReportsRepository.findAll();
    }

    public long getTotalItemReportsCount() {
        return itemReportsRepository.count();
    }

    // Get an item report by ID
    public Optional<ItemReports> getItemReportById(Integer itemId) {
        return itemReportsRepository.findById(itemId);
    }

    @Transactional
    public void deleteItemReportById(Integer itemId) {
        // Delete dependent transactions first
        transactionsService.deleteTransactionsByItemIds(java.util.Collections.singletonList(itemId));
        // Delete the item report
        itemReportsRepository.deleteById(itemId);
    }

    // Report a found item
    public ItemReports reportFoundItem(FoundItemReportRequest request) {
        ItemReports foundItem = new ItemReports();
        foundItem.setItemName(request.getItemName());
        foundItem.setDescription(request.getDescription());
        foundItem.setLocation(request.getLocation());
        foundItem.setImageUrl(request.getImageUrl());
        foundItem.setDateReported(LocalDateTime.now());
        foundItem.setType(ItemReports.Type.FOUND);
        foundItem.setIsRequested(false);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            foundItem.setName(request.getName().trim());
        }

        if (request.getHandoverToSecurity() != null && request.getHandoverToSecurity()) {
            // 🔐 Validate Security ID
            if (request.getSecurityId() == null || request.getSecurityId().trim().isEmpty()) {
                throw new IllegalArgumentException("Security ID must be provided for handover.");
            }

            Optional<User> securityUser = userRepository.findByEmployeeId(request.getSecurityId());
            if (securityUser.isEmpty() || !securityUser.get().isSecurity()) {
                throw new IllegalArgumentException("Provided security ID does not belong to a valid security personnel.");
            }

            foundItem.setItemStatus(ItemReports.ItemStatus.WITH_SECURITY);
        } else {
            foundItem.setItemStatus(ItemReports.ItemStatus.WITH_FINDER);
        }

        // Set User
        if (request.getUserId() != null) {
            Optional<User> userOptional = userRepository.findById(request.getUserId());
            userOptional.ifPresent(foundItem::setUser);
        }

        // Set Category
        if (request.getCategoryId() != null) {
            Optional<Category> categoryOptional = categoryRepository.findById(request.getCategoryId());
            if (categoryOptional.isPresent()) {
                foundItem.setCategory(categoryOptional.get());
            } else {
                throw new RuntimeException("Category not found for ID: " + request.getCategoryId());
            }
        } else {
            throw new RuntimeException("Category ID must be provided.");
        }

        // Set dateLostOrFound if applicable
        if (request.getDateLostOrFound() != null) {
            foundItem.setDateLostOrFound(LocalDateTime.parse(request.getDateLostOrFound(), formatter));
        }

        // Save the found item report
        ItemReports savedItem = itemReportsRepository.save(foundItem);

        // Save associated FoundItemDetails
        FoundItemDetails details = new FoundItemDetails();
        details.setItem(savedItem);
        details.setSecurityId(request.getSecurityId());
        details.setSecurityName(request.getSecurityName());
        details.setPickupMessage(request.getPickupMessage());
        details.setHandoverToSecurity(request.getHandoverToSecurity());

        foundItemDetailsRepository.save(details);

        return savedItem;
    }


    // Report a lost item
    public ItemReports reportLostItem(LostItemReportRequest request) {
        if (request.getDateLostOrFound() == null) {
            throw new IllegalArgumentException("Date lost or found must be provided.");
        }

        ItemReports lostItem = new ItemReports();
        lostItem.setItemName(request.getItemName());
        lostItem.setDescription(request.getDescription());
        lostItem.setLocation(request.getLocation());
        lostItem.setImageUrl(request.getImageUrl());
        lostItem.setDateReported(LocalDateTime.now());
        lostItem.setType(ItemReports.Type.LOST);
        lostItem.setItemStatus(ItemReports.ItemStatus.NOT_FOUND);
        lostItem.setIsRequested(false);

        // Set optional reporter name if present
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            lostItem.setName(request.getName().trim());
        }

        // Set User
        if (request.getUserId() != null) {
            userRepository.findById(request.getUserId()).ifPresent(lostItem::setUser);
        }

        // Set Category
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(lostItem::setCategory);
        }

        // Parse and set the dateLostOrFound
        lostItem.setDateLostOrFound(LocalDateTime.parse(request.getDateLostOrFound(), formatter));

        return itemReportsRepository.save(lostItem);
    }

    // Get items by type (Lost or Found)
    public List<ItemReports> getItemsByType(ItemReports.Type type) {
        return itemReportsRepository.findByType(type);
    }

    // Get recent lost items preview
    public List<LostItemPreviewDTO> getRecentLostItemsPreview(int limit) {
        Pageable pageable = PageRequest.of(0, limit); // ✅ Pageable created correctly
        return itemReportsRepository
                .findByTypeOrderByDateReportedDesc(ItemReports.Type.LOST, pageable)
                .stream()
                .map(item -> new LostItemPreviewDTO(
                        item.getItemName(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getCategory() != null ? item.getCategory().getCategoryName() : "Uncategorized",
                        item.getDateReported(),
                        item.getDateLostOrFound(),
                        item.getImageUrl(),
                        item.getItemStatus()
                ))
                .collect(Collectors.toList());
    }

    // Get lost reports by user ID
    public List<ItemReportResponse> getLostReportsByUserId(Long userId) {
        List<ItemReports> reports = itemReportsRepository.findByUser_UserIdAndType(userId, ItemReports.Type.LOST);
        return reports.stream()
                      .map(ItemReportResponse::fromEntity)
                      .collect(Collectors.toList());
    }

    // Get found reports by user ID
    public List<ItemReportResponse> getFoundReportsByUserId(Long userId) {
        List<ItemReports> reports = itemReportsRepository.findByUser_UserIdAndType(userId, ItemReports.Type.FOUND);
        return reports.stream()
                      .map(ItemReportResponse::fromEntity)
                      .collect(Collectors.toList());
    }

    // Count the number of handovers to security (handoverToSecurity = true)
    public long getHandoverToSecurityCount() {
        return foundItemDetailsRepository.countByHandoverToSecurityTrue();
    }
   
    @Transactional
    public void deleteItemReportsByUserId(Integer userId) {
        List<ItemReports> reports = itemReportsRepository.findByUserId(userId.longValue());
        if (reports != null && !reports.isEmpty()) {
            // Extract item IDs
            List<Integer> itemIds = reports.stream()
                                           .map(ItemReports::getItemId)
                                           .collect(java.util.stream.Collectors.toList());
            // Delete dependent transactions first
            transactionsService.deleteTransactionsByItemIds(itemIds);
            // Delete item reports
            itemReportsRepository.deleteAll(reports);
        }
    }
    public List<CategoryCountDTO> getCategoryCounts() {
        return itemReportsRepository.getCategoryCounts();
    }


}
