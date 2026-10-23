package com.lukematthews.routeoptimiser.solver;

import com.lukematthews.routeoptimiser.domain.DurationMatrix;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.lukematthews.routeoptimiser.testsupport.TestMatrices.line;
import static org.junit.jupiter.api.Assertions.*;

class TwoOptSolverTest {
    @Test
    void fixesCrossingInMiddleOfPath(){
        DurationMatrix matrix = line(0,1,2,3);
        RouteSolver badStart = (m,s) -> List.of(0,2,1,3); //cost 5
        List<Integer> result = new TwoOptSolver(badStart).solve(matrix,0);

        assertEquals(List.of(0,1,2,3), result);
        assertEquals(3, matrix.pathDuration(result));
    }

    @Test
    void reversesTailWhenPathEndsInSegment(){
        //index -> position: 0->, 1->3, 2->2, 3->1
        DurationMatrix matrix = line(0,3,2,1);
        RouteSolver badStart = (m,s) -> List.of(0,1,2,3); // cost 5

        List<Integer> result = new TwoOptSolver(badStart).solve(matrix, 0);

        assertEquals(List.of(0,3,2,1), result);
        assertEquals(3, matrix.pathDuration(result));
    }

    @Test
    void leavesAlreadyOptimalPathUnchanged(){
        DurationMatrix matrix = line(0,1,2,3);
        RouteSolver optimal = (m, s) -> List.of(0,1,2,3);
        assertEquals(List.of(0,1,2,3), new TwoOptSolver(optimal).solve(matrix, 0));
    }

    @Test
    void handlesTinyInputs(){
        assertEquals(List.of(0), new TwoOptSolver().solve(line(5), 0));
        assertEquals(List.of(1,0), new TwoOptSolver().solve(line(5,9),1));
    }

    @Test
    void rejectsInitialSolverThatIgnoresStart(){
        RouteSolver broken = (m,s) -> List.of(1,0,2);
        assertThrows(IllegalStateException.class, () -> new TwoOptSolver(broken).solve(line(0,1,2), 0));
    }

    @Test
    void neverWorsensAndEndsAtLocalOptimumOnRandomAsymmetricMatrices(){
        Random random = new Random(7);
        for (int trial = 0; trial < 100; trial++){
            int n = 2 + random.nextInt(12);
            double [][] d = new double [n][n];
            for (int i = 0; i < n; i++){
                for (int j = 0; j < n; j++){
                    d[i][j] = i == j ? 0 : 1 + random.nextInt(100); //asymmetric
                }
            }
            DurationMatrix matrix = new DurationMatrix(d);
            int start = random.nextInt(n);

            List<Integer> initial = new NearestNeighbourSolver().solve(matrix, start);
            List<Integer> result = new TwoOptSolver().solve(matrix, start);

            //valid permutation beginning at start
            assertEquals(n, result.size());
            assertEquals(start, result.get(0));
            assertEquals(n, new HashSet<>(result).size());

            //never worse than what it started from
            assertTrue(matrix.pathDuration(result) <= matrix.pathDuration(initial) + 1e-9);

            //local optimum: no single reversal of any segment (start excluded) shortens it
            double cost = matrix.pathDuration(result);
            for(int i = 1; i < n; i++){
                for(int j = i + 1; j < n; j++){
                    List<Integer> candidate = new ArrayList<>(result);
                    Collections.reverse(candidate.subList(i, j + 1));
                    assertTrue(matrix.pathDuration(candidate) >= cost - 1e-9,
                            "reversing " + i + ".." + j + " would improve " + result);
                }
            }
        }
    }
}