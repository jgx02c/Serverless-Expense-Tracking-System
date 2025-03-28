output "user_pool_id" {
  description = "ID of the Cognito User Pool"
  value       = aws_cognito_user_pool.this.id
}

output "user_pool_arn" {
  description = "ARN of the Cognito User Pool"
  value       = aws_cognito_user_pool.this.arn
}

output "client_id" {
  description = "ID of the Cognito User Pool Client"
  value       = aws_cognito_user_pool_client.this.id
}

output "client_secret" {
  description = "Client secret of the Cognito User Pool Client"
  value       = aws_cognito_user_pool_client.this.client_secret
  sensitive   = true
}

output "domain" {
  description = "Domain of the Cognito User Pool"
  value       = aws_cognito_user_pool_domain.this.domain
}

output "authenticated_role_arn" {
  description = "ARN of the IAM role for authenticated users"
  value       = aws_iam_role.authenticated.arn
}

output "unauthenticated_role_arn" {
  description = "ARN of the IAM role for unauthenticated users"
  value       = aws_iam_role.unauthenticated.arn
} 