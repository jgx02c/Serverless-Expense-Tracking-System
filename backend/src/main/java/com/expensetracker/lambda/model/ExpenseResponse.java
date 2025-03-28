package com.expensetracker.lambda.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponse {
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