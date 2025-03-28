package com.expensetracker.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.expensetracker.lambda.model.ExpenseRequest;
import com.expensetracker.lambda.model.ExpenseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;

public class CreateExpenseHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final AmazonDynamoDB dynamoDB;
    private final AmazonSQS sqs;
    private final ObjectMapper objectMapper;
    private final String tableName;
    private final String queueUrl;

    public CreateExpenseHandler() {
        this.dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        this.sqs = AmazonSQSClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
        this.tableName = System.getenv("DYNAMODB_TABLE");
        this.queueUrl = System.getenv("SQS_QUEUE_URL");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Parse request body
            ExpenseRequest request = objectMapper.readValue(input.getBody(), ExpenseRequest.class);
            String userId = input.getRequestContext().getAuthorizer().getClaims().get("sub");

            // Create expense
            String id = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", new AttributeValue(id));
            item.put("userId", new AttributeValue(userId));
            item.put("description", new AttributeValue(request.getDescription()));
            item.put("amount", new AttributeValue(request.getAmount().toString()));
            item.put("category", new AttributeValue(request.getCategory()));
            item.put("date", new AttributeValue(request.getDate().toString()));
            item.put("status", new AttributeValue("PENDING"));
            item.put("createdAt", new AttributeValue(now.toString()));
            item.put("updatedAt", new AttributeValue(now.toString()));
            item.put("receiptUrl", new AttributeValue(request.getReceiptUrl()));
            item.put("notes", new AttributeValue(request.getNotes()));

            // Save to DynamoDB
            PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(tableName)
                .withItem(item);
            dynamoDB.putItem(putItemRequest);

            // Send to SQS for processing
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(objectMapper.writeValueAsString(item));
            sqs.sendMessage(sendMessageRequest);

            // Create response
            ExpenseResponse response = ExpenseResponse.builder()
                .id(id)
                .userId(userId)
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .date(request.getDate())
                .status("PENDING")
                .createdAt(now)
                .updatedAt(now)
                .receiptUrl(request.getReceiptUrl())
                .notes(request.getNotes())
                .build();

            return APIGatewayProxyResponseEvent.builder()
                .statusCode(200)
                .body(objectMapper.writeValueAsString(response))
                .headers(Map.of("Content-Type", "application/json"))
                .build();

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return APIGatewayProxyResponseEvent.builder()
                .statusCode(500)
                .body("{\"error\": \"" + e.getMessage() + "\"}")
                .headers(Map.of("Content-Type", "application/json"))
                .build();
        }
    }
} 