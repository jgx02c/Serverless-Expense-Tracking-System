# IAM role for Lambda functions
resource "aws_iam_role" "lambda" {
  name = "${var.project_name}-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# IAM policy for Lambda functions
resource "aws_iam_role_policy" "lambda" {
  name = "${var.project_name}-lambda-policy"
  role = aws_iam_role.lambda.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Query",
          "dynamodb:Scan",
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = [
          "${var.dynamodb_arn}",
          "${var.dynamodb_arn}/index/*",
          "${var.sqs_queue_arn}"
        ]
      }
    ]
  })
}

# Lambda function for creating expenses
resource "aws_lambda_function" "create_expense" {
  filename         = "${path.module}/functions/create-expense.jar"
  function_name    = "${var.project_name}-create-expense"
  role            = aws_iam_role.lambda.arn
  handler         = "com.expensetracker.lambda.CreateExpenseHandler::handleRequest"
  runtime         = "java17"
  timeout         = 30
  memory_size     = 256
  source_code_hash = filebase64sha256("${path.module}/functions/create-expense.jar")

  environment {
    variables = {
      DYNAMODB_TABLE = var.dynamodb_table_name
      SQS_QUEUE_URL  = var.sqs_queue_url
    }
  }
}

# Lambda function for getting expenses
resource "aws_lambda_function" "get_expenses" {
  filename         = "${path.module}/functions/get-expenses.jar"
  function_name    = "${var.project_name}-get-expenses"
  role            = aws_iam_role.lambda.arn
  handler         = "com.expensetracker.lambda.GetExpensesHandler::handleRequest"
  runtime         = "java17"
  timeout         = 30
  memory_size     = 256
  source_code_hash = filebase64sha256("${path.module}/functions/get-expenses.jar")

  environment {
    variables = {
      DYNAMODB_TABLE = var.dynamodb_table_name
    }
  }
}

# Lambda function for processing expenses (triggered by SQS)
resource "aws_lambda_function" "process_expense" {
  filename         = "${path.module}/functions/process-expense.jar"
  function_name    = "${var.project_name}-process-expense"
  role            = aws_iam_role.lambda.arn
  handler         = "com.expensetracker.lambda.ProcessExpenseHandler::handleRequest"
  runtime         = "java17"
  timeout         = 30
  memory_size     = 256
  source_code_hash = filebase64sha256("${path.module}/functions/process-expense.jar")

  environment {
    variables = {
      DYNAMODB_TABLE = var.dynamodb_table_name
    }
  }
}

# SQS trigger for process_expense Lambda
resource "aws_lambda_event_source_mapping" "process_expense" {
  event_source_arn = var.sqs_queue_arn
  enabled          = true
  function_name    = aws_lambda_function.process_expense.arn
  batch_size       = 1
}

# API Gateway integration for create_expense Lambda
resource "aws_apigatewayv2_integration" "create_expense" {
  api_id           = var.api_gateway_id
  integration_type = "AWS_PROXY"
  integration_uri  = aws_lambda_function.create_expense.invoke_arn
  payload_format_version = "2.0"
}

# API Gateway route for create_expense
resource "aws_apigatewayv2_route" "create_expense" {
  api_id    = var.api_gateway_id
  route_key = "POST /expenses"
  target    = "integrations/${aws_apigatewayv2_integration.create_expense.id}"
  authorization_type = "JWT"
  authorizer_id = var.api_gateway_authorizer_id
}

# Lambda permission for API Gateway
resource "aws_lambda_permission" "create_expense" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.create_expense.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${var.api_gateway_arn}/*/*"
}

# API Gateway integration for get_expenses Lambda
resource "aws_apigatewayv2_integration" "get_expenses" {
  api_id           = var.api_gateway_id
  integration_type = "AWS_PROXY"
  integration_uri  = aws_lambda_function.get_expenses.invoke_arn
  payload_format_version = "2.0"
}

# API Gateway route for get_expenses
resource "aws_apigatewayv2_route" "get_expenses" {
  api_id    = var.api_gateway_id
  route_key = "GET /expenses"
  target    = "integrations/${aws_apigatewayv2_integration.get_expenses.id}"
  authorization_type = "JWT"
  authorizer_id = var.api_gateway_authorizer_id
}

# Lambda permission for API Gateway
resource "aws_lambda_permission" "get_expenses" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_expenses.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${var.api_gateway_arn}/*/*"
} 