package com.lukematthews.routeoptimiser.routing;

import com.lukematthews.routeoptimiser.domain.Coordinate;
import com.lukematthews.routeoptimiser.domain.DurationMatrix;
import com.lukematthews.routeoptimiser.domain.NoRouteFoundException;
import com.lukematthews.routeoptimiser.domain.RoutingUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OsrmRoutingClientTest {

    private static final String BASE =  "http://osrm.test";

    private final Coordinate london = new Coordinate(51.5074, -0.1278);
    private final Coordinate paris = new Coordinate(48.8566, 2.3522);

    private MockRestServiceServer server;
    private OsrmRoutingClient client;

    @BeforeEach
    void setUp(){
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OsrmRoutingClient(builder.build());
    }

    @Test
    void sendsLongitudeFirstAndParsesMatrix(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{\"code\":\"Ok\",\"durations\":[[0,540.5],[560.2,0]],\"sources\":[]}",
                        MediaType.APPLICATION_JSON
                ));
        DurationMatrix matrix = client.getDurationMatrix(List.of(london, paris));

        assertEquals(2, matrix.size());
        assertEquals(540.5, matrix.get(0,1));
        assertEquals(560.2, matrix.get(1,0));
        server.verify();
    }

    @Test
    void formatsTinyCoordinatesAsPlainDecimals(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.000020,0.000010;2.352200,48.856600?annotations=duration"))
                .andRespond(withSuccess("{\"code\":\"Ok\",\"durations\":[[0,1],[1,0]]}", MediaType.APPLICATION_JSON));

        client.getDurationMatrix(List.of(new Coordinate(0.00001, -0.00002), paris));

        server.verify();
    }

    @Test
    void singleCoordinateNeedsNoRequest(){
        DurationMatrix matrix = client.getDurationMatrix(List.of(london));

        assertEquals(1, matrix.size());
        assertEquals(0, matrix.get(0,0));
        server.verify(); // no expectations set, so any request would fail test
    }

    @Test
    void rejectsEmptyInput(){
        assertThrows(IllegalArgumentException.class, () -> client.getDurationMatrix(List.of()));
        assertThrows(IllegalArgumentException.class, () -> client.getDurationMatrix(null));
    }

    @Test
    void nullDurationMeansNoRoute(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andRespond(withSuccess("{\"code\":\"Ok\",\"durations\":[[0,null],[560.2,0]]}", MediaType.APPLICATION_JSON));

        assertThrows(NoRouteFoundException.class, () -> client.getDurationMatrix(List.of(london, paris)));
    }

    @Test
    void noSegmentErrorMeansNoRoute(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"NoSegment\",\"message\":\"Could not find a matching segment\"}"));

        assertThrows(NoRouteFoundException.class, () -> client.getDurationMatrix(List.of(london, paris)));
    }

    @Test
    void invalidQueryTreatedAsBackendProblem(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"InvalidQuery\",\"message\":\"Query string malformed\"}"));

        assertThrows(RoutingUnavailableException.class, () -> client.getDurationMatrix(List.of(london, paris)));
    }

    @Test
    void serverErrorMeansUnavailable(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andRespond(withServerError());

        assertThrows(RoutingUnavailableException.class, () -> client.getDurationMatrix(List.of(london, paris)));
    }

    @Test
    void connectionFailureMeansUnavailable(){
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andRespond(withException(new IOException("connection refused")));

        assertThrows(RoutingUnavailableException.class, () -> client.getDurationMatrix(List.of(london, paris)));
    }

    @Test
    void wrongSizedTableMeansUnavailable() {
        server.expect(requestTo(BASE + "/table/v1/driving/-0.127800,51.507400;2.352200,48.856600?annotations=duration"))
                .andRespond(withSuccess("{\"code\":\"Ok\",\"durations\":[[0]]}", MediaType.APPLICATION_JSON));

        assertThrows(RoutingUnavailableException.class, () -> client.getDurationMatrix(List.of(london, paris)));
    }
}