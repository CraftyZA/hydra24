# To do

Architecture decisions to revisit. Add the date and why it was parked.

## Run Flyway at deploy time, not on Lambda cold start

*Parked 2026-10-27, when users/login was first added.*

Today `quarkus.flyway.migrate-at-start=true` (`backend/src/main/resources/application.properties`):
every Lambda cold start connects to Postgres and checks `flyway_schema_history`
before serving its first request. Warm requests skip it. That is fine at this
size, but:

- Every cold start pays for the check, even when there is nothing to migrate.
- A failed migration only shows up as a broken first request after the deploy,
  not as a failed deploy.
- When several instances cold-start at once, they race to migrate. Flyway
  locks, but the losers wait inside Lambda's 10 s init window.
- The app role needs DDL rights at runtime, so it is not least privilege.

Options:

1. **Migrate endpoint**, the MouseScoring pattern: `migrate-at-start=false` and a
   `POST /admin/migrate` route protected by a shared secret, which `deploy.sh`
   calls after the apply and fails on error. It's simple, but the API now has
   an admin route.
2. **Separate migration Lambda** (or the same image with a migrate-only flag)
   that `deploy.sh` runs with `aws lambda invoke`. It has no public route and
   can use its own DDL-capable role, while the app role keeps DML only.

Option 2 fits the security-first standard best.

## Profile ID number, race and disability

*Parked 2026-11-05, because nothing uses them yet and POPIA needs a purpose for
collecting them.*

They are off the profile form (`ui/profile/`), so the site doesn't collect them.
The API and the `profiles` table still accept them, and `/api/options` still lists
the race and disability choices, ready for when they come back.

Race and disability are special personal information, so before putting them back:

- Name the purpose: a federation or reporting need that actually uses them.
- Publish a privacy policy that names these fields and why they're collected.
- Add an explicit consent line to that form step.
- Add a way to delete a profile.
- Get someone qualified to check the wording.

If no such need appears, remove them from the API and the table instead.
