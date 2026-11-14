#############################################
# HTTP API in front of the backend Lambda
# Routes are listed one by one, so only real endpoints reach the Lambda. No
# authorizer: the backend verifies the bearer token itself (jwt_trust.tf).
#############################################

resource "aws_apigatewayv2_api" "backend" {
  name          = "hydra24-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = local.hydra24_origins
    allow_methods = ["GET", "POST", "PUT", "DELETE"]
    allow_headers = ["Authorization", "Content-Type"]
    max_age       = 300
  }
}

resource "aws_apigatewayv2_integration" "backend" {
  api_id                 = aws_apigatewayv2_api.backend.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.backend.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "backend" {
  for_each = toset([
    "GET /api/options",
    "GET /api/classes",
    "GET /api/events",
    "GET /api/events/{id}",
    "GET /api/events/{id}/classes",

    "PUT /api/me",
    "GET /api/me/profile",
    "PUT /api/me/profile",
    "GET /api/me/entries",
    "GET /api/events/{id}/entry",
    "PUT /api/events/{id}/entry",
    "DELETE /api/events/{id}/entry",

    "GET /api/admin/classes",
    "POST /api/admin/classes",
    "GET /api/admin/classes/{id}",
    "PUT /api/admin/classes/{id}",
    "DELETE /api/admin/classes/{id}",
    "POST /api/admin/events",
    "PUT /api/admin/events/{id}",
    "DELETE /api/admin/events/{id}",
    "GET /api/admin/events/{id}/entries",
    "POST /api/admin/events/{id}/image/upload",
    "PUT /api/admin/events/{id}/image",
    "DELETE /api/admin/events/{id}/image",
    "GET /api/admin/members",
    "GET /api/admin/members/{id}",
    "GET /api/admin/members/{id}/profile",
    "PUT /api/admin/members/{id}/profile",
    "GET /api/admin/members/{id}/entries",
  ])

  api_id             = aws_apigatewayv2_api.backend.id
  route_key          = each.value
  target             = "integrations/${aws_apigatewayv2_integration.backend.id}"
  authorization_type = "NONE"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.backend.id
  name        = "$default"
  auto_deploy = true

  # 0/0 would block every request.
  default_route_settings {
    throttling_burst_limit = 20
    throttling_rate_limit  = 10
  }
}

resource "aws_lambda_permission" "api_invoke_backend" {
  statement_id  = "AllowHydra24ApiInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.backend.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.backend.execution_arn}/*/*"
}

output "api_url" {
  description = "Base URL of the Hydra24 API"
  value       = aws_apigatewayv2_api.backend.api_endpoint
}
