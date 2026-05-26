package com.financemanager.dto;

import com.financemanager.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,
        
        @NotNull(message = "Type is required")
        CategoryType type
) {}
