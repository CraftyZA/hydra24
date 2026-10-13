# Hydra24

hydra24.co.za. It was split out of the MouseScoring project and still runs on
MouseScoring's VPC and Postgres instance to keep AWS costs low.

## Layout

- `iac/` - Terraform. Own state: `s3://mousescoring-terraform-state/hydra24/terraform.tfstate`
  (same bucket and DynamoDB lock table as MouseScoring, different key).
- `backend/` - backend service (not started).
- `ui/` - static landing page, S3 + CloudFront (OAC).
- `docs/` - design notes. Read `docs/shared_infrastructure.md` before touching
  anything network or database related.
- `deploy.sh` - applies `iac/`, syncs `ui/` to S3, invalidates CloudFront.

## Shared infrastructure rules

MouseScoring **owns** the VPC, private subnets, security groups
(`mousescoring-lambda-sg`, `mousescoring-rds-sg`, `mousescoring-endpoints-sg`),
VPC endpoints and the RDS instance `mousescoring-postgres`. This project:

- Reads them only through data sources in `iac/shared.tf`. Never declare,
  import or modify them here.
- May add its own standalone resources that point at them, such as
  `aws_vpc_security_group_ingress_rule` into the RDS (5432) or endpoints (443)
  group from a Hydra24-owned Lambda SG.
- Must never require a change in the MouseScoring repo just to work, except when
  a shared resource itself has to change. Make that change in MouseScoring.

Postgres: use the separate `hydra24` database with its own `hydra24_app` role.
Do not use a schema in MouseScoring's `3dscores` database, and do not use the
RDS master credentials in the app.

Cognito: Hydra24 has its own user pool, defined in `iac/`. Nothing is shared
with MouseScoring's pools.

## MouseScoring reference

Repo: https://github.com/CraftyZA/mousescoring (local checkout:
`~/code/mousescoring`). Use it as the reference for patterns this project
should follow. Read it; don't change it from here.

- `iac/vpc.tf`, `iac/db_postgres.tf` - the shared resources listed above.
- `iac/lambda.tf`, `iac/apigw.tf` - Lambda-in-VPC + HTTP API pattern.
- `iac/cognito.tf`, `iac/jwt_trust.tf` - Cognito pools and backend-side JWT
  verification (the backend verifies ID tokens itself, with no API Gateway authorizer).
- `iac/active_site.tf` - S3 + CloudFront site pattern (`iac/site.tf` here is a copy).
- `backend/` - Quarkus (GraalVM native) backend with Flyway migrations.
- `deploy.sh` - full build-and-deploy flow.

## Working preferences

- Don't run `sudo`. Give the user the command to run.
- Don't run `terraform apply` or `deploy.sh` unless asked. `terraform plan`
  is fine.
