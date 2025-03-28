output "id" {
  description = "ID of the API Gateway"
  value       = aws_apigatewayv2_api.this.id
}

output "arn" {
  description = "ARN of the API Gateway"
  value       = aws_apigatewayv2_api.this.arn
}

output "api_url" {
  description = "URL of the API Gateway endpoint"
  value       = aws_apigatewayv2_api.this.api_endpoint
}

output "stage_name" {
  description = "Name of the API Gateway stage"
  value       = aws_apigatewayv2_stage.this.name
}

output "domain_name" {
  description = "Domain name of the API Gateway"
  value       = aws_apigatewayv2_domain_name.this.domain_name
}

output "authorizer_id" {
  description = "ID of the Cognito authorizer"
  value       = aws_apigatewayv2_authorizer.cognito.id
}

output "api_gw_role_arn" {
  description = "ARN of the API Gateway IAM role"
  value       = aws_iam_role.api_gw.arn
} 