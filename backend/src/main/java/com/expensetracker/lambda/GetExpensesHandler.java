package com.expensetracker.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.expensetracker.lambda.model.ExpenseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GetExpensesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final AmazonDynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public GetExpensesHandler() {
        this.dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        this.objectMapper = new ObjectMapper();
        this.tableName = System.getenv("DYNAMODB_TABLE");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String userId = input.getRequestContext().getAuthorizer().getClaims().get("sub");
            Map<String, String> queryParams = input.getQueryStringParameters();

            List<ExpenseResponse> expenses;
            if (queryParams != null && queryParams.containsKey("startDate") && queryParams.containsKey("endDate")) {
                LocalDateTime startDate = LocalDateTime.parse(queryParams.get("startDate"));
                LocalDateTime endDate = LocalDateTime.parse(queryParams.get("endDate"));
                expenses = getExpensesByDateRange(userId, startDate, endDate);
            } else {
                expenses = getAllExpenses(userId);
            }

            return APIGatewayProxyResponseEvent.builder()
                .statusCode(200)
                .body(objectMapper.writeValueAsString(expenses))
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

    private List<ExpenseResponse> getAllExpenses(String userId) {
        QueryRequest queryRequest = new QueryRequest()
            .withTableName(tableName)
            .withIndexName("UserIdDateIndex")
            .withKeyConditionExpression("userId = :userId")
            .withExpressionAttributeValues(Collections.singletonMap(":userId", new AttributeValue(userId)));

        QueryResult result = dynamoDB.query(queryRequest);
        return result.getItems().stream()
            .map(this::mapToExpenseResponse)
            .collect(Collectors.toList());
    }

    private List<ExpenseResponse> getExpensesByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        QueryRequest queryRequest = new QueryRequest()
            .withTableName(tableName)
            .withIndexName("UserIdDateIndex")
            .withKeyConditionExpression("userId = :userId AND #date BETWEEN :startDate AND :endDate")
            .withExpressionAttributeNames(Collections.singletonMap("#date", "date"))
            .withExpressionAttributeValues(Map.of(
                ":userId", new AttributeValue(userId),
                ":startDate", new AttributeValue(startDate.toString()),
                ":endDate", new AttributeValue(endDate.toString())
            ));

        QueryResult result = dynamoDB.query(queryRequest);
        return result.getItems().stream()
            .map(this::mapToExpenseResponse)
            .collect(Collectors.toList());
    }

    private ExpenseResponse mapToExpenseResponse(Map<String, AttributeValue> item) {
        return ExpenseResponse.builder()
            .id(item.get("id").getS())
            .userId(item.get("userId").getS())
            .description(item.get("description").getS())
            .amount(new BigDecimal(item.get("amount").getS()))
            .category(item.get("category").getS())
            .date(LocalDateTime.parse(item.get("date").getS()))
            .status(item.get("status").getS())
            .createdAt(LocalDateTime.parse(item.get("createdAt").getS()))
            .updatedAt(LocalDateTime.parse(item.get("updatedAt").getS()))
            .receiptUrl(item.get("receiptUrl").getS())
            .notes(item.get("notes").getS())
            .build();
    }
} 