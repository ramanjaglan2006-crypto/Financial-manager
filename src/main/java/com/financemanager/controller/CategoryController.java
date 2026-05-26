package com.financemanager.controller;

import com.financemanager.dto.CategoryRequest;
import com.financemanager.dto.CategoryResponse;
import com.financemanager.dto.GenericResponse;
import com.financemanager.security.CurrentUserProvider;
import com.financemanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<GenericResponse<List<CategoryResponse>>> getCategories() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<CategoryResponse> categories = categoryService.getCategories(userId);
        return ResponseEntity.ok(GenericResponse.success(categories, "Categories fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<GenericResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        CategoryResponse category = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.success(category, "Category created successfully"));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<GenericResponse<Void>> deleteCategory(@PathVariable String name) {
        Long userId = currentUserProvider.getCurrentUserId();
        categoryService.deleteCategory(userId, name);
        return ResponseEntity.ok(GenericResponse.success("Category deleted successfully"));
    }
}
