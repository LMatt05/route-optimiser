package com.lukematthews.routeoptimiser.api.dto;

import com.lukematthews.routeoptimiser.domain.Route;

import java.util.ArrayList;
import java.util.List;

public record RouteResponse(List<StopDto> stops, List<LegDto> legs, double totalDurationSeconds) {
    /** Leg i travels from stops[fromIndex] to stops [toIndex]. */
    public record LegDto(int fromIndex, int toIndex, double durationSeconds){
    }

    public static RouteResponse from(Route route){
        List<StopDto> stops = route.stops().stream().map(StopDto::from).toList();
        List<LegDto> legs = new ArrayList<>(route.legs().size());
        for (int i = 0; i< route.legs().size(); i++){
            legs.add(new LegDto(i, i+1, route.legs().get(i).durationSeconds()));
        }
        return new RouteResponse(stops, legs, route.totalDurationSeconds());
    }
}
