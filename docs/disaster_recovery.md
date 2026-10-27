# Disaster recovery

What is backed up, what is not, and how to restore Hydra24 without disturbing
MouseScoring.

## What is backed up

| What                               | Backup                                                        | Owner        |
|------------------------------------|---------------------------------------------------------------|--------------|
| `hydra24` database                 | Covered by the RDS instance's backups (below)                 | MouseScoring |
| Code, Terraform, site              | Git (`github.com/CraftyZA/hydra24`); rebuilt by `deploy.sh`   | Hydra24      |
| Terraform state                    | S3 `mousescoring-terraform-state`, key `hydra24/terraform.tfstate` | shared bucket |
| `hydra24/rds/app` secret           | Password is in Terraform state; `terraform apply` recreates the secret | Hydra24 |
| **Cognito user pool**              | **None.** See below                                           | Hydra24      |

### The database

The `hydra24` database lives on MouseScoring's `mousescoring-postgres` instance,
so it is covered by MouseScoring's protection (`mousescoring/iac/db_postgres.tf`,
`backup.tf`):

- **Automated RDS backups:** point-in-time recovery over the last 14 days.
- **AWS Backup, daily:** vault `mousescoring-backup` (us-east-1), kept 35 days.
- **AWS Backup, weekly:** copied to `mousescoring-backup-dr` (us-west-2), kept 1 year.
- **AWS Backup, monthly:** copied to `mousescoring-backup-dr` (us-west-2), kept 7 years.

**Restores are instance-wide.** Every backup is of the whole instance, so
restoring it in place rolls back MouseScoring's `3dscores` as well as `hydra24`.
Never do that for a Hydra24-only problem. Restore to a new instance and copy
the one database across (below).

### Cognito

Cognito has no backup, and passwords can't be exported. If the pool is deleted,
every account is gone and users must register again. Protection:

- The pool has `deletion_protection = "ACTIVE"` (`iac/cognito.tf`), so neither
  Terraform nor the console can delete it without turning that off first.
- `users` rows are matched on the lowercased email (`UserRepository`), so a
  user who registers again in a new pool gets their existing row (and anything
  hanging off it) back on first sign-in. Nothing else is needed to relink them.

## Restore only the `hydra24` database

Use this when Hydra24 data is lost or corrupted and MouseScoring is fine.

1. **Restore to a new instance.** In the RDS console, open `mousescoring-postgres`:
   - **Point in time** (last 14 days): *Actions > Restore to point in time*, or
   - **A snapshot**: AWS Backup > vault `mousescoring-backup` > the recovery point > *Restore*.

   Name it e.g. `hydra24-restore`. Use DB subnet group `mousescoring-db-privatesubnets`
   and security group `mousescoring-rds-sg`; no public access. The master
   credentials are the same as at backup time.
2. **Open a CloudShell VPC environment** as in the one-off setup in
   [shared_infrastructure.md](shared_infrastructure.md) (`mousescoring-vpc`, a
   private subnet, `hydra24-lambda-sg`). Both instances accept it on 5432.
3. **Copy the database across**, as the master user:
   ```bash
   pg_dump "host=<restore host> dbname=hydra24 user=<master> sslmode=require" -Fc -f hydra24.dump
   psql "host=<live host> dbname=postgres user=<master> sslmode=require" \
     -c "ALTER DATABASE hydra24 RENAME TO hydra24_broken;" \
     -c "CREATE DATABASE hydra24 OWNER hydra24_app;" \
     -c "REVOKE CONNECT ON DATABASE hydra24 FROM PUBLIC;"
   pg_restore "host=<live host> dbname=hydra24 user=<master> sslmode=require" \
     --no-owner --role=hydra24_app hydra24.dump
   ```
   The rename needs no open connections to `hydra24`. If it fails, set the
   `hydra24-backend` Lambda's reserved concurrency to 0 first, then back afterwards.
4. **Check the site**, then drop `hydra24_broken` and delete the
   `hydra24-restore` instance (it is billed while it runs).

## Lose the whole instance

That is a MouseScoring recovery: it restores `mousescoring-postgres`, and
`hydra24` comes back with it. Afterwards, check that the `hydra24_app` login still
works with the password in `hydra24/rds/app`. If the instance was replaced under
a new endpoint, run `./deploy.sh` so the Lambda picks up the new host from
`iac/shared.tf`.
