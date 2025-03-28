package com.expensetracker.service;

import com.expensetracker.model.Expense;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ExpenseService {
    Expense createExpense(Expense expense);
    Optional<Expense> getExpense(String id);
    List<Expense> getUserExpenses(String userId);
    List<Expense> getUserExpensesByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
    void deleteExpense(String id);
    List<Expense> getExpensesByCategory(String category);
    List<Expense> getExpensesByStatus(String status);
    Expense updateExpense(String id, Expense expense);
    void processExpense(String id);
} 