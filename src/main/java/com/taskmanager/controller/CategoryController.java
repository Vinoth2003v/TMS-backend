package com.taskmanager.controller;

import com.taskmanager.entity.Category;
import com.taskmanager.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equalsIgnoreCase("ROLE_ADMIN")
        );
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "You do not have permission to manage categories.",
                "message", "You do not have permission to manage categories."
            ));
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category name is required"));
        }
        if (categoryRepository.existsByName(category.getName().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category already exists"));
        }
        category.setName(category.getName().trim());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "You do not have permission to manage categories.",
                "message", "You do not have permission to manage categories."
            ));
        }
        return categoryRepository.findById(id).map(category -> {
            if (categoryDetails.getName() != null && !categoryDetails.getName().trim().isEmpty()) {
                category.setName(categoryDetails.getName().trim());
            }
            if (categoryDetails.getDescription() != null) {
                category.setDescription(categoryDetails.getDescription().trim());
            }
            return ResponseEntity.ok(categoryRepository.save(category));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "You do not have permission to manage categories.",
                "message", "You do not have permission to manage categories."
            ));
        }
        return categoryRepository.findById(id).map(category -> {
            categoryRepository.delete(category);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }
}
