terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  required_version = ">= 1.0.0"

  backend "s3" {
    bucket         = "expense-tracker-terraform-state"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
}

# DynamoDB Table
module "dynamodb" {
  source = "./modules/dynamodb"

  table_name = "${var.project_name}-expenses"
  hash_key   = "id"
  attributes = [
    {
      name = "id"
      type = "S"
    },
    {
      name = "userId"
      type = "S"
    },
    {
      name = "date"
      type = "S"
    }
  ]
  global_secondary_indexes = [
    {
      name               = "UserIdDateIndex"
      hash_key           = "userId"
      range_key          = "date"
      projection_type    = "ALL"
      read_capacity      = 5
      write_capacity     = 5
    }
  ]
}

# Cognito User Pool
module "cognito" {
  source = "./modules/cognito"

  user_pool_name = "${var.project_name}-user-pool"
  client_name    = "${var.project_name}-client"
}

# API Gateway
module "api_gateway" {
  source = "./modules/api_gateway"

  project_name = var.project_name
  cognito_arn  = module.cognito.user_pool_arn
}

# Lambda Functions
module "lambda" {
  source = "./modules/lambda"

  project_name     = var.project_name
  dynamodb_arn     = module.dynamodb.table_arn
  cognito_user_pool = module.cognito.user_pool_id
  api_gateway_id    = module.api_gateway.id
}

# SQS Queue
module "sqs" {
  source = "./modules/sqs"

  queue_name = "${var.project_name}-expense-queue"
} 