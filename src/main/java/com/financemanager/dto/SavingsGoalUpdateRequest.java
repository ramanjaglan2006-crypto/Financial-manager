package com.financemanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoalUpdateRequest(
        String goalName,
        
        @Positive(message = "Target amount must be positive")
        BigDecimal targetAmount,
        
        @Future(message = "Target date must be in the future")
        LocalDate targetDate
) {}
