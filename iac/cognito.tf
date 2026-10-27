#############################################
# Cognito: Hydra24's own user pool (nothing shared with MouseScoring).
# Users register and sign in on the Cognito hosted UI (authorization code
# + PKCE); the site exchanges the code for tokens itself.
#############################################

resource "aws_cognito_user_pool" "users" {
  name                = "hydra24-users"
  deletion_protection = "ACTIVE"

  # The email is the username, matched case-insensitively:
  # Ben@Example.com and ben@example.com are the same account.
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]
  username_configuration {
    case_sensitive = false
  }

  verification_message_template {
    default_email_option = "CONFIRM_WITH_CODE"
    email_subject        = "Hydra 24 - Verify your email"
    email_message        = "Welcome to Hydra 24! Your verification code is {####}"
  }

  # Cognito's own sender is limited to 50 emails a day. Move to SES before launch.
  email_configuration {
    email_sending_account = "COGNITO_DEFAULT"
  }

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = true
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  admin_create_user_config {
    allow_admin_create_user_only = false
  }
}

resource "aws_cognito_user_pool_client" "web" {
  name         = "hydra24-web"
  user_pool_id = aws_cognito_user_pool.users.id

  # Public browser client: no secret, PKCE instead.
  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["openid", "email"]
  supported_identity_providers         = ["COGNITO"]
  explicit_auth_flows                  = ["ALLOW_USER_SRP_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
  prevent_user_existence_errors        = "ENABLED"

  callback_urls = [for origin in local.hydra24_origins : "${origin}/auth/callback.html"]
  logout_urls   = [for origin in local.hydra24_origins : "${origin}/"]

  # ID token for a day, refreshed in the browser for up to 30 days.
  id_token_validity      = 24
  access_token_validity  = 24
  refresh_token_validity = 30
  token_validity_units {
    id_token      = "hours"
    access_token  = "hours"
    refresh_token = "days"
  }
}

resource "aws_cognito_user_pool_domain" "users" {
  domain       = var.auth_domain_prefix
  user_pool_id = aws_cognito_user_pool.users.id
}

locals {
  auth_url       = "https://${aws_cognito_user_pool_domain.users.domain}.auth.${var.region}.amazoncognito.com"
  cognito_issuer = "https://cognito-idp.${var.region}.amazonaws.com/${aws_cognito_user_pool.users.id}"
}

output "auth_url" {
  description = "Cognito hosted UI base URL"
  value       = local.auth_url
}

output "auth_client_id" {
  description = "Cognito app client id for the site"
  value       = aws_cognito_user_pool_client.web.id
}
