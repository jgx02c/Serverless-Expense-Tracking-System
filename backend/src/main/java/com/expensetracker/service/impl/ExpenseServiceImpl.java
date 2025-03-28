package com.expensetracker.service.impl;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.service.ExpenseService;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AmazonSQS sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    @Autowired
    public ExpenseServiceImpl(
            ExpenseRepository expenseRepository,
            AmazonSQS sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.queue-url}") String queueUrl) {
        this.expenseRepository = expenseRepository;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    @Override
    @Transactional
    public Expense createExpense(Expense expense) {
        expense.setId(UUID.randomUUID().toString());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        expense.setStatus("PENDING");

        Expense savedExpense = expenseRepository.save(expense);
        sendToProcessingQueue(savedExpense);
        return savedExpense;
    }

    @Override
    public Optional<Expense> getExpense(String id) {
        return expenseRepository.findById(id);
    }

    @Override
    public List<Expense> getUserExpenses(String userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public List<Expense> getUserExpensesByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    @Transactional
    public void deleteExpense(String id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(category);
    }

    @Override
    public List<Expense> getExpensesByStatus(String status) {
        return expenseRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Expense updateExpense(String id, Expense expense) {
        return expenseRepository.findById(id)
            .map(existingExpense -> {
                expense.setId(id);
                expense.setUpdatedAt(LocalDateTime.now());
                return expenseRepository.save(expense);
            })
            .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    @Override
    @Transactional
    public void processExpense(String id) {
        expenseRepository.findById(id)
            .ifPresent(expense -> {
                expense.setStatus("PROCESSED");
                expense.setUpdatedAt(LocalDateTime.now());
                expenseRepository.save(expense);
            });
    }

    private void sendToProcessingQueue(Expense expense) {
        try {
            String message = objectMapper.writeValueAsString(expense);
            sqsClient.sendMessage(queueUrl, message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send expense to processing queue", e);
        }
    }
} 