variable "project_name" {
  description = "Name of the project, used as prefix for resource names"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g., dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "cognito_client_id" {
  description = "ID of the Cognito User Pool Client"
  type        = string
}

variable "cognito_domain" {
  description = "Domain of the Cognito User Pool"
  type        = string
}

variable "domain_name" {
  description = "Domain name for the API Gateway"
  type        = string
  default     = "expensetracker.com"
}

variable "certificate_arn" {
  description = "ARN of the ACM certificate for the domain"
  type        = string
}

variable "route53_zone_id" {
  description = "ID of the Route53 hosted zone"
  type        = string
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
} 