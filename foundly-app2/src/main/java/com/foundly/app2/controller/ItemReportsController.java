package com.foundly.app2.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.foundly.app2.dto.CategoryCountDTO;
import com.foundly.app2.dto.FoundItemReportRequest;
import com.foundly.app2.dto.ItemReportResponse;
import com.foundly.app2.dto.LostItemReportRequest;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.service.ItemReportsService;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/items")
public class ItemReportsController {

    @Autowired
    private ItemReportsService itemReportsService;

    // Get all item reports
    @GetMapping
    public ResponseEntity<List<ItemReportResponse>> getAllItemReports() {
        List<ItemReports> itemReports = itemReportsService.getAllItemReports();
        List<ItemReportResponse> responseList = itemReports.stream()
                .map(ItemReportResponse::fromEntity)
                .toList();
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @GetMapping("/lost-items")
    public List<ItemReports> getAllLostItems() {
        return itemReportsService.getItemsByType(ItemReports.Type.LOST);
    }

   @GetMapping("/found-items")
public ResponseEntity<List<ItemReportResponse>> getAllFoundItems() {
    List<ItemReports> foundItems = itemReportsService.getItemsByType(ItemReports.Type.FOUND);
    List<ItemReportResponse> responseList = foundItems.stream()
            .map(ItemReportResponse::fromEntity)
            .collect(Collectors.toList());
    return new ResponseEntity<>(responseList, HttpStatus.OK);
}
    // Get an item report by ID
    @GetMapping("/{id}")
    public ResponseEntity<ItemReports> getItemReportById(@PathVariable Integer id) {
        return itemReportsService.getItemReportById(id)
                .map(itemReport -> new ResponseEntity<>(itemReport, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Report a found item
    @PostMapping("/found")
    public ResponseEntity<ItemReports> reportFoundItem(@RequestBody FoundItemReportRequest request) {
        try {
            ItemReports createdItemReport = itemReportsService.reportFoundItem(request);
            return new ResponseEntity<>(createdItemReport, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Report a lost item
    @PostMapping("/lost")
    public ResponseEntity<ItemReports> reportLostItem(@RequestBody LostItemReportRequest request) {
        try {
            ItemReports createdItemReport = itemReportsService.reportLostItem(request);
            return new ResponseEntity<>(createdItemReport, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get lost reports by user ID
    @GetMapping("/lost/user/{userId}")
    public ResponseEntity<List<ItemReportResponse>> getLostReportsByUserId(@PathVariable Long userId) {
        List<ItemReportResponse> reports = itemReportsService.getLostReportsByUserId(userId);
        return ResponseEntity.ok(reports);
    }

    // Get found reports by user ID
    @GetMapping("/found/user/{userId}")
    public ResponseEntity<List<ItemReportResponse>> getFoundReportsByUserId(@PathVariable Long userId) {
        List<ItemReportResponse> reports = itemReportsService.getFoundReportsByUserId(userId);
        return ResponseEntity.ok(reports);
    }

    // Delete an item report by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemReport(@PathVariable Integer id) {
        itemReportsService.deleteItemReportById(id);
        return ResponseEntity.noContent().build();
    }

    // Get the count of items handed over to security
    @GetMapping("/handover-to-security-count")
    public ResponseEntity<Long> getHandoverToSecurityCount() {
        long count = itemReportsService.getHandoverToSecurityCount();
        return ResponseEntity.ok(count);
    }
//    @GetMapping("/category-counts")
//    public ResponseEntity<List<CategoryCountDTO>> getCategoryCounts() {
//        List<CategoryCountDTO> categoryCounts = itemReportsService.getCategoryCounts();
//        return ResponseEntity.ok(categoryCounts);
//    }
    @GetMapping("/category-counts")
    public ResponseEntity<List<CategoryCountDTO>> getCategoryCounts() {
        return ResponseEntity.ok(itemReportsService.getCategoryCounts());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>("Server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
