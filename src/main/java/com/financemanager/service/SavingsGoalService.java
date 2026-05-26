package com.financemanager.service;

import com.financemanager.dto.SavingsGoalRequest;
import com.financemanager.dto.SavingsGoalResponse;
import com.financemanager.dto.SavingsGoalUpdateRequest;
import com.financemanager.entity.SavingsGoal;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.NotFoundException;
import com.financemanager.repository.SavingsGoalRepository;
import com.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Cacheable(value = "goals", key = "#userId")
    public List<SavingsGoalResponse> getGoals(Long userId) {
        return savingsGoalRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SavingsGoalResponse getGoalById(Long userId, Long goalId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NotFoundException("Savings Goal not found"));
        return mapToResponse(goal);
    }

    @Transactional
    @CacheEvict(value = "goals", key = "#userId")
    public SavingsGoalResponse createGoal(Long userId, SavingsGoalRequest request) {
        if (request.targetDate().isBefore(request.startDate())) {
            throw new BadRequestException("Target date must be after start date");
        }

        User user = userService.findUserEntityById(userId);

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.goalName())
                .targetAmount(request.targetAmount())
                .targetDate(request.targetDate())
                .startDate(request.startDate())
                .user(user)
                .build();

        goal = savingsGoalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Transactional
    @CacheEvict(value = "goals", key = "#userId")
    public SavingsGoalResponse updateGoal(Long userId, Long goalId, SavingsGoalUpdateRequest request) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NotFoundException("Savings Goal not found"));

        if (request.goalName() != null) {
            goal.setGoalName(request.goalName());
        }
        if (request.targetAmount() != null) {
            goal.setTargetAmount(request.targetAmount());
        }
        if (request.targetDate() != null) {
            if (request.targetDate().isBefore(goal.getStartDate())) {
                throw new BadRequestException("Target date must be after start date");
            }
            goal.setTargetDate(request.targetDate());
        }

        goal = savingsGoalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Transactional
    @CacheEvict(value = "goals", key = "#userId")
    public void deleteGoal(Long userId, Long goalId) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NotFoundException("Savings Goal not found"));
        savingsGoalRepository.delete(goal);
    }

    private SavingsGoalResponse mapToResponse(SavingsGoal goal) {
        BigDecimal totalIncome = transactionRepository.calculateTotalIncomeSince(goal.getUser().getId(), goal.getStartDate());
        BigDecimal totalExpense = transactionRepository.calculateTotalExpenseSince(goal.getUser().getId(), goal.getStartDate());
        
        BigDecimal currentProgress = totalIncome.subtract(totalExpense);
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double progressPercentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }
        
        progressPercentage = Math.min(progressPercentage, 100.0);

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress,
                remainingAmount,
                progressPercentage
        );
    }
}
