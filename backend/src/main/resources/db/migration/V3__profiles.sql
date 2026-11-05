-- One per user. gender, race and disability hold enum names. The primary key
-- doubles as the member number. preferred_class_id only pre-fills entry forms, so
-- deleting the class just clears it.
CREATE TABLE profiles (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            BIGINT      NOT NULL UNIQUE REFERENCES users (id),
    first_name         TEXT        NOT NULL,
    last_name          TEXT        NOT NULL,
    cellphone          TEXT        NOT NULL,
    date_of_birth      DATE        NOT NULL,
    gender             TEXT        NOT NULL,
    preferred_class_id BIGINT      REFERENCES classes (id) ON DELETE SET NULL,
    emergency_name     TEXT        NOT NULL,
    emergency_no       TEXT        NOT NULL,
    id_number          TEXT,
    race               TEXT,
    disability         TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
