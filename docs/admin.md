# Admins

An admin can manage classes and events and see who has entered an event. It is
membership of the `admin` group in the Hydra24 Cognito pool (`iac/cognito.tf`). The
backend reads the group from the verified ID token, so every `/api/admin` route is
protected whatever the site shows.

The person must have registered first. To add them:

```bash
aws cognito-idp admin-add-user-to-group \
  --user-pool-id "$(terraform -chdir=iac output -raw user_pool_id)" \
  --username person@example.com \
  --group-name admin
```

The group is read from the token, so it takes effect at the next sign-in, or within
a day when the browser refreshes the token. To remove someone, use
`admin-remove-user-from-group` with the same arguments. Their current token keeps
working until it expires, for up to a day.
