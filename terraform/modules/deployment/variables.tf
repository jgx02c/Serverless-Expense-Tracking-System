variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g., dev, staging, prod)"
  type        = string
}

variable "environment_variables" {
  description = "Environment variables for the application"
  type        = map(string)
  default     = {}
}

variable "lambda_functions" {
  description = "Configuration for Lambda functions"
  type = map(object({
    filename    = string
    role_name   = string
    role_arn    = string
    handler     = string
    timeout     = number
    memory_size = number
  }))
}

variable "lambda_configs" {
  description = "Configuration for Lambda functions"
  type        = map(any)
  default     = {}
}

variable "lambda_schedules" {
  description = "Schedule configurations for Lambda functions"
  type = map(object({
    schedule = string
  }))
  default = {}
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table"
  type        = string
}

variable "sqs_queue_url" {
  description = "URL of the SQS queue"
  type        = string
}

variable "sqs_queue_arn" {
  description = "ARN of the SQS queue"
  type        = string
}

variable "api_gateway_execution_arn" {
  description = "Execution ARN of the API Gateway"
  type        = string
}

variable "log_retention_days" {
  description = "Number of days to retain CloudWatch logs"
  type        = number
  default     = 30
} 