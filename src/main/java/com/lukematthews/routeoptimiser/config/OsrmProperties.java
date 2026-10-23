package com.lukematthews.routeoptimiser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix="osrm")
public record OsrmProperties(
    @DefaultValue("http://localhost:5000") String baseUrl,
    @DefaultValue("5s") Duration timeout){
}
