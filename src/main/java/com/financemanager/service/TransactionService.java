package com.financemanager.service;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.dto.TransactionResponse;
import com.financemanager.dto.TransactionUpdateRequest;
import com.financemanager.entity.Category;
import com.financemanager.entity.CategoryType;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.NotFoundException;
import com.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    public List<TransactionResponse> getTransactions(Long userId, LocalDate startDate, LocalDate endDate, String categoryName, CategoryType type) {
        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(userId, startDate, endDate, categoryName, type, sort);
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "reports", allEntries = true),
        @CacheEvict(value = "goals", key = "#userId")
    })
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        if (request.date().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        User user = userService.findUserEntityById(userId);
        Category category = categoryService.findCategoryByNameAndUserId(request.categoryName(), userId);

        if (category.getType() != request.type()) {
            throw new BadRequestException("Category type does not match transaction type");
        }

        Transaction transaction = Transaction.builder()
                .amount(request.amount())
                .date(request.date())
                .category(category)
                .description(request.description())
                .type(request.type())
                .user(user)
                .build();

        transaction = transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "reports", allEntries = true),
        @CacheEvict(value = "goals", key = "#userId")
    })
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (request.amount() != null) {
            transaction.setAmount(request.amount());
        }
        if (request.description() != null) {
            transaction.setDescription(request.description());
        }
        if (request.type() != null) {
            transaction.setType(request.type());
        }
        if (request.categoryName() != null) {
            Category category = categoryService.findCategoryByNameAndUserId(request.categoryName(), userId);
            if (category.getType() != transaction.getType()) {
                throw new BadRequestException("Category type does not match transaction type");
            }
            transaction.setCategory(category);
        }

        transaction = transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "reports", allEntries = true),
        @CacheEvict(value = "goals", key = "#userId")
    })
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategory().getName(),
                transaction.getDescription(),
                transaction.getType()
        );
    }
}
