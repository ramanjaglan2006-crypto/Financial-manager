package com.financemanager.dto;

import com.financemanager.entity.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String categoryName,
        String description,
        CategoryType type
) {}
