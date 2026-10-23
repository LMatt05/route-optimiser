package com.lukematthews.routeoptimiser.domain;

/**The points are valid, but OSRM cannot connect them by road. */
public class NoRouteFoundException extends RuntimeException{
    public NoRouteFoundException(String message){
        super(message);
    }
}
