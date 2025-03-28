package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @RequestBody Expense expense,
            @AuthenticationPrincipal String userId) {
        expense.setUserId(userId);
        return ResponseEntity.ok(expenseService.createExpense(expense));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpense(@PathVariable String id) {
        return expenseService.getExpense(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getUserExpenses(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(expenseService.getUserExpensesByDateRange(userId, startDate, endDate));
        }
        return ResponseEntity.ok(expenseService.getUserExpenses(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(expenseService.getExpensesByCategory(category));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Expense>> getExpensesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(expenseService.getExpensesByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable String id,
            @RequestBody Expense expense,
            @AuthenticationPrincipal String userId) {
        expense.setUserId(userId);
        return ResponseEntity.ok(expenseService.updateExpense(id, expense));
    }
} 