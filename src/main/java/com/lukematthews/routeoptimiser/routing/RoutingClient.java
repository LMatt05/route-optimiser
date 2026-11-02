package com.lukematthews.routeoptimiser.routing;

import com.lukematthews.routeoptimiser.domain.Coordinate;
import com.lukematthews.routeoptimiser.domain.DurationMatrix;

import java.util.List;

public interface RoutingClient {
    /**
     * @return a matrix where entry [i][j] is the driving time in seconds from
     *         coordinates.get(i) to coordinates.get(j)
     * @throws IllegalArgumentException     if the list is null or empty
     * @throws com.lukematthews.routeoptimiser.domain.NoRouteFoundException        if any pair of points cannot be connected
     * @throws com.lukematthews.routeoptimiser.domain.RoutingUnavailableException  if the routing backend fails
     */
    DurationMatrix getDurationMatrix(List<Coordinate> coordinates);


    /**
     *
     * @param orderedCoordinates the stops in the order they will be visited (at least two)
     * @return points along the roads joining the stops in order, or an empty list
     *         if this client cannot provide geometry
     * @throws IllegalArgumentException     if fewer than two coordinates are given
     * @throws com.lukematthews.routeoptimiser.domain.NoRouteFoundException         if the stops cannot be connected
     * @throws com.lukematthews.routeoptimiser.domain.RoutingUnavailableException   if the routing backend fails
     */
    default List<Coordinate> getRouteGeometry(List<Coordinate> orderedCoordinates){
        return List.of();
    }
}


