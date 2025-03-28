package com.expensetracker.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessExpenseHandlerTest {

    @Mock
    private AmazonDynamoDB dynamoDB;

    @Mock
    private Context context;

    private ProcessExpenseHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new ProcessExpenseHandler(dynamoDB, new ObjectMapper(), "test-table");
        objectMapper = new ObjectMapper();
    }

    @Test
    void handleRequest_Success() throws Exception {
        // Prepare test data
        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        
        Map<String, AttributeValue> expense = new HashMap<>();
        expense.put("id", new AttributeValue("test-id"));
        expense.put("userId", new AttributeValue("test-user-id"));
        expense.put("description", new AttributeValue("Test Expense"));
        expense.put("amount", new AttributeValue("100.00"));
        expense.put("category", new AttributeValue("Food"));
        expense.put("date", new AttributeValue(LocalDateTime.now().toString()));
        expense.put("status", new AttributeValue("PENDING"));
        expense.put("createdAt", new AttributeValue(LocalDateTime.now().toString()));
        expense.put("updatedAt", new AttributeValue(LocalDateTime.now().toString()));
        expense.put("receiptUrl", new AttributeValue("https://example.com/receipt.jpg"));
        expense.put("notes", new AttributeValue("Test notes"));

        message.setBody(objectMapper.writeValueAsString(expense));
        event.setRecords(Collections.singletonList(message));

        // Mock DynamoDB response
        when(dynamoDB.updateItem(any(UpdateItemRequest.class))).thenReturn(new UpdateItemResult());

        // Execute test
        Void result = handler.handleRequest(event, context);

        // Verify results
        assertNull(result);
        
        // Verify DynamoDB was called with correct parameters
        verify(dynamoDB).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void handleRequest_InvalidMessageBody() throws Exception {
        // Prepare test data with invalid message body
        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody("invalid json");
        event.setRecords(Collections.singletonList(message));

        // Execute test and verify exception
        assertThrows(RuntimeException.class, () -> handler.handleRequest(event, context));
        
        // Verify DynamoDB was not called
        verifyNoInteractions(dynamoDB);
    }

    @Test
    void handleRequest_DynamoDBError() throws Exception {
        // Prepare test data
        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        
        Map<String, AttributeValue> expense = new HashMap<>();
        expense.put("id", new AttributeValue("test-id"));
        expense.put("userId", new AttributeValue("test-user-id"));
        expense.put("description", new AttributeValue("Test Expense"));
        expense.put("amount", new AttributeValue("100.00"));
        expense.put("category", new AttributeValue("Food"));
        expense.put("date", new AttributeValue(LocalDateTime.now().toString()));
        expense.put("status", new AttributeValue("PENDING"));
        expense.put("createdAt", new AttributeValue(LocalDateTime.now().toString()));
        expense.put("updatedAt", new AttributeValue(LocalDateTime.now().toString()));
        expense.put("receiptUrl", new AttributeValue("https://example.com/receipt.jpg"));
        expense.put("notes", new AttributeValue("Test notes"));

        message.setBody(objectMapper.writeValueAsString(expense));
        event.setRecords(Collections.singletonList(message));

        // Mock DynamoDB error
        when(dynamoDB.updateItem(any(UpdateItemRequest.class))).thenThrow(new RuntimeException("DynamoDB error"));

        // Execute test and verify exception
        assertThrows(RuntimeException.class, () -> handler.handleRequest(event, context));
        
        // Verify DynamoDB was called
        verify(dynamoDB).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void handleRequest_EmptyEvent() {
        // Prepare test data with empty event
        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.emptyList());

        // Execute test
        Void result = handler.handleRequest(event, context);

        // Verify results
        assertNull(result);
        
        // Verify DynamoDB was not called
        verifyNoInteractions(dynamoDB);
    }
} 