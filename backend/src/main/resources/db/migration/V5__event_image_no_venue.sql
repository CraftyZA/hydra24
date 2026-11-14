-- Every event is at Prime View, so events no longer carry a venue.
-- image_key is the event's image in the images bucket (event-images/<id>/<uuid>.jpg).
ALTER TABLE events
    DROP COLUMN venue_name,
    DROP COLUMN venue_address,
    ADD COLUMN image_key TEXT;
