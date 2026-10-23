package com.lukematthews.routeoptimiser.service;

import com.lukematthews.routeoptimiser.config.RouteProperties;
import com.lukematthews.routeoptimiser.domain.*;
import com.lukematthews.routeoptimiser.routing.RoutingClient;
import com.lukematthews.routeoptimiser.solver.NearestNeighbourSolver;
import com.lukematthews.routeoptimiser.solver.RouteSolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RouteOptimisationServiceTest {
    private final Stop a = new Stop("A", new Coordinate(51.5, -0.1));
    private final Stop b = new Stop("B", new Coordinate(51.6,-0.2));
    private final Stop c = new Stop("C", new Coordinate(51.7,-0.3));

    private static DurationMatrix lineMatrix(double ... positions){
        int n = positions.length;
        double[][] d = new double [n][n];
        for(int i = 0; i < n; i++){
            for (int j = 0; j<0;j++){
                d[i][j] = Math.abs(positions[i] - positions[j]);
            }
        }
        return new DurationMatrix(d);
    }

    private static RouteOptimisationService serviceWith(RoutingClient client, RouteSolver solver, int maxStops){
        return new RouteOptimisationService(client, solver, new RouteProperties(maxStops));
    }

    @Test
    void returnsStopsInSolverOrderWithLegDurationsFromMatrix(){
        //positions: A=0, B=10, C=3
        RoutingClient client = coords -> lineMatrix(0,10,3);
        RouteSolver solver = (matrix, start) -> List.of(0,2,1); //A, C, B

        Route route = serviceWith(client, solver, 10).optimise(a, List.of(b,c));

        assertEquals(List.of(a,c,b), route.stops());
        assertEquals(2, route.legs().size());
        assertEquals(3,route.legs().get(0).durationSeconds()); // A -> C
        assertEquals(7, route.legs().get(1).durationSeconds()); // C -> B
        assertEquals(10, route.totalDurationSeconds());
    }

    @Test
    void sendsStartFirstThenDestinationsToRoutingClient(){
        List<List<Coordinate>> received = new ArrayList<>();
        RoutingClient client = coords ->{
            received.add(coords);
            return lineMatrix(0,1,2);
        };

        serviceWith(client, new NearestNeighbourSolver(), 10).optimise(a, List.of(b,c));

        assertEquals(1, received.size());
        assertEquals(List.of(a.coordinate(), b.coordinate(), c.coordinate()), received.get(0));
    }

    @Test
    void asksSolverToStartAtIndexZero(){
        List<Integer> startIndexes = new ArrayList<>();
        RouteSolver solver = (matrix, start) -> {
            startIndexes.add(start);
            return List.of(0,1,2);
        };
        serviceWith(coords -> lineMatrix(0,1,2), solver, 10).optimise(a, List.of(b,c));

        assertEquals(List.of(0), startIndexes);
    }

    @Test
    void worksWithRealSolver(){
        //positions: A = 0, B=5, C=1, D=2 -> nearest first from A goes C,D,B
        Stop d = new Stop("D", new Coordinate(51.8, -0.4));
        RoutingClient client = coords -> lineMatrix(0,5,1,2);

        Route route = serviceWith(client, new NearestNeighbourSolver(), 10).optimise(a, List.of(b,c,d));

        assertEquals(List.of(a,c,d,b), route.stops());
        assertEquals(5, route.totalDurationSeconds()); // 1+1+3
    }

    @Test
    void acceptsExactlyMaximumNumberStops(){
        RoutingClient client = coords -> lineMatrix(0,1,2);

        assertDoesNotThrow(()->
                serviceWith(client, new NearestNeighbourSolver(), 3).optimise(a,List.of(b,c)));
    }

    @Test
    void rejectsTooManyStopsWithoutCallingOsrm(){
        AtomicInteger calls = new AtomicInteger();
        RoutingClient client = coords->{
            calls.incrementAndGet();
            return lineMatrix(0,1,2,3);
        };
        RouteOptimisationService service = serviceWith(client, new NearestNeighbourSolver(), 3);

        Stop d = new Stop("D", new Coordinate(51.8,-0.4));
        assertThrows(IllegalArgumentException.class, () -> service.optimise(a,List.of(b,c,d)));
        assertEquals(0, calls.get());
    }

    @Test
    void rejectsEmptyNullInputs(){
        RouteOptimisationService service =
                serviceWith(coords->lineMatrix(0), new NearestNeighbourSolver(), 10);

        assertThrows(IllegalArgumentException.class, () -> service.optimise(a, List.of()));
        assertThrows(NullPointerException.class, () -> service.optimise(null, List.of(b)));
        assertThrows(NullPointerException.class, () -> service.optimise(a, null));

        List<Stop> withNull = new ArrayList<>();
        withNull.add(b);
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> service.optimise(a, withNull));
    }
    @Test
    void routingFailuresPassThroughUnchanged() {
        RoutingClient noRoute = coords -> { throw new NoRouteFoundException("islands"); };
        RoutingClient down = coords -> { throw new RoutingUnavailableException("osrm down"); };

        assertThrows(NoRouteFoundException.class,
                () -> serviceWith(noRoute, new NearestNeighbourSolver(), 10).optimise(a, List.of(b)));
        assertThrows(RoutingUnavailableException.class,
                () -> serviceWith(down, new NearestNeighbourSolver(), 10).optimise(a, List.of(b)));
    }

    @Test
    void detectsInvalidOrderFromSolver() {
        RoutingClient client = coords -> lineMatrix(0, 1, 2);

        RouteSolver wrongSize = (m, s) -> List.of(0, 1);
        RouteSolver wrongStart = (m, s) -> List.of(1, 0, 2);
        RouteSolver duplicate = (m, s) -> List.of(0, 1, 1);

        for (RouteSolver bad : List.of(wrongSize, wrongStart, duplicate)) {
            assertThrows(IllegalStateException.class,
                    () -> serviceWith(client, bad, 10).optimise(a, List.of(b, c)));
        }
    }

    @Test
    void detectsMatrixOfWrongSize() {
        RoutingClient client = coords -> lineMatrix(0, 1);   // 2 points for 3 stops

        assertThrows(IllegalStateException.class,
                () -> serviceWith(client, new NearestNeighbourSolver(), 10).optimise(a, List.of(b, c)));
    }
}