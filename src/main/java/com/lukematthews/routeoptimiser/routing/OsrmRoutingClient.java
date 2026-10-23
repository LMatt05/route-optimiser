package com.lukematthews.routeoptimiser.routing;

import com.lukematthews.routeoptimiser.domain.Coordinate;
import com.lukematthews.routeoptimiser.domain.DurationMatrix;
import com.lukematthews.routeoptimiser.domain.NoRouteFoundException;
import com.lukematthews.routeoptimiser.domain.RoutingUnavailableException;
import com.lukematthews.routeoptimiser.routing.dto.OsrmTableResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class OsrmRoutingClient implements RoutingClient{

    private final RestClient restClient;

    public OsrmRoutingClient(RestClient restClient){
        this.restClient = Objects.requireNonNull(restClient, "restClient must not be null.");
    }

    @Override
    public DurationMatrix getDurationMatrix(List<Coordinate> coordinates){
        if(coordinates == null || coordinates.isEmpty()){
            throw new IllegalArgumentException("at least one coordinate is required");
        }
        int n = coordinates.size();
        if(n == 1){
            return new DurationMatrix(new double[][] {{0}});
        }

        OsrmTableResponse body = fetch(coordinates);
        if (body == null){
            throw new RoutingUnavailableException("OSRM returned an empty response");
        }
        if(!"Ok".equals(body.code())){
            throw errorFor(body);
        }
        return toMatrix(body, n);
    }

    private OsrmTableResponse fetch(List<Coordinate> coordinates){
        String uri = "/table/v1/driving/" + toPath(coordinates) + "?annotations=duration";
        try{
            return restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange((request, response) ->{
                        //OSRM reports "no route" as HTTP 400 with a JSON body, so only 5xx is fatal
                        if(response.getStatusCode().is5xxServerError()){
                            throw new RoutingUnavailableException(
                                    "OSRM responded with HTTP " + response.getStatusCode().value()
                            );
                        }
                        return response.bodyTo(OsrmTableResponse.class);
                    });
        } catch (RestClientException e) {
            // connection refused, timeout, or an unreadable body
            throw new RoutingUnavailableException("Could not get a usable response from OSRM", e);
        }
    }

    /** OSRM wants longitude first, and plain decimals (not scientific notation like 1.0E-5).*/
    private static String toPath(List<Coordinate> coordinates){
        return coordinates.stream()
                .map(c -> String.format(Locale.ROOT, "%.6f,%.6f", c.longitude(), c.latitude()))
                .collect(Collectors.joining(";"));
    }

    private static RuntimeException errorFor(OsrmTableResponse body){
        String code = body.code();
        String detail = body.message() == null ? code : code + ": " + body.message();
        if ("NoTable".equals(code) || "NoSegment".equals(code)){
            return new NoRouteFoundException("OSRM could not connect the points(" + detail + ")");
        }
        //InvalidQuery, TooBig, etc. means request wrong, not caller's fault
        return new RoutingUnavailableException("OSRM rejected the request (" + detail + ")");
    }

    private static DurationMatrix toMatrix(OsrmTableResponse body, int n){
        List<List<Double>> rows = body.durations();
        if(rows == null || rows.size() != n ){
            throw new RoutingUnavailableException("OSRM returned a duration table of unexpected size");
        }
        double[][] seconds = new double [n][n];
        for(int i = 0; i<n; i++){
            List<Double> row = rows.get(i);
            if (row == null || row.size() != n){
                throw new RoutingUnavailableException("OSRM returned a duration table of unexpected size");
            }
            for (int j = 0; j < n; j++){
                Double value = row.get(j);
                if(value == null){
                    throw new NoRouteFoundException("No route between point " + i + " and point " + j);
                }
                seconds[i][j] = value;
            }
        }
        return new DurationMatrix(seconds);
    }
}
