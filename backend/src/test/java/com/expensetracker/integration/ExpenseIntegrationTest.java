package com.expensetracker.integration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.expensetracker.lambda.CreateExpenseHandler;
import com.expensetracker.lambda.GetExpensesHandler;
import com.expensetracker.lambda.ProcessExpenseHandler;
import com.expensetracker.lambda.model.ExpenseRequest;
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
class ExpenseIntegrationTest {

    @Mock
    private AmazonDynamoDB dynamoDB;

    @Mock
    private AmazonSQS sqs;

    @Mock
    private Context context;

    private CreateExpenseHandler createHandler;
    private GetExpensesHandler getHandler;
    private ProcessExpenseHandler processHandler;
    private ObjectMapper objectMapper;

    private static final String TABLE_NAME = "test-table";
    private static final String QUEUE_URL = "test-queue";
    private static final String TEST_USER_ID = "test-user-id";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        createHandler = new CreateExpenseHandler(dynamoDB, sqs, objectMapper, TABLE_NAME, QUEUE_URL);
        getHandler = new GetExpensesHandler(dynamoDB, objectMapper, TABLE_NAME);
        processHandler = new ProcessExpenseHandler(dynamoDB, objectMapper, TABLE_NAME);
    }

    @Test
    void testCompleteExpenseFlow() throws Exception {
        // Step 1: Create an expense
        ExpenseRequest expenseRequest = new ExpenseRequest();
        expenseRequest.setDescription("Test Integration Expense");
        expenseRequest.setAmount(new BigDecimal("150.00"));
        expenseRequest.setCategory("Food");
        expenseRequest.setDate(LocalDateTime.now());
        expenseRequest.setReceiptUrl("https://example.com/receipt.jpg");
        expenseRequest.setNotes("Integration test notes");

        APIGatewayProxyRequestEvent createInput = new APIGatewayProxyRequestEvent();
        createInput.setBody(objectMapper.writeValueAsString(expenseRequest));
        
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", TEST_USER_ID);
        createInput.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        createInput.getRequestContext().setAuthorizer(new APIGatewayProxyRequestEvent.ProxyRequestContext.Authorizer());
        createInput.getRequestContext().getAuthorizer().setClaims(claims);

        // Mock DynamoDB and SQS responses for create
        when(dynamoDB.putItem(any())).thenReturn(new com.amazonaws.services.dynamodbv2.model.PutItemResult());
        when(sqs.sendMessage(any())).thenReturn(new SendMessageResult().withMessageId("test-message-id"));

        APIGatewayProxyResponseEvent createResponse = createHandler.handleRequest(createInput, context);
        assertEquals(200, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        // Parse the created expense response
        ExpenseResponse createdExpense = objectMapper.readValue(createResponse.getBody(), ExpenseResponse.class);
        assertNotNull(createdExpense.getId());
        assertEquals("PENDING", createdExpense.getStatus());

        // Step 2: Get the expense
        APIGatewayProxyRequestEvent getInput = new APIGatewayProxyRequestEvent();
        getInput.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        getInput.getRequestContext().setAuthorizer(new APIGatewayProxyRequestEvent.ProxyRequestContext.Authorizer());
        getInput.getRequestContext().getAuthorizer().setClaims(claims);

        // Mock DynamoDB response for get
        List<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue(createdExpense.getId()));
        item.put("userId", new AttributeValue(TEST_USER_ID));
        item.put("description", new AttributeValue("Test Integration Expense"));
        item.put("amount", new AttributeValue("150.00"));
        item.put("category", new AttributeValue("Food"));
        item.put("date", new AttributeValue(LocalDateTime.now().toString()));
        item.put("status", new AttributeValue("PENDING"));
        item.put("createdAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("updatedAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("receiptUrl", new AttributeValue("https://example.com/receipt.jpg"));
        item.put("notes", new AttributeValue("Integration test notes"));
        items.add(item);

        QueryResult queryResult = new QueryResult().withItems(items);
        when(dynamoDB.query(any(QueryRequest.class))).thenReturn(queryResult);

        APIGatewayProxyResponseEvent getResponse = getHandler.handleRequest(getInput, context);
        assertEquals(200, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());

        // Step 3: Process the expense from SQS
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody(objectMapper.writeValueAsString(item));
        sqsEvent.setRecords(Collections.singletonList(message));

        // Mock DynamoDB response for update
        when(dynamoDB.updateItem(any())).thenReturn(new com.amazonaws.services.dynamodbv2.model.UpdateItemResult());

        processHandler.handleRequest(sqsEvent, context);

        // Verify the expense was processed
        verify(dynamoDB).updateItem(any());
    }

    @Test
    void testExpenseRetrievalWithDateFilter() throws Exception {
        // Prepare test data
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        Map<String, String> claims = new HashMap<>();
        claims.put("sub", TEST_USER_ID);
        input.setRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());
        input.getRequestContext().setAuthorizer(new APIGatewayProxyRequestEvent.ProxyRequestContext.Authorizer());
        input.getRequestContext().getAuthorizer().setClaims(claims);

        // Set date range parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("startDate", LocalDateTime.now().minusDays(7).toString());
        queryParams.put("endDate", LocalDateTime.now().toString());
        input.setQueryStringParameters(queryParams);

        // Mock DynamoDB response
        List<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("test-id"));
        item.put("userId", new AttributeValue(TEST_USER_ID));
        item.put("description", new AttributeValue("Test Expense"));
        item.put("amount", new AttributeValue("100.00"));
        item.put("category", new AttributeValue("Food"));
        item.put("date", new AttributeValue(LocalDateTime.now().toString()));
        item.put("status", new AttributeValue("PROCESSED"));
        item.put("createdAt", new AttributeValue(LocalDateTime.now().toString()));
        item.put("updatedAt", new AttributeValue(LocalDateTime.now().toString()));
        items.add(item);

        QueryResult queryResult = new QueryResult().withItems(items);
        when(dynamoDB.query(any(QueryRequest.class))).thenReturn(queryResult);

        // Execute test
        APIGatewayProxyResponseEvent response = getHandler.handleRequest(input, context);

        // Verify results
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        
        List<ExpenseResponse> expenses = objectMapper.readValue(
            response.getBody(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, ExpenseResponse.class)
        );
        assertEquals(1, expenses.size());
        assertEquals("test-id", expenses.get(0).getId());
        assertEquals("PROCESSED", expenses.get(0).getStatus());
    }
} 