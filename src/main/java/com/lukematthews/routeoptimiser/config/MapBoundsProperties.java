package com.lukematthews.routeoptimiser.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Bounding box of area loaded into OSRM. */
@ConfigurationProperties(prefix = "map.bounds")
public record MapBoundsProperties(@DefaultValue("51.28") double south,
                                  @DefaultValue("-0.52") double west,
                                  @DefaultValue("51.70") double north,
                                  @DefaultValue("0.34") double east) {
    public MapBoundsProperties{
        if (south < -90 || north > 90 || south >= north){
            throw new IllegalArgumentException("map.bounds latitudes must satisfy -90 <= south < north <= 90");
        }
        if (west <-180 || east > 180 || west >= east) {
            throw new IllegalArgumentException("map.bounds longitudes must satisfy -180 <= west < east <= 180");
        }
    }
}
