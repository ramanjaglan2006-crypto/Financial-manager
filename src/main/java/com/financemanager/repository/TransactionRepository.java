package com.financemanager.repository;

import com.financemanager.entity.CategoryType;
import com.financemanager.entity.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserId(Long userId, Sort sort);
    
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByCategoryId(Long categoryId);
    
    @Query("SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.user.id = :userId " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:categoryName IS NULL OR t.category.name = :categoryName) " +
           "AND (:type IS NULL OR t.type = :type)")
    List<Transaction> findFilteredTransactions(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("categoryName") String categoryName,
                                               @Param("type") CategoryType type,
                                               Sort sort);
                                               
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME' AND t.date >= :startDate")
    BigDecimal calculateTotalIncomeSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.date >= :startDate")
    BigDecimal calculateTotalExpenseSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND YEAR(t.date) = :year AND MONTH(t.date) = :month AND t.type = :type " +
           "GROUP BY t.category.name")
    List<Object[]> aggregateMonthlyByCategory(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month, @Param("type") CategoryType type);
    
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND YEAR(t.date) = :year AND t.type = :type " +
           "GROUP BY t.category.name")
    List<Object[]> aggregateYearlyByCategory(@Param("userId") Long userId, @Param("year") int year, @Param("type") CategoryType type);
}
