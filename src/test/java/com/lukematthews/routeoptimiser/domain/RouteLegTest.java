package com.lukematthews.routeoptimiser.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RouteLegTest {
    private final Stop a = Stop.of(new Coordinate(51.5, -0.1));
    private final Stop b = Stop.of(new Coordinate(51.6, -0.2));

    @Test
    void storesValues() {
        RouteLeg leg = new RouteLeg(a,b,120.5);
        assertEquals(a, leg.from());
        assertEquals(b, leg.to());
        assertEquals(120.5, leg.durationSeconds());
    }

    @Test
    void rejectsNullStops(){
        assertThrows(NullPointerException.class, () -> new RouteLeg(null, b, 1));
        assertThrows(NullPointerException.class, () -> new RouteLeg(a, null, 1));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidDuration(double bad){
        assertThrows(IllegalArgumentException.class, () -> new RouteLeg(a, b, bad));
    }


}