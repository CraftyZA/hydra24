#############################################
# The hydra24 database on MouseScoring's Postgres instance
#
# Terraform owns the app role's password, not the database itself: the
# instance is private, so the database and role are created once by hand as
# the master user. See docs/shared_infrastructure.md ("One-off setup").
#############################################

locals {
  database = {
    name     = "hydra24"
    username = "hydra24_app"
  }
}

# Alphanumeric only, so it can be pasted into SQL without quoting trouble.
resource "random_password" "hydra24_app" {
  length  = 32
  special = false
}

resource "aws_secretsmanager_secret" "hydra24_app" {
  name        = "hydra24/rds/app"
  description = "hydra24_app login for the hydra24 database on mousescoring-postgres"
}

resource "aws_secretsmanager_secret_version" "hydra24_app" {
  secret_id = aws_secretsmanager_secret.hydra24_app.id
  secret_string = jsonencode({
    username = local.database.username
    password = random_password.hydra24_app.result
    engine   = "postgres"
    host     = data.aws_db_instance.shared_postgres.address
    port     = data.aws_db_instance.shared_postgres.port
    dbname   = local.database.name
  })
}
