-- event_type holds an EventType enum name. entry_close is South African wall-clock
-- time; NULL means entries stay open until the event starts.
CREATE TABLE events (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          TEXT      NOT NULL,
    event_type    TEXT      NOT NULL,
    starts_on     DATE      NOT NULL,
    days          INT       NOT NULL CHECK (days > 0),
    entry_close   TIMESTAMP,
    venue_name    TEXT      NOT NULL,
    venue_address TEXT      NOT NULL,
    itinerary     TEXT,
    accommodation TEXT,
    catering      TEXT
);

CREATE INDEX events_starts_on_ix ON events (starts_on);

-- A person's place at an event. `cost` is the price when they entered, so later
-- price changes do not touch existing entries. attendance is ALL_DAYS or DAY_<n>.
CREATE TABLE entries (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id   BIGINT        NOT NULL REFERENCES events (id),
    profile_id BIGINT        NOT NULL REFERENCES profiles (id),
    class_id   BIGINT        NOT NULL REFERENCES classes (id),
    attendance TEXT          NOT NULL,
    cost       NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (event_id, profile_id)
);

CREATE INDEX entries_profile_ix ON entries (profile_id);
CREATE INDEX entries_class_ix ON entries (class_id);
