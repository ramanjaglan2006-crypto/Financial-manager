package com.financemanager.controller;

import com.financemanager.dto.GenericResponse;
import com.financemanager.dto.TransactionRequest;
import com.financemanager.dto.TransactionResponse;
import com.financemanager.dto.TransactionUpdateRequest;
import com.financemanager.entity.CategoryType;
import com.financemanager.security.CurrentUserProvider;
import com.financemanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<GenericResponse<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        TransactionResponse transaction = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.success(transaction, "Transaction created successfully"));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) CategoryType type) {
        Long userId = currentUserProvider.getCurrentUserId();
        List<TransactionResponse> transactions = transactionService.getTransactions(userId, startDate, endDate, categoryName, type);
        return ResponseEntity.ok(GenericResponse.success(transactions, "Transactions fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id, @Valid @RequestBody TransactionUpdateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        TransactionResponse transaction = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(GenericResponse.success(transaction, "Transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteTransaction(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(GenericResponse.success("Transaction deleted successfully"));
    }
}
