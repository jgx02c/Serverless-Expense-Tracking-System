 #!/bin/bash

# Exit on error
set -e

# Load environment variables
source .env.prod

# Build the application
echo "Building application..."
mvn clean package -DskipTests

# Deploy Lambda functions
echo "Deploying Lambda functions..."
aws lambda update-function-code \
  --function-name expense-tracker-prod-create-expense \
  --zip-file fileb://target/create-expense.jar

aws lambda update-function-code \
  --function-name expense-tracker-prod-get-expenses \
  --zip-file fileb://target/get-expenses.jar

aws lambda update-function-code \
  --function-name expense-tracker-prod-process-expense \
  --zip-file fileb://target/process-expense.jar

# Update environment variables
echo "Updating environment variables..."
aws ssm put-parameter \
  --name "/expense-tracker/prod/COGNITO_USER_POOL_ID" \
  --value "$COGNITO_USER_POOL_ID" \
  --type SecureString \
  --overwrite

aws ssm put-parameter \
  --name "/expense-tracker/prod/COGNITO_CLIENT_ID" \
  --value "$COGNITO_CLIENT_ID" \
  --type SecureString \
  --overwrite

aws ssm put-parameter \
  --name "/expense-tracker/prod/DYNAMODB_TABLE_NAME" \
  --value "$DYNAMODB_TABLE_NAME" \
  --type SecureString \
  --overwrite

# Deploy API Gateway
echo "Deploying API Gateway..."
aws apigateway create-deployment \
  --rest-api-id "$API_GATEWAY_ID" \
  --stage-name prod

# Update CloudWatch alarms
echo "Updating CloudWatch alarms..."
terraform apply -var-file=environments/prod.tfvars -auto-approve

# Notify deployment
echo "Sending deployment notification..."
aws sns publish \
  --topic-arn "$DEPLOYMENT_NOTIFICATION_TOPIC" \
  --message "Production deployment completed successfully at $(date)"

echo "Production deployment completed successfully!"