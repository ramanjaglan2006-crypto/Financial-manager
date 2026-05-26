package com.financemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoalResponse(
        Long id,
        String goalName,
        BigDecimal targetAmount,
        LocalDate targetDate,
        LocalDate startDate,
        BigDecimal currentProgress,
        BigDecimal remainingAmount,
        double progressPercentage
) {}
