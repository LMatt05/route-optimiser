package com.lukematthews.routeoptimiser.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapBoundsPropertiesTest {
    @Test
    void acceptsValidBounds() {
        assertDoesNotThrow(() -> new MapBoundsProperties(51.28, -0.52, 51.70, 0.34));
    }

    @Test
    void rejectsSouthNotBelowNorth() {
        assertThrows(IllegalArgumentException.class, () -> new MapBoundsProperties(51.7, -0.52, 51.28, 0.34));
    }

    @Test
    void rejectsWestNotBelowEast() {
        assertThrows(IllegalArgumentException.class, () -> new MapBoundsProperties(51.28, 0.34, 51.70, -0.52));
    }

    @Test
    void rejectsOutOfRangeLatitude() {
        assertThrows(IllegalArgumentException.class, () -> new MapBoundsProperties(-91, -0.52, 51.70, 0.34));
    }
}