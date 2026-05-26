package com.financemanager.repository;

import com.financemanager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrIsDefaultTrue(Long userId);
    Optional<Category> findByNameAndUserId(String name, Long userId);
    Optional<Category> findByNameAndIsDefaultTrue(String name);
    boolean existsByNameAndUserId(String name, Long userId);
}
