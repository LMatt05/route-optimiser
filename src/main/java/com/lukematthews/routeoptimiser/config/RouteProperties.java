package com.lukematthews.routeoptimiser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "route")
public record RouteProperties(@DefaultValue("50") int maxStops) {
    public RouteProperties{
        if(maxStops<2){
            throw new IllegalArgumentException(
                    "route.max-stops must be at least 2 (a start and one destination), but was " + maxStops);
        }
    }
}
