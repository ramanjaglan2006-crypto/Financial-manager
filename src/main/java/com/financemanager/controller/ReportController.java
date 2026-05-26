package com.financemanager.controller;

import com.financemanager.dto.GenericResponse;
import com.financemanager.dto.ReportResponse;
import com.financemanager.security.CurrentUserProvider;
import com.financemanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<GenericResponse<ReportResponse>> getMonthlyReport(
            @PathVariable int year, @PathVariable int month) {
        Long userId = currentUserProvider.getCurrentUserId();
        ReportResponse report = reportService.getMonthlyReport(userId, year, month);
        return ResponseEntity.ok(GenericResponse.success(report, "Monthly report generated successfully"));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<GenericResponse<ReportResponse>> getYearlyReport(@PathVariable int year) {
        Long userId = currentUserProvider.getCurrentUserId();
        ReportResponse report = reportService.getYearlyReport(userId, year);
        return ResponseEntity.ok(GenericResponse.success(report, "Yearly report generated successfully"));
    }
}
