package com.lukematthews.routeoptimiser.domain;

import java.util.Objects;

public record RouteLeg(Stop from, Stop to, double durationSeconds) {
    public RouteLeg{
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if(Double.isNaN(durationSeconds) || Double.isInfinite(durationSeconds)|| durationSeconds < 0){
            throw new IllegalArgumentException(
                    "durationSeconds must be finite and non-negative but was " + durationSeconds
            );
        }
    }
}
