package com.lukematthews.routeoptimiser.routing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmTableResponse(String code, String message, List<List<Double>> durations) {
}
