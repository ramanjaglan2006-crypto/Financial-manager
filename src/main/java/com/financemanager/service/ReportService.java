package com.financemanager.service;

import com.financemanager.dto.ReportResponse;
import com.financemanager.entity.CategoryType;
import com.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    @Cacheable(value = "reports", key = "#userId + '-' + #year + '-' + #month")
    public ReportResponse getMonthlyReport(Long userId, int year, int month) {
        List<Object[]> incomeAgg = transactionRepository.aggregateMonthlyByCategory(userId, year, month, CategoryType.INCOME);
        List<Object[]> expenseAgg = transactionRepository.aggregateMonthlyByCategory(userId, year, month, CategoryType.EXPENSE);
        
        return buildReportResponse(incomeAgg, expenseAgg);
    }

    @Cacheable(value = "reports", key = "#userId + '-' + #year")
    public ReportResponse getYearlyReport(Long userId, int year) {
        List<Object[]> incomeAgg = transactionRepository.aggregateYearlyByCategory(userId, year, CategoryType.INCOME);
        List<Object[]> expenseAgg = transactionRepository.aggregateYearlyByCategory(userId, year, CategoryType.EXPENSE);

        return buildReportResponse(incomeAgg, expenseAgg);
    }

    private ReportResponse buildReportResponse(List<Object[]> incomeAgg, List<Object[]> expenseAgg) {
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Object[] row : incomeAgg) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            incomeByCategory.put(category, amount);
            totalIncome = totalIncome.add(amount);
        }

        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Object[] row : expenseAgg) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            expenseByCategory.put(category, amount);
            totalExpense = totalExpense.add(amount);
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        return new ReportResponse(incomeByCategory, expenseByCategory, totalIncome, totalExpense, netSavings);
    }
}
