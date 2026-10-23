package com.lukematthews.routeoptimiser.solver;

import com.lukematthews.routeoptimiser.domain.DurationMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class NearestNeighbourSolverTest {
    private final RouteSolver solver = new NearestNeighbourSolver();

    /**Builds a symmetric matrix for points on a line, so distances are easy to reason about. */
    private static DurationMatrix lineMatrix(double ... positions){
        int n = positions.length;
        double[][] d = new double[n][n];
        for (int i = 0; i<n;i++){
            for (int j = 0; j < n; j++){
                d[i][j] = Math.abs(positions[i] - positions[j]);
            }
        }
        return new DurationMatrix(d);
    }

    @Test
    void singlePointReturnsJustStart(){
        assertEquals(List.of(0), solver.solve(lineMatrix(0),0));
    }

    @Test
    void visitsPointsInOrderOfProximityOnLine(){
        // points at 0, 1, 3, 6: from 0 the nearest-first order is left to right
        assertEquals(List.of(0,1,2,3), solver.solve(lineMatrix(0,1,3,5),0));
    }

    @Test
    void beginsAtRequestedStartIndex(){
        List<Integer> order = solver.solve(lineMatrix(0,1,3,6),2);
        assertEquals(2, order.get(0));
        assertEquals(List.of(2,1,0,3), order); //1 is 2 away, 3 is 3 away, 0 then 3
    }

    @Test
    void breaksTiesByLowestIndex(){
        //points at -1, 0, 1: both neighbours of the start (index 1) are distance 1
        assertEquals(List.of(1,0,2), solver.solve(lineMatrix(-1,0,1),1));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3})
    void rejectsOutOfRangeStart(int bad){
        assertThrows(IllegalArgumentException.class, () -> solver.solve(lineMatrix(0,1,2), bad));
    }

    @Test
    void alwaysReturnsPermutationBeginningAtStart() {
        Random random = new Random(42);
        for (int trial = 0; trial < 50; trial++) {
            int n = 2 + random.nextInt(15);
            double[][] d = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = i == j ? 0 : 1 + random.nextInt(100);
                }
            }
            int start = random.nextInt(n);

            List<Integer> order = solver.solve(new DurationMatrix(d), start);

            assertEquals(n, order.size());
            assertEquals(start, order.get(0));
            assertEquals(n, new HashSet<>(order).size(), "no point visited twice");
        }
    }

    @Test
    void greedyChoiceIsNotAlwaysOptimal(){
        //positions: start 0, A 10, B -11, C25
        DurationMatrix matrix = lineMatrix(0,10,-11,25);
        List<Integer> greedy = solver.solve(matrix, 0);

        assertEquals(List.of(0,1,3,2),greedy);
        assertEquals(61, matrix.pathDuration(greedy));
        // best order, 0 -> B -> A -> C, costs 47
        assertEquals(47, matrix.pathDuration(List.of(0,2,1,3)));
    }
}