# State migration from MouseScoring

The landing page (S3, ACM, CloudFront, Route53 records) was first applied from
`mousescoring/iac/hydra24.tf`, so the live resources are recorded in the
MouseScoring state (`production/terraform.tfstate`). They move to this
project's state (`hydra24/terraform.tfstate`) with no AWS changes.

Two files do the handover. The two applies can run in either order:

| Side         | File                              | Effect on apply                                  |
|--------------|-----------------------------------|--------------------------------------------------|
| MouseScoring | `mousescoring/iac/hydra24_removed.tf` | `removed { destroy = false }`: forgets the 15 resources, deletes nothing |
| Hydra24      | `hydra24/iac/imports.tf`          | `import {}`: adopts them by their AWS IDs         |

If one side applies first, the resources are briefly in neither state or in
both. That's harmless as long as nobody deletes `hydra24_removed.tf` before
MouseScoring has applied it. Without it, MouseScoring would plan to
**destroy the live site** (the bucket has `force_destroy = true`).

## Hydra24

```bash
cd hydra24/iac
terraform init
terraform plan    # expect: 14 to import, 1 to add, 3 to change, 0 to destroy
terraform apply   # or ../deploy.sh, which also uploads ui/
```

The 3 changes are Terraform-only flags (`force_destroy`, `allow_overwrite`). The
1 add is `aws_acm_certificate_validation`, a wait-only resource that can't be
imported. It makes no AWS change.

## MouseScoring

The next normal deploy picks it up. The plan shows 15 × "will no longer be
managed by Terraform, but will not be destroyed".

## Clean up

After both sides have applied, delete `hydra24/iac/imports.tf` and
`mousescoring/iac/hydra24_removed.tf`.
