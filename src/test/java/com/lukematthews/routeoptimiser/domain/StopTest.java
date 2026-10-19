package com.lukematthews.routeoptimiser.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StopTest {
    private final Coordinate london = new Coordinate(51.5074, -0.1278);

    @Test
    void rejectsNullCoordinate() {
        assertThrows(NullPointerException.class, () -> new Stop("Depot", null));
    }

    @Test
    void keepsLabel(){
        assertEquals("Depot", new Stop("Depot", london).label());
    }

    @Test
    void blankLabelBecomesNull(){
        assertNull(new Stop("   ", london).label());
    }

    @Test
    void ofCreatesUnlabelledStop(){
        Stop stop = Stop.of(london);
        assertNull(stop.label());
        assertEquals(london, stop.coordinate());
    }
}