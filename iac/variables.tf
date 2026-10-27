variable "region" {
  type        = string
  description = "AWS region (must match the MouseScoring VPC and RDS instance)"
  default     = "us-east-1"
}

variable "site_price_class" {
  type        = string
  description = "CloudFront price class for the hydra24.co.za distribution"
  default     = "PriceClass_100"
}

variable "auth_domain_prefix" {
  type        = string
  description = "Cognito hosted UI domain prefix (<prefix>.auth.<region>.amazoncognito.com); must be globally unique"
  default     = "hydra24"
}
