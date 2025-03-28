package com.expensetracker.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;

public class ProcessExpenseHandler implements RequestHandler<SQSEvent, Void> {
    private final AmazonDynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public ProcessExpenseHandler() {
        this.dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
        this.tableName = System.getenv("DYNAMODB_TABLE");
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                // Parse message body
                Map<String, AttributeValue> expense = objectMapper.readValue(message.getBody(), Map.class);
                String id = expense.get("id").getS();

                // Update expense status
                UpdateItemRequest updateRequest = new UpdateItemRequest()
                    .withTableName(tableName)
                    .withKey(Map.of("id", new AttributeValue(id)))
                    .withUpdateExpression("SET #status = :status, updatedAt = :updatedAt")
                    .withExpressionAttributeNames(Map.of("#status", "status"))
                    .withExpressionAttributeValues(Map.of(
                        ":status", new AttributeValue("PROCESSED"),
                        ":updatedAt", new AttributeValue(LocalDateTime.now().toString())
                    ));

                dynamoDB.updateItem(updateRequest);
                context.getLogger().log("Successfully processed expense: " + id);

            } catch (Exception e) {
                context.getLogger().log("Error processing message: " + e.getMessage());
                throw new RuntimeException("Failed to process expense", e);
            }
        }
        return null;
    }
} 