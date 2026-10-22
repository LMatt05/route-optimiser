package com.lukematthews.routeoptimiser.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {
    private final Stop a = Stop.of(new Coordinate(51.5, -0.1));
    private final Stop b = Stop.of(new Coordinate(51.6, -0.2));
    private final Stop c = Stop.of(new Coordinate(51.7, -0.3));

    @Test
    void totalIsSumOfLegs() {
        Route route = new Route(List.of(a,b,c),
                List.of(new RouteLeg(a,b,100), new RouteLeg(b,c, 50)));
        assertEquals(150, route.totalDurationSeconds());
        assertEquals(a, route.start());
    }

    @Test
    void singleStopRouteHasNoLegsAndZeroDuration(){
        Route route = new Route(List.of(a), List.of());
        assertEquals(0, route.totalDurationSeconds());
    }

    @Test
    void rejectsEmptyStops(){
        assertThrows(IllegalArgumentException.class,() -> new Route(List.of(), List.of()));
    }

    @Test
    void rejectsWrongNumberOfLegs(){
        assertThrows(IllegalArgumentException.class, () -> new Route(List.of(a,b,c), List.of(new RouteLeg(a,b,1))));
    }

    @Test
    void rejectsLegsThatDoNotChain(){
        assertThrows(IllegalArgumentException.class, () -> new Route(List.of(a,b,c), List.of(new RouteLeg(a,b,1), new RouteLeg(a,c,1))));
    }

    @Test
    void isUnaffectedByLaterChangesToInputLists(){
        List<Stop> stops = new ArrayList<>(List.of(a));
        Route route = new Route(stops, List.of());
        stops.add(b);
        assertEquals(1, route.stops().size());
    }
}