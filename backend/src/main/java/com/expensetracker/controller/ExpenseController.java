package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(
        summary = "Create a new expense",
        description = "Creates a new expense and sends it for processing"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense created successfully",
            content = @Content(schema = @Schema(implementation = Expense.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> createExpense(
        @Parameter(description = "Expense details", required = true)
        @Valid @RequestBody Expense expense
    ) {
        return ResponseEntity.ok(expenseService.createExpense(expense));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get expense by ID",
        description = "Retrieves a specific expense by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense found",
            content = @Content(schema = @Schema(implementation = Expense.class))),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> getExpense(
        @Parameter(description = "Expense ID", required = true)
        @PathVariable String id
    ) {
        return expenseService.getExpense(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
        summary = "Get all expenses",
        description = "Retrieves all expenses with optional date filtering"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
            content = @Content(schema = @Schema(implementation = Expense.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Expense>> getAllExpenses(
        @Parameter(description = "Start date for filtering (ISO format)")
        @RequestParam(required = false) LocalDateTime startDate,
        @Parameter(description = "End date for filtering (ISO format)")
        @RequestParam(required = false) LocalDateTime endDate
    ) {
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update expense",
        description = "Updates an existing expense"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense updated successfully",
            content = @Content(schema = @Schema(implementation = Expense.class))),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> updateExpense(
        @Parameter(description = "Expense ID", required = true)
        @PathVariable String id,
        @Parameter(description = "Updated expense details", required = true)
        @Valid @RequestBody Expense expense
    ) {
        return ResponseEntity.ok(expenseService.updateExpense(id, expense));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete expense",
        description = "Deletes an existing expense"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteExpense(
        @Parameter(description = "Expense ID", required = true)
        @PathVariable String id
    ) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get expenses by category",
        description = "Retrieves all expenses for a specific category"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
            content = @Content(schema = @Schema(implementation = Expense.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Expense>> getExpensesByCategory(
        @Parameter(description = "Category name", required = true)
        @PathVariable String category
    ) {
        return ResponseEntity.ok(expenseService.getExpensesByCategory(category));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get expenses by status",
        description = "Retrieves all expenses with a specific status"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
            content = @Content(schema = @Schema(implementation = Expense.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Expense>> getExpensesByStatus(
        @Parameter(description = "Expense status", required = true)
        @PathVariable String status
    ) {
        return ResponseEntity.ok(expenseService.getExpensesByStatus(status));
    }
} 