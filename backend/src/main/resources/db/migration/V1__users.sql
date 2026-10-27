-- One row per Cognito account, written on every sign-in (PUT /api/me).
CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cognito_sub   UUID        NOT NULL UNIQUE,
    email         TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Emails are case-insensitive, as they are in the Cognito pool.
CREATE UNIQUE INDEX users_email_lower_uk ON users (lower(email));
