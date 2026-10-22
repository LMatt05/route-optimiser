package com.lukematthews.routeoptimiser.domain;

import java.util.List;
import java.util.Objects;

public record Route(List<Stop> stops, List<RouteLeg> legs) {
    public Route{
        Objects.requireNonNull(stops, "stop must not be null");
        Objects.requireNonNull(legs, "legs must not be null");
        stops = List.copyOf(stops);
        legs = List.copyOf(legs);

        if (stops.isEmpty()){
            throw new IllegalArgumentException("a route needs at least one stop");
        }
        if (legs.size() != stops.size() - 1){
            throw new IllegalArgumentException(
                    "expected " + (stops.size() -1) + " legs for " + stops.size() + " stops but got " + legs.size()
            );
        }
        for (int i = 0; i < legs.size(); i++){
            if (!legs.get(i).from().equals(stops.get(i)) || !legs.get(i).to().equals(stops.get(i + 1))) {
                throw new IllegalArgumentException("leg " + i + " does not connect stops " + i + " and " + (i + 1));
            }
        }
    }
    public Stop start() {
        return stops.get(0);
    }
    public double totalDurationSeconds(){
        return legs.stream().mapToDouble(RouteLeg::durationSeconds).sum();
    }
}
