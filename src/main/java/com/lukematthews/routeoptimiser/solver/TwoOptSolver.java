package com.lukematthews.routeoptimiser.solver;

import com.lukematthews.routeoptimiser.domain.DurationMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TwoOptSolver implements RouteSolver{
    /**Ignore "improvements" smaller than floating-point noise, guarantees termintation. */
    private static final double EPSILON = 1e-9;

    private final RouteSolver initialSolver;

    public TwoOptSolver(){
        this(new NearestNeighboutSolver());
    }
    public TwoOptSolver(RouteSolver initialSolver){
        this.initialSolver = Objects.requireNonNull(initialSolver, "initialSolver must not be null");
    }

    @Override
    public List<Integer> solve(DurationMatrix matrix, int startIndex){
        List<Integer> order = new ArrayList<>(initialSolver.solve(matrix, startIndex));
        if(order.isEmpty() || order.get(0) != startIndex){
            throw new IllegalStateException("initial solver must return an order beginning at startIndex");
        }
        while (applyFirstImprovingReversal(matrix, order)){
            //keep going until no single reversal shortens the path
        }
        return order;
    }

    /**
     * Looks for a segment order [i..j] whose reversal shortens the path, applies the first one
     * found and returns true. Position 0 (the start) is never moved, so i begins at 1.
     */
    private boolean applyFirstImprovingReversal(DurationMatrix m, List<Integer> order){
        int n = order.size();
        for (int i= 1; i<n-1; i++){
            int before = order.get(i-1);
            int first = order.get(i);
            double forward = 0; //cost of the segment's internal legs as currently ordered
            double reverse = 0; //cost of the same legs travelled in the opposite direction

            for (int j = i+1; j<n; j++){
                int prev = order.get(j - 1);
                int last = order.get(j);
                forward += m.get(prev, last);
                reverse += m.get(last, prev);

                double oldCost = m.get(before, first) + forward;
                double newCost = m.get(before, last) + reverse;

                if (j< n - 1){ //there is a point after the segment
                    int after = order.get(j + 1);
                    oldCost += m.get(last, after);
                    newCost += m.get(first, after);
                } // else: the path ends, no closing edge
                if (newCost<oldCost - EPSILON){
                    Collections.reverse(order.subList(i, j+1));
                    return true;
                }
            }
        }
        return false;
    }
}
