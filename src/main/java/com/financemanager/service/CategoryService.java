package com.financemanager.service;

import com.financemanager.dto.CategoryRequest;
import com.financemanager.dto.CategoryResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.User;
import com.financemanager.exception.ConflictException;
import com.financemanager.exception.ForbiddenException;
import com.financemanager.exception.NotFoundException;
import com.financemanager.repository.CategoryRepository;
import com.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Cacheable(value = "categories", key = "#userId")
    public List<CategoryResponse> getCategories(Long userId) {
        return categoryRepository.findByUserIdOrIsDefaultTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public CategoryResponse createCategory(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByNameAndUserId(request.name(), userId) || 
            categoryRepository.findByNameAndIsDefaultTrue(request.name()).isPresent()) {
            throw new ConflictException("Category with this name already exists");
        }

        User user = userService.findUserEntityById(userId);

        Category category = Category.builder()
                .name(request.name())
                .type(request.type())
                .user(user)
                .isDefault(false)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    @CacheEvict(value = "categories", key = "#userId")
    public void deleteCategory(Long userId, String name) {
        Category category = categoryRepository.findByNameAndUserId(name, userId)
                .orElseThrow(() -> new NotFoundException("Custom category not found or you don't have permission to delete it"));

        if (category.isDefault()) {
            throw new ForbiddenException("Cannot delete default categories");
        }

        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new ConflictException("Cannot delete category as it is linked to existing transactions");
        }

        categoryRepository.delete(category);
    }

    public Category findCategoryByNameAndUserId(String name, Long userId) {
        return categoryRepository.findByNameAndUserId(name, userId)
                .orElseGet(() -> categoryRepository.findByNameAndIsDefaultTrue(name)
                        .orElseThrow(() -> new NotFoundException("Category not found: " + name)));
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.isDefault());
    }
}
