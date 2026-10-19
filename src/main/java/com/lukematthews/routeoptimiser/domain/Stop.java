package com.lukematthews.routeoptimiser.domain;

import java.util.Objects;

public record Stop(String label, Coordinate coordinate) {
    public Stop{
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        if (label != null && label.isBlank()){
            label = null;
        }
    }

    public static Stop of(Coordinate coordinate){
        return new Stop(null, coordinate);
    }
}
