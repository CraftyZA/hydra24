package za.co.hydra24.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventImagesTest {

    private static final String KEY = "event-images/7/0b6e7c1e-8f4a-4a52-9a43-1c2d3e4f5a6b.jpg";

    @Test
    void acceptsAKeyInTheEventsFolder() {
        assertTrue(EventImages.isKeyFor(7, KEY));
    }

    @Test
    void refusesAnotherEventsKey() {
        assertFalse(EventImages.isKeyFor(70, KEY));
        assertFalse(EventImages.isKeyFor(8, KEY));
    }

    @Test
    void refusesKeysItWouldNotHaveHandedOut() {
        assertFalse(EventImages.isKeyFor(7, null));
        assertFalse(EventImages.isKeyFor(7, "event-images/7/../8/0b6e7c1e-8f4a-4a52-9a43-1c2d3e4f5a6b.jpg"));
        assertFalse(EventImages.isKeyFor(7, "event-images/7/0b6e7c1e-8f4a-4a52-9a43-1c2d3e4f5a6b.html"));
        assertFalse(EventImages.isKeyFor(7, "index.html"));
    }

    @Test
    void servesImagesAtTheirKey() {
        assertEquals("/" + KEY, EventImages.path(KEY));
        assertNull(EventImages.path(null));
    }
}
