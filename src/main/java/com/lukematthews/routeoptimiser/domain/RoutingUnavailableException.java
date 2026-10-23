package com.lukematthews.routeoptimiser.domain;

/** OSRM could not be reached, timed out, or returned something unusable. */
public class RoutingUnavailableException extends RuntimeException{
    public RoutingUnavailableException(String message){
        super(message);
    }

    public RoutingUnavailableException(String message, Throwable cause){
        super(message, cause);
    }
}
