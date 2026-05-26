package com.financemanager.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReportResponse(
        Map<String, BigDecimal> incomeByCategory,
        Map<String, BigDecimal> expenseByCategory,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings
) {}
