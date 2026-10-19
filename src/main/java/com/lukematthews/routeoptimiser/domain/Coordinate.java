package com.lukematthews.routeoptimiser.domain;

public record Coordinate(double latitude, double longitude) {

    public Coordinate {
        if (Double.isNaN(latitude) || latitude < -90.0 || latitude > 90.0){
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90, but was " + latitude
            );
        }
        if (Double.isNaN(longitude) || longitude < -180.0 || longitude > 180.0){
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180, but was " + longitude
            );
        }
    }
}
