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
    allow_methods = ["PUT"]
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
    "PUT /api/me",
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
