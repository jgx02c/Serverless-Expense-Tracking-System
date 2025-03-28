package com.expensetracker.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.expensetracker.lambda.model.ExpenseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetExpensesHandlerTest {

    @Mock
    private AmazonDynamoDB dynamoDB;

    @Mock
    private Context context;

    private GetExpensesHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new GetExpensesHandler(dynamoDB, new ObjectMapper(), "test-table");
        objectMapper = new ObjectMapper();
    }

    @Test
    void handleRequest_GetAllExpenses() throws Exception {
        // Prepare test data
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        input.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        input.getRequestContext().setAuthorizer(new APIGatewayProxyRequestEvent.ProxyRequestContext.Authorizer());
        input.getRequestContext().getAuthorizer().setClaims(claims);

        // Mock DynamoDB response
        List<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("test-id"));
        item.put("userId", new AttributeValue("test-user-id"));
        item.put("description", new AttributeValue("Test Expense"));
        item.put("amount", new AttributeValue("100.00"));
        item.put("category", new AttributeValue("Food"));
        item.put("date", new AttributeValue(LocalDateTime.now().toString()));
        item.put("status", new AttributeValue("PROCESSED"));
        item.put("createdAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("updatedAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("receiptUrl", new AttributeValue("https://example.com/receipt.jpg"));
        item.put("notes", new AttributeValue("Test notes"));
        items.add(item);

        QueryResult queryResult = new QueryResult().withItems(items);
        when(dynamoDB.query(any(QueryRequest.class))).thenReturn(queryResult);

        // Execute test
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

        // Verify results
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Parse response body
        List<ExpenseResponse> expenses = objectMapper.readValue(
            response.getBody(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, ExpenseResponse.class)
        );
        assertEquals(1, expenses.size());
        assertEquals("test-id", expenses.get(0).getId());
        assertEquals("Test Expense", expenses.get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), expenses.get(0).getAmount());
        
        // Verify DynamoDB was called
        verify(dynamoDB).query(any(QueryRequest.class));
    }

    @Test
    void handleRequest_GetExpensesByDateRange() throws Exception {
        // Prepare test data
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        input.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        input.getRequestContext().setAuthorizer(new APIGatewayProxyRequestEvent.ProxyRequestContext.Authorizer());
        input.getRequestContext().getAuthorizer().setClaims(claims);

        // Set query parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("startDate", LocalDateTime.now().minusDays(7).toString());
        queryParams.put("endDate", LocalDateTime.now().toString());
        input.setQueryStringParameters(queryParams);

        // Mock DynamoDB response
        List<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("test-id"));
        item.put("userId", new AttributeValue("test-user-id"));
        item.put("description", new AttributeValue("Test Expense"));
        item.put("amount", new AttributeValue("100.00"));
        item.put("category", new AttributeValue("Food"));
        item.put("date", new AttributeValue(LocalDateTime.now().toString()));
        item.put("status", new AttributeValue("PROCESSED"));
        item.put("createdAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("updatedAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("receiptUrl", new AttributeValue("https://example.com/receipt.jpg"));
        item.put("notes", new AttributeValue("Test notes"));
        items.add(item);

        QueryResult queryResult = new QueryResult().withItems(items);
        when(dynamoDB.query(any(QueryRequest.class))).thenReturn(queryResult);

        // Execute test
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

        // Verify results
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Parse response body
        List<ExpenseResponse> expenses = objectMapper.readValue(
            response.getBody(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, ExpenseResponse.class)
        );
        assertEquals(1, expenses.size());
        assertEquals("test-id", expenses.get(0).getId());
        assertEquals("Test Expense", expenses.get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), expenses.get(0).getAmount());
        
        // Verify DynamoDB was called with date range parameters
        verify(dynamoDB).query(any(QueryRequest.class));
    }

    @Test
    void handleRequest_DynamoDBError() throws Exception {
        // Prepare test data
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        input.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        input.getRequestContext().setAuthorizer(new APIGatewayProxyRequestEvent.ProxyRequestContext.Authorizer());
        input.getRequestContext().getAuthorizer().setClaims(claims);

        // Mock DynamoDB error
        when(dynamoDB.query(any(QueryRequest.class))).thenThrow(new RuntimeException("DynamoDB error"));

        // Execute test
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);

        // Verify results
        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("error"));
        
        // Verify DynamoDB was called
        verify(dynamoDB).query(any(QueryRequest.class));
    }
} 