package com.lukematthews.routeoptimiser.service;

import com.lukematthews.routeoptimiser.config.RouteProperties;
import com.lukematthews.routeoptimiser.domain.*;
import com.lukematthews.routeoptimiser.routing.RoutingClient;
import com.lukematthews.routeoptimiser.solver.RouteSolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class RouteOptimisationService {

    /** The start is placed first in the list sent to OSRM, so it is always index 0. */
    private static final int START_INDEX = 0;

    private final RoutingClient routingClient;
    private final RouteSolver routeSolver;
    private final int maxStops;

    public RouteOptimisationService(RoutingClient routingClient,
                                    RouteSolver routeSolver,
                                    RouteProperties properties){
        this.routingClient = Objects.requireNonNull(routingClient, "routingClient must not be null");
        this.routeSolver = Objects.requireNonNull(routeSolver, "routeSolver must not be null");
        this.maxStops = Objects.requireNonNull(properties, "properties must not be null").maxStops();
    }

    /**
     * Finds a quick order for visiting every destination, beginning at {@code start}
     * not returning to it.
     *
     * @throws IllegalArgumentException     if there are no desitnations or too many stops
     * @throws com.lukematthews.routeoptimiser.domain.NoRouteFoundException if some points cannot be connected
     * @throws com.lukematthews.routeoptimiser.domain.RoutingUnavailableException if OSRM fails
     */
    public Route optimise(Stop start, List<Stop> destinations){
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(destinations, "destinations must not be null");
        if (destinations.isEmpty()){
            throw new IllegalArgumentException("at least one destination is required");
        }
        if(destinations.stream().anyMatch(Objects::isNull)){
            throw new IllegalArgumentException("destinations must not contain null");
        }
        int totalStops = destinations.size() + 1;
        if(totalStops > maxStops) {
            throw new IllegalArgumentException(
                    "too many stops: " + totalStops + " (including the start), the limit is " + maxStops);
        }

        List<Stop> stops = new ArrayList<>(totalStops);
        stops.add(start);
        stops.addAll(destinations);

        List<Coordinate> coordinates = stops.stream().map(Stop::coordinate).toList();
        DurationMatrix matrix = routingClient.getDurationMatrix(coordinates);
        if(matrix.size() != totalStops){
            throw new IllegalStateException(
                    "routing client returned a " + matrix.size() + "x" + matrix.size() + " matrix for " + totalStops + " stops"
            );
        }
        List<Integer> order = routeSolver.solve(matrix, START_INDEX);
        checkOrder(order, totalStops);

        return buildRoute(stops, order, matrix);
    }

    /** prevents solvers from producing incorrect route.*/
    private static void checkOrder(List<Integer> order, int n){
        if (order.size() != n || order.get(0) != START_INDEX){
            throw new IllegalStateException("solver returned an invalid visiting order: " + order);
        }
        boolean[] seen = new boolean[n];
        for (int index : order){
            if (index < 0 || index>= n || seen[index]){
                throw new IllegalStateException("solver returned an invalid visiting order: " + order);
            }
            seen[index] = true;
        }
    }

    private static Route buildRoute(List<Stop> stops, List<Integer> order, DurationMatrix matrix){
        List<Stop> orderedStops = new ArrayList<>(order.size());
        List<RouteLeg> legs = new ArrayList<>(order.size() - 1);

        for (int i = 0; i < order.size(); i++) {
            orderedStops.add(stops.get(order.get(i)));
            if (i > 0){
                double seconds = matrix.get(order.get(i - 1), order.get(i));
                legs.add(new RouteLeg(orderedStops.get(i-1), orderedStops.get(i), seconds));
            }
        }
        return new Route(orderedStops, legs);
    }

}
