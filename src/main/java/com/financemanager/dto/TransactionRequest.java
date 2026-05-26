package com.financemanager.dto;

import com.financemanager.entity.CategoryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,
        
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,
        
        @NotNull(message = "Category name is required")
        String categoryName,
        
        String description,
        
        @NotNull(message = "Type is required")
        CategoryType type
) {}
