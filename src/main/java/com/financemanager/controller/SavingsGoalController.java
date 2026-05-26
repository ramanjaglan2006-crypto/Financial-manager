package com.financemanager.controller;

import com.financemanager.dto.GenericResponse;
import com.financemanager.dto.SavingsGoalRequest;
import com.financemanager.dto.SavingsGoalResponse;
import com.financemanager.dto.SavingsGoalUpdateRequest;
import com.financemanager.security.CurrentUserProvider;
import com.financemanager.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<GenericResponse<SavingsGoalResponse>> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        SavingsGoalResponse goal = savingsGoalService.createGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.success(goal, "Savings goal created successfully"));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<SavingsGoalResponse>>> getGoals() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<SavingsGoalResponse> goals = savingsGoalService.getGoals(userId);
        return ResponseEntity.ok(GenericResponse.success(goals, "Savings goals fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<SavingsGoalResponse>> getGoalById(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        SavingsGoalResponse goal = savingsGoalService.getGoalById(userId, id);
        return ResponseEntity.ok(GenericResponse.success(goal, "Savings goal fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<SavingsGoalResponse>> updateGoal(
            @PathVariable Long id, @Valid @RequestBody SavingsGoalUpdateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        SavingsGoalResponse goal = savingsGoalService.updateGoal(userId, id, request);
        return ResponseEntity.ok(GenericResponse.success(goal, "Savings goal updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteGoal(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        savingsGoalService.deleteGoal(userId, id);
        return ResponseEntity.ok(GenericResponse.success("Savings goal deleted successfully"));
    }
}
