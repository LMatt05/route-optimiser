package com.lukematthews.routeoptimiser.routing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRouteResponse(String code, String message, List<RouteResult> routes) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RouteResult(Geometry geometry){

    }
    /** GeoJSON LineString: each position is [longitude, latitude]. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(List<List<Double>> coordinates){

    }
}

