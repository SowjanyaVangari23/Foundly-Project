package com.foundly.app2.service;

import com.foundly.app2.entity.Category;
import com.foundly.app2.entity.ItemReports;
import com.foundly.app2.exception.CategoryNotFoundException;
import com.foundly.app2.exception.InvalidRequestException;
import com.foundly.app2.repository.CategoryRepository;
import com.foundly.app2.repository.ItemReportsRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ItemReportsRepository itemReportsRepository;


    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public Category createCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new InvalidRequestException("Category name cannot be null or empty");
        }
        if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new InvalidRequestException("Category with this name already exists");
        }
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Integer id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new InvalidRequestException("Category name cannot be null or empty");
        }
        
        existingCategory.setCategoryName(category.getCategoryName());
        return categoryRepository.save(existingCategory);
    }

@Transactional
public void deleteCategory(Integer categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

    // Delete all item reports related to this category
    List<ItemReports> relatedItems = itemReportsRepository.findByCategory(category);
    itemReportsRepository.deleteAll(relatedItems);

    // Now delete the category
    categoryRepository.deleteById(categoryId);
}


}
   
