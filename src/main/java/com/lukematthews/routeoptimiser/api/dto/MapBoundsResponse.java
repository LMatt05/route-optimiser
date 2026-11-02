package com.lukematthews.routeoptimiser.api.dto;

import com.lukematthews.routeoptimiser.config.MapBoundsProperties;

public record MapBoundsResponse(double south, double west, double north, double east) {
    public static MapBoundsResponse from(MapBoundsProperties bounds) {
        return new MapBoundsResponse(bounds.south(), bounds.west(), bounds.north(), bounds.east());
    }
}