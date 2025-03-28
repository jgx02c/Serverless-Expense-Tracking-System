 resource "aws_ssm_parameter" "environment_vars" {
  for_each = var.environment_variables

  name  = "/${var.project_name}/${var.environment}/${each.key}"
  type  = "SecureString"
  value = each.value

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ssm_parameter" "lambda_config" {
  for_each = var.lambda_configs

  name  = "/${var.project_name}/${var.environment}/lambda/${each.key}"
  type  = "String"
  value = jsonencode(each.value)

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_cloudwatch_log_group" "lambda_logs" {
  for_each = var.lambda_functions

  name              = "/aws/lambda/${var.project_name}-${var.environment}-${each.key}"
  retention_in_days = var.log_retention_days

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_iam_role_policy_attachment" "lambda_logging" {
  for_each = var.lambda_functions

  role       = each.value.role_name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_dynamodb" {
  for_each = var.lambda_functions

  role       = each.value.role_name
  policy_arn = aws_iam_policy.dynamodb_access[each.key].arn
}

resource "aws_iam_policy" "dynamodb_access" {
  for_each = var.lambda_functions

  name = "${var.project_name}-${var.environment}-${each.key}-dynamodb-access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Query",
          "dynamodb:Scan"
        ]
        Resource = [
          "${var.dynamodb_table_arn}",
          "${var.dynamodb_table_arn}/index/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_sqs" {
  for_each = var.lambda_functions

  role       = each.value.role_name
  policy_arn = aws_iam_policy.sqs_access[each.key].arn
}

resource "aws_iam_policy" "sqs_access" {
  for_each = var.lambda_functions

  name = "${var.project_name}-${var.environment}-${each.key}-sqs-access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = [var.sqs_queue_arn]
      }
    ]
  })
}

resource "aws_lambda_function" "expense_handlers" {
  for_each = var.lambda_functions

  filename         = each.value.filename
  function_name    = "${var.project_name}-${var.environment}-${each.key}"
  role            = each.value.role_arn
  handler         = each.value.handler
  runtime         = "java17"
  timeout         = each.value.timeout
  memory_size     = each.value.memory_size
  environment {
    variables = {
      ENVIRONMENT = var.environment
      DYNAMODB_TABLE = var.dynamodb_table_name
      SQS_QUEUE_URL = var.sqs_queue_url
    }
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_lambda_permission" "api_gateway" {
  for_each = var.lambda_functions

  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.expense_handlers[each.key].function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${var.api_gateway_execution_arn}/*/*"
}

resource "aws_cloudwatch_event_rule" "lambda_schedule" {
  for_each = var.lambda_schedules

  name                = "${var.project_name}-${var.environment}-${each.key}-schedule"
  description         = "Schedule for ${each.key} Lambda function"
  schedule_expression = each.value.schedule
  is_enabled         = true

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_cloudwatch_event_target" "lambda_target" {
  for_each = var.lambda_schedules

  rule      = aws_cloudwatch_event_rule.lambda_schedule[each.key].name
  target_id = "${var.project_name}-${var.environment}-${each.key}"
  arn       = aws_lambda_function.expense_handlers[each.key].arn
}

resource "aws_lambda_permission" "eventbridge" {
  for_each = var.lambda_schedules

  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.expense_handlers[each.key].function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.lambda_schedule[each.key].arn
}