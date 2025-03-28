variable "project_name" {
  description = "Name of the project, used as prefix for resource names"
  type        = string
}

variable "dynamodb_arn" {
  description = "ARN of the DynamoDB table"
  type        = string
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "sqs_queue_arn" {
  description = "ARN of the SQS queue"
  type        = string
}

variable "sqs_queue_url" {
  description = "URL of the SQS queue"
  type        = string
}

variable "api_gateway_id" {
  description = "ID of the API Gateway"
  type        = string
}

variable "api_gateway_arn" {
  description = "ARN of the API Gateway"
  type        = string
}

variable "api_gateway_authorizer_id" {
  description = "ID of the API Gateway authorizer"
  type        = string
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
} 