#############################################
# Backend Lambda (Quarkus native) in MouseScoring's private subnets
# Same pattern as MouseScoring's lambda.tf. It makes no AWS SDK calls, so the
# only network access it gets is Postgres.
#############################################

locals {
  backend_name = "hydra24-backend"
  backend_zip  = "${path.module}/../backend/target/function.zip"
}

resource "aws_security_group" "backend" {
  name        = "hydra24-lambda-sg"
  description = "Hydra24 backend Lambda"
  vpc_id      = data.aws_vpc.shared.id
}

resource "aws_vpc_security_group_egress_rule" "backend_to_postgres" {
  security_group_id            = aws_security_group.backend.id
  referenced_security_group_id = data.aws_security_group.shared_rds.id
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  description                  = "Postgres"
}

# Standalone rule into MouseScoring's RDS group (see docs/shared_infrastructure.md).
resource "aws_vpc_security_group_ingress_rule" "postgres_from_backend" {
  security_group_id            = data.aws_security_group.shared_rds.id
  referenced_security_group_id = aws_security_group.backend.id
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  description                  = "Hydra24 backend Lambda"
}

resource "aws_iam_role" "backend" {
  name = "${local.backend_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "backend_vpc_access" {
  role       = aws_iam_role.backend.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/aws/lambda/${local.backend_name}"
  retention_in_days = 30
}

resource "aws_lambda_function" "backend" {
  function_name    = local.backend_name
  role             = aws_iam_role.backend.arn
  runtime          = "provided.al2023"
  handler          = "bootstrap" # ignored by the custom runtime, required by the schema
  timeout          = 15
  memory_size      = 256
  filename         = local.backend_zip
  source_code_hash = filebase64sha256(local.backend_zip)

  environment {
    variables = {
      DB_HOST     = data.aws_db_instance.shared_postgres.address
      DB_PORT     = tostring(data.aws_db_instance.shared_postgres.port)
      DB_NAME     = local.database.name
      DB_USERNAME = local.database.username
      DB_PASSWORD = random_password.hydra24_app.result

      AUTH_JWT_ISSUER   = local.cognito_issuer
      AUTH_JWT_AUDIENCE = aws_cognito_user_pool_client.web.id
      AUTH_JWT_KEYS     = local.cognito_signing_keys

      # Recommended for Quarkus native images on Lambda.
      DISABLE_SIGNAL_HANDLERS = "true"
    }
  }

  vpc_config {
    subnet_ids         = data.aws_subnets.shared_private.ids
    security_group_ids = [aws_security_group.backend.id]
  }

  depends_on = [
    aws_cloudwatch_log_group.backend,
    aws_iam_role_policy_attachment.backend_vpc_access,
  ]
}
