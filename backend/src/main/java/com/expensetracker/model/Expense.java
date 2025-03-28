package com.expensetracker.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private String id;
    private String userId;
    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDateTime date;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String receiptUrl;
    private String notes;
} 