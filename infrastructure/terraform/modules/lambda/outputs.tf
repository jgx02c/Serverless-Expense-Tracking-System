output "create_expense_function_arn" {
  description = "ARN of the create expense Lambda function"
  value       = aws_lambda_function.create_expense.arn
}

output "get_expenses_function_arn" {
  description = "ARN of the get expenses Lambda function"
  value       = aws_lambda_function.get_expenses.arn
}

output "process_expense_function_arn" {
  description = "ARN of the process expense Lambda function"
  value       = aws_lambda_function.process_expense.arn
}

output "function_arns" {
  description = "Map of all Lambda function ARNs"
  value = {
    create_expense = aws_lambda_function.create_expense.arn
    get_expenses   = aws_lambda_function.get_expenses.arn
    process_expense = aws_lambda_function.process_expense.arn
  }
}

output "lambda_role_arn" {
  description = "ARN of the Lambda IAM role"
  value       = aws_iam_role.lambda.arn
} 