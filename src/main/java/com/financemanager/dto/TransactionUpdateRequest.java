package com.financemanager.dto;

import com.financemanager.entity.CategoryType;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionUpdateRequest(
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,
        
        String categoryName,
        String description,
        CategoryType type
) {}
