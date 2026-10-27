# Hydra24

hydra24.co.za, split out of the MouseScoring repo. This folder is self-contained
and can be moved to its own repository as-is.

| Folder     | Contents                                                        |
|------------|-----------------------------------------------------------------|
| `iac/`     | Terraform, with its own state (`hydra24/terraform.tfstate`)      |
| `backend/` | Quarkus native Lambda (users, sign-in)                           |
| `ui/`      | Static site, served from S3 behind CloudFront                    |
| `docs/`    | Design notes                                                     |

Deploy with `./deploy.sh`. It builds the backend, applies `iac/`, then syncs
`ui/` to S3. The first deploy needs the one-off database setup in
[docs/shared_infrastructure.md](docs/shared_infrastructure.md).

## Relationship to MouseScoring

Hydra24 runs inside MouseScoring's VPC and on its Postgres instance, to keep
costs down. MouseScoring owns those resources, and Hydra24 only reads them. See
[docs/shared_infrastructure.md](docs/shared_infrastructure.md) before adding
anything that touches the VPC, security groups or database.

The site was first deployed from the MouseScoring state. The one-off handover is
in [docs/state_migration.md](docs/state_migration.md).
