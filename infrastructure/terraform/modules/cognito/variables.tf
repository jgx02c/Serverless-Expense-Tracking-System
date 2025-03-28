variable "user_pool_name" {
  description = "Name of the Cognito User Pool"
  type        = string
}

variable "client_name" {
  description = "Name of the Cognito User Pool Client"
  type        = string
}

variable "domain_prefix" {
  description = "Domain prefix for the Cognito User Pool"
  type        = string
  default     = "expense-tracker"
}

variable "callback_urls" {
  description = "List of allowed callback URLs for the Cognito User Pool Client"
  type        = list(string)
  default     = ["http://localhost:3000/callback"]
}

variable "logout_urls" {
  description = "List of allowed logout URLs for the Cognito User Pool Client"
  type        = list(string)
  default     = ["http://localhost:3000"]
}

variable "tags" {
  description = "Tags to apply to the Cognito User Pool"
  type        = map(string)
  default     = {}
} 