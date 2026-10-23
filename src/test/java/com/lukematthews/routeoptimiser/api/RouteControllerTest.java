package com.lukematthews.routeoptimiser.api;

import com.lukematthews.routeoptimiser.api.exception.GlobalExceptionHandler;
import com.lukematthews.routeoptimiser.config.RouteProperties;
import com.lukematthews.routeoptimiser.domain.NoRouteFoundException;
import com.lukematthews.routeoptimiser.domain.RoutingUnavailableException;
import com.lukematthews.routeoptimiser.routing.RoutingClient;
import com.lukematthews.routeoptimiser.service.RouteOptimisationService;
import com.lukematthews.routeoptimiser.solver.NearestNeighbourSolver;
import com.lukematthews.routeoptimiser.testsupport.TestMatrices;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RouteControllerTest {
    private static final String URL = "/api/v1/routes/optimise";

    private static final String VALID_REQUEST = """
            {
              "start": {"label": "Depot", "latitude": 51.5074, "longitude": -0.1278},
              "destinations": [
                {"label": "B", "latitude": 51.6, "longitude": -0.2},
                {"label": "C", "latitude": 51.7, "longitude": -0.3}
              ]
            }
            """;

    private static final RoutingClient NEVER_CALLED = coords -> {
        throw new AssertionError("routing client should not be called");
    };

    private MockMvc mockMvcWith(RoutingClient client) {
        RouteOptimisationService service = new RouteOptimisationService(
                client, new NearestNeighbourSolver(), new RouteProperties(5));
        return MockMvcBuilders.standaloneSetup(new RouteController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ResultActions send(MockMvc mvc, String json) throws Exception {
        return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Sends the request, expects a 400 error format, and returns the response body. */
    private String badRequestBody(String json) throws Exception {
        return send(mockMvcWith(NEVER_CALLED), json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String requestWithDestinations(int count) {
        String destinations = IntStream.range(0, count)
                .mapToObj(i -> "{\"latitude\": 51.5, \"longitude\": -0.1}")
                .collect(Collectors.joining(","));
        return "{\"start\": {\"latitude\": 51.5, \"longitude\": -0.1}, \"destinations\": [" + destinations + "]}";
    }

    @Test
    void returnsTheOptimisedRoute() throws Exception {
        // positions: Depot=0, B=10, C=3, so nearest-first visits C before B
        MockMvc mvc = mockMvcWith(coords -> TestMatrices.line(0, 10, 3));

        send(mvc, VALID_REQUEST)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stops.length()").value(3))
                .andExpect(jsonPath("$.stops[0].label").value("Depot"))
                .andExpect(jsonPath("$.stops[1].label").value("C"))
                .andExpect(jsonPath("$.stops[1].latitude").value(51.7))
                .andExpect(jsonPath("$.stops[2].label").value("B"))
                .andExpect(jsonPath("$.legs.length()").value(2))
                .andExpect(jsonPath("$.legs[0].fromIndex").value(0))
                .andExpect(jsonPath("$.legs[0].toIndex").value(1))
                .andExpect(jsonPath("$.legs[0].durationSeconds").value(3.0))
                .andExpect(jsonPath("$.legs[1].durationSeconds").value(7.0))
                .andExpect(jsonPath("$.totalDurationSeconds").value(10.0));
    }

    @Test
    void labelsAreOptionalAndOmittedFromTheResponseWhenAbsent() throws Exception {
        String json = """
                {"start": {"latitude": 51.5, "longitude": -0.1},
                 "destinations": [{"latitude": 51.6, "longitude": -0.2}]}
                """;

        send(mockMvcWith(coords -> TestMatrices.line(0, 1)), json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stops[0].label").doesNotExist());
    }


    @Test
    void missingStartIsRejected() throws Exception {
        String body = badRequestBody("""
                {"destinations": [{"latitude": 51.6, "longitude": -0.2}]}
                """);
        assertTrue(body.contains("start"), body);
    }

    @Test
    void emptyDestinationsAreRejected() throws Exception {
        String body = badRequestBody("""
                {"start": {"latitude": 51.5, "longitude": -0.1}, "destinations": []}
                """);
        assertTrue(body.contains("destinations"), body);
    }

    @Test
    void latitudeOutOfRangeIsRejectedAndNamesTheField() throws Exception {
        String body = badRequestBody("""
                {"start": {"latitude": 95, "longitude": -0.1},
                 "destinations": [{"latitude": 51.6, "longitude": -0.2}]}
                """);
        assertTrue(body.contains("start.latitude"), body);
    }

    @Test
    void missingLongitudeIsRejectedRatherThanDefaultingToZero() throws Exception {
        String body = badRequestBody("""
                {"start": {"latitude": 51.5, "longitude": -0.1},
                 "destinations": [{"latitude": 51.6}]}
                """);
        assertTrue(body.contains("destinations[0].longitude"), body);
    }

    @Test
    void malformedJsonIsRejected() throws Exception {
        badRequestBody("{\"start\": ");
    }

    @Test
    void tooManyStopsIsRejectedWithoutCallingOsrm() throws Exception {
        // limit is 5 including the start, start + 5 destinations is one too many
        String body = badRequestBody(requestWithDestinations(5));
        assertTrue(body.contains("too many stops"), body);
    }

    @Test
    void noRouteReturns422() throws Exception {
        MockMvc mvc = mockMvcWith(coords -> {
            throw new NoRouteFoundException("No route between point 0 and point 1");
        });

        send(mvc, VALID_REQUEST)
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("No route between point 0 and point 1"));
    }

    @Test
    void unavailableRoutingReturns502WithoutLeakingInternalDetails() throws Exception {
        MockMvc mvc = mockMvcWith(coords -> {
            throw new RoutingUnavailableException("connect to http://osrm:5000 failed");
        });

        send(mvc, VALID_REQUEST)
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("The routing service is currently unavailable"));
    }

    @Test
    void internalInconsistencyReturnsGeneric500() throws Exception {
        MockMvc mvc = mockMvcWith(coords -> TestMatrices.line(0, 1));   // wrong size for 3 stops

        send(mvc, VALID_REQUEST)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}