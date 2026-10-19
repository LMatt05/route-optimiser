package com.lukematthews.routeoptimiser.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {
    @Test
    void acceptsValidCoordinate() {
        assertDoesNotThrow(() -> new Coordinate(51.5074, -0.1287));
    }

    @Test
    void acceptsBoundaryValues() {
        assertDoesNotThrow(() -> new Coordinate(90,180));
        assertDoesNotThrow(() -> new Coordinate(-90,-180));
    }

    @ParameterizedTest
    @CsvSource({"90.1, 0", "-90.1, 0", "0, 180.1", "0, -180.1", "NaN, 0", "0, NaN"})
    void rejectsInvalidValues(double lat, double lon) {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(lat, lon));
    }

    @Test
    void equalCoordinatesAreEqual(){
        assertEquals(new Coordinate(1,2), new Coordinate(1,2));
    }
}