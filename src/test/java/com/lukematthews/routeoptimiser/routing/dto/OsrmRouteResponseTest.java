package com.lukematthews.routeoptimiser.routing.dto;

import com.lukematthews.routeoptimiser.api.dto.RouteResponse;
import com.lukematthews.routeoptimiser.domain.Coordinate;
import com.lukematthews.routeoptimiser.domain.Route;
import com.lukematthews.routeoptimiser.domain.Stop;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsrmRouteResponseTest {
    private final Stop a = Stop.of(new Coordinate(51.5, -0.1));

    @Test
    void geometryIsMappedAsLongitudeThenLatitude(){
        Route route = new Route(List.of(a), List.of(),
                List.of(new Coordinate(51.5, -0.12))); // latitude 51.5, longitude -0.12

        RouteResponse response = RouteResponse.from(route);

        assertThat(response.geometry()).containsExactly(List.of(-0.12, 51.5));
    }

    @Test
    void geometryIsEmptyWhenRouteHasNone() {
        RouteResponse response = RouteResponse.from(new Route(List.of(a), List.of()));

        assertTrue(response.geometry().isEmpty());
    }
}