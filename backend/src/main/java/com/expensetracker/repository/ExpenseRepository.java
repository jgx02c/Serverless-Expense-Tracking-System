package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ExpenseRepository {
    Expense save(Expense expense);
    Optional<Expense> findById(String id);
    List<Expense> findByUserId(String userId);
    List<Expense> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
    void deleteById(String id);
    List<Expense> findByCategory(String category);
    List<Expense> findByStatus(String status);
} 