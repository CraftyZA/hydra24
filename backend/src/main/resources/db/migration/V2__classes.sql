-- What an entrant competes in. `cost` is the flat entry price; class_type holds a
-- ClassType enum name.
CREATE TABLE classes (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        TEXT          NOT NULL,
    description TEXT          NOT NULL,
    sort_order  INT           NOT NULL,
    class_type  TEXT          NOT NULL,
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    cost        NUMERIC(10,2) NOT NULL CHECK (cost >= 0)
);

-- A row holds only a deviation from classes.cost at one event type (an EventType
-- enum name). No row: the class's own cost applies.
CREATE TABLE class_event_type_costs (
    class_id   BIGINT        NOT NULL REFERENCES classes (id) ON DELETE CASCADE,
    event_type TEXT          NOT NULL,
    cost       NUMERIC(10,2) NOT NULL CHECK (cost >= 0),
    PRIMARY KEY (class_id, event_type)
);
