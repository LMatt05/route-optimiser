package com.lukematthews.routeoptimiser.api.dto;

import com.lukematthews.routeoptimiser.domain.Route;

import java.util.ArrayList;
import java.util.List;

public record RouteResponse(List<StopDto> stops, List<LegDto> legs, double totalDurationSeconds, List<List<Double>> geometry) {
    /** Leg i travels from stops[fromIndex] to stops [toIndex]. */
    public record LegDto(int fromIndex, int toIndex, double durationSeconds){
    }

    public static RouteResponse from(Route route){
        List<StopDto> stops = route.stops().stream().map(StopDto::from).toList();
        List<LegDto> legs = new ArrayList<>(route.legs().size());
        for (int i = 0; i< route.legs().size(); i++){
            legs.add(new LegDto(i, i+1, route.legs().get(i).durationSeconds()));
        }
        // GeoJSON order: [longitude, latitude]. Empty when road geometry isn't available.
        List<List<Double>> geometry = route.geometry().stream()
                .map(c -> List.of(c.longitude(), c.latitude()))
                .toList();
        return new RouteResponse(stops, legs, route.totalDurationSeconds(), geometry);
    }
}
