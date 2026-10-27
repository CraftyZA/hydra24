# Shared infrastructure

Hydra24 reuses MouseScoring's VPC and Postgres instance to avoid paying for a
second RDS instance (and a second set of VPC interface endpoints).

## Ownership rule

Every AWS object has exactly one owning Terraform state.

- **MouseScoring owns** the VPC, private subnets, route table, security groups
  (`mousescoring-lambda-sg`, `mousescoring-rds-sg`, `mousescoring-endpoints-sg`),
  VPC endpoints, the RDS instance `mousescoring-postgres` and its master secret.
- **Hydra24 reads** them through data sources in `iac/shared.tf`, by name/tag.
  It never declares them as `resource`s, never imports them, and never changes
  their attributes.
- **Hydra24 owns** anything it adds next to them, such as its own Lambda security
  group and standalone security group rules pointing into the shared groups.

There is no AWS "shared" flag that enforces this. Terraform only gets confused
when two states claim the same object, or when one state's resource rewrites a
list another state appends to. The rule above avoids the first. The next
section covers the second.

## Security groups

A security group with **inline** `ingress {}` / `egress {}` blocks owns its whole
rule list, so a rule added from another state is deleted on the owner's next
apply. Both shared groups keep their ingress as standalone
`aws_vpc_security_group_ingress_rule` resources, so Hydra24 can add its own:

| Group                       | Hydra24 adds (owned here, `iac/backend.tf`)       |
|-----------------------------|---------------------------------------------------|
| `mousescoring-rds-sg`       | 5432 from `hydra24-lambda-sg`                     |
| `mousescoring-endpoints-sg` | nothing yet: the backend makes no AWS SDK calls   |

`hydra24-lambda-sg` itself only allows egress to `mousescoring-rds-sg` on 5432.

Keep it that way. Never add an inline `ingress {}` block to either group in
MouseScoring.

## Postgres

Yes, Hydra24 shares the instance. It costs nothing extra on the same
`db.t3.micro` beyond storage and CPU headroom.

Use a **separate database plus its own login role** instead of a new schema
inside `3dscores`:

- MouseScoring's backend connects to `3dscores` as the RDS **master** user. A
  Hydra24 schema in that database would be readable and writable by
  MouseScoring, and the reverse if Hydra24 reused those credentials.
- A separate database makes the eventual split a plain `pg_dump hydra24`. Flyway
  history, extensions and `search_path` never collide.
- It costs the same as a schema.

### One-off setup

Terraform (`iac/database.tf`) generates `hydra24_app`'s password and stores it
in the Hydra24-owned secret `hydra24/rds/app`. The database and role have to
be created once by hand as the master user, because the instance is private.
Run `terraform apply` first so the secret exists.

1. Get both passwords on your machine:
   ```bash
   aws secretsmanager get-secret-value --secret-id mousescoring/rds/master --query SecretString --output text
   aws secretsmanager get-secret-value --secret-id hydra24/rds/app --query SecretString --output text
   ```
2. In the AWS console, open CloudShell, then **Actions > Create VPC environment**:
   VPC `mousescoring-vpc`, one `mousescoring-private-*` subnet, security group
   `hydra24-lambda-sg`. That group can already reach Postgres, so nothing
   shared changes.
3. Connect as the master user (`username` from the first secret), using the
   `host` from either secret:
   ```bash
   psql "host=<host> dbname=postgres user=<master username> sslmode=require"
   ```
4. Run:
   ```sql
   CREATE ROLE hydra24_app LOGIN;
   \password hydra24_app
   -- paste the password from hydra24/rds/app (twice)
   GRANT hydra24_app TO CURRENT_USER; -- PG16+: needed to create a database it owns
   CREATE DATABASE hydra24 OWNER hydra24_app;
   REVOKE CONNECT ON DATABASE hydra24 FROM PUBLIC;
   ```
5. Delete the CloudShell VPC environment.

The backend runs its Flyway migrations (`backend/src/main/resources/db/migration`)
on cold start, so the tables appear on the first request after this.

`hydra24_app` can still open a connection to `3dscores`, because PUBLIC has
CONNECT there by default. It has no privileges on the `archery` schema or its
tables, so it can't read or change anything. Leave the PUBLIC grant on
`3dscores` alone; revoking it is a MouseScoring change.

`hydra24_app`'s password lives only in the Hydra24-owned secret
`hydra24/rds/app` (Terraform-managed), never in the MouseScoring secret.

Backups and restores: see [disaster_recovery.md](disaster_recovery.md).

## Cognito

Hydra24 gets its own user pool, owned by `iac/cognito.tf` here. Nothing is
shared with MouseScoring's per-country pools. There is no `cognito-idp` VPC
endpoint (MouseScoring removed it), so the backend never calls Cognito:
Terraform fetches the pool's signing keys and passes them to the Lambda
(`iac/jwt_trust.tf`).

## Moving out later

If Hydra24 gets its own VPC/RDS, swap the data sources in `iac/shared.tf` for
resources. MouseScoring needs no changes, because it never references Hydra24.
