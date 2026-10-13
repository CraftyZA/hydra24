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

| Group                       | Hydra24 adds (owned here)                         |
|-----------------------------|---------------------------------------------------|
| `mousescoring-rds-sg`       | 5432 from the Hydra24 Lambda SG                   |
| `mousescoring-endpoints-sg` | 443 from the Hydra24 Lambda SG                    |

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

One-off setup, run as the master user (credentials in the
`mousescoring/rds/master` secret):

```sql
CREATE ROLE hydra24_app LOGIN PASSWORD '<generated>';
CREATE DATABASE hydra24 OWNER hydra24_app;
REVOKE CONNECT ON DATABASE hydra24 FROM PUBLIC;
```

`hydra24_app` can still open a connection to `3dscores`, because PUBLIC has
CONNECT there by default. It has no privileges on the `archery` schema or its
tables, so it can't read or change anything. Leave the PUBLIC grant on
`3dscores` alone; revoking it is a MouseScoring change.

Store `hydra24_app`'s password in a Hydra24-owned secret, such as
`hydra24/rds/app`, not in the MouseScoring secret.

The instance is already covered by MouseScoring's AWS Backup plan and
automated snapshots, so the `hydra24` database is backed up too. Restores are
instance-wide, which means a point-in-time restore for one product rolls back
the other unless you restore to a new instance and copy across.

## Cognito

Hydra24 gets its own user pool, owned by `iac/` here. Nothing is shared with
MouseScoring's per-country pools. The existing `cognito-idp` VPC endpoint is
service-wide, so a VPC Lambda can reach the new pool without changes.

## Moving out later

If Hydra24 gets its own VPC/RDS, swap the data sources in `iac/shared.tf` for
resources. MouseScoring needs no changes, because it never references Hydra24.
