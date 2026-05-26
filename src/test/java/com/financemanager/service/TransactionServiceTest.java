package com.financemanager.service;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.entity.Category;
import com.financemanager.entity.CategoryType;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_FutureDateThrowsError() {
        TransactionRequest req = new TransactionRequest(BigDecimal.TEN, LocalDate.now().plusDays(1), "Food", "desc", CategoryType.EXPENSE);
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(1L, req));
    }
    
    @Test
    void createTransaction_Success() {
        TransactionRequest req = new TransactionRequest(BigDecimal.TEN, LocalDate.now(), "Food", "desc", CategoryType.EXPENSE);
        when(userService.findUserEntityById(1L)).thenReturn(new User());
        
        Category cat = Category.builder().name("Food").type(CategoryType.EXPENSE).build();
        when(categoryService.findCategoryByNameAndUserId("Food", 1L)).thenReturn(cat);
        
        Transaction tx = Transaction.builder().id(1L).amount(BigDecimal.TEN).category(cat).build();
        when(transactionRepository.save(any())).thenReturn(tx);
        
        transactionService.createTransaction(1L, req);
        verify(transactionRepository, times(1)).save(any());
    }
}
