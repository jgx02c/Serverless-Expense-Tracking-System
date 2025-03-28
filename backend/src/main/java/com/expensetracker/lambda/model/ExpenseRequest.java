 package com.expensetracker.lambda.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRequest {
    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDateTime date;
    private String receiptUrl;
    private String notes;
}