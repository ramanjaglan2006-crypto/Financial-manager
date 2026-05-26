package com.financemanager.config;

import com.financemanager.entity.Category;
import com.financemanager.entity.CategoryType;
import com.financemanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedDefaultCategories();
    }

    private void seedDefaultCategories() {
        List<String> expenseCategories = List.of("Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities");
        List<String> incomeCategories = List.of("Salary", "Freelance", "Investment");

        for (String name : expenseCategories) {
            if (categoryRepository.findByNameAndIsDefaultTrue(name).isEmpty()) {
                categoryRepository.save(Category.builder().name(name).type(CategoryType.EXPENSE).isDefault(true).build());
            }
        }

        for (String name : incomeCategories) {
            if (categoryRepository.findByNameAndIsDefaultTrue(name).isEmpty()) {
                categoryRepository.save(Category.builder().name(name).type(CategoryType.INCOME).isDefault(true).build());
            }
        }
    }
}
