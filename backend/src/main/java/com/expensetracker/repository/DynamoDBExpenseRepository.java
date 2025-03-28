package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Repository
public class DynamoDBExpenseRepository implements ExpenseRepository {

    private final AmazonDynamoDB dynamoDB;
    private final String tableName;

    @Autowired
    public DynamoDBExpenseRepository(AmazonDynamoDB dynamoDB, @Value("${aws.dynamodb.table-name}") String tableName) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
    }

    @Override
    public Expense save(Expense expense) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue(expense.getId()));
        item.put("userId", new AttributeValue(expense.getUserId()));
        item.put("description", new AttributeValue(expense.getDescription()));
        item.put("amount", new AttributeValue(expense.getAmount().toString()));
        item.put("category", new AttributeValue(expense.getCategory()));
        item.put("date", new AttributeValue(expense.getDate().toString()));
        item.put("status", new AttributeValue(expense.getStatus()));
        item.put("createdAt", new AttributeValue(expense.getCreatedAt().toString()));
        item.put("updatedAt", new AttributeValue(expense.getUpdatedAt().toString()));
        item.put("receiptUrl", new AttributeValue(expense.getReceiptUrl()));
        item.put("notes", new AttributeValue(expense.getNotes()));

        PutItemRequest putItemRequest = new PutItemRequest()
            .withTableName(tableName)
            .withItem(item);

        dynamoDB.putItem(putItemRequest);
        return expense;
    }

    @Override
    public Optional<Expense> findById(String id) {
        GetItemRequest getItemRequest = new GetItemRequest()
            .withTableName(tableName)
            .withKey(Collections.singletonMap("id", new AttributeValue(id)));

        GetItemResult result = dynamoDB.getItem(getItemRequest);
        return Optional.ofNullable(result.getItem())
            .map(this::mapToExpense);
    }

    @Override
    public List<Expense> findByUserId(String userId) {
        QueryRequest queryRequest = new QueryRequest()
            .withTableName(tableName)
            .withIndexName("UserIdDateIndex")
            .withKeyConditionExpression("userId = :userId")
            .withExpressionAttributeValues(Collections.singletonMap(":userId", new AttributeValue(userId)));

        QueryResult result = dynamoDB.query(queryRequest);
        return result.getItems().stream()
            .map(this::mapToExpense)
            .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
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
            .map(this::mapToExpense)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
            .withTableName(tableName)
            .withKey(Collections.singletonMap("id", new AttributeValue(id)));

        dynamoDB.deleteItem(deleteItemRequest);
    }

    @Override
    public List<Expense> findByCategory(String category) {
        ScanRequest scanRequest = new ScanRequest()
            .withTableName(tableName)
            .withFilterExpression("category = :category")
            .withExpressionAttributeValues(Collections.singletonMap(":category", new AttributeValue(category)));

        ScanResult result = dynamoDB.scan(scanRequest);
        return result.getItems().stream()
            .map(this::mapToExpense)
            .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByStatus(String status) {
        ScanRequest scanRequest = new ScanRequest()
            .withTableName(tableName)
            .withFilterExpression("status = :status")
            .withExpressionAttributeValues(Collections.singletonMap(":status", new AttributeValue(status)));

        ScanResult result = dynamoDB.scan(scanRequest);
        return result.getItems().stream()
            .map(this::mapToExpense)
            .collect(Collectors.toList());
    }

    private Expense mapToExpense(Map<String, AttributeValue> item) {
        return Expense.builder()
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