package com.financemanager.dto;

import com.financemanager.entity.CategoryType;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type,
        boolean isDefault
) {}
