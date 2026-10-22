package com.lukematthews.routeoptimiser.solver;

import com.lukematthews.routeoptimiser.domain.DurationMatrix;

import java.util.List;

public interface RouteSolver {
    /**
     * Finds a visiting order for an open path(no return to the start).
     *
     * @param matrix        travel times between all points
     * @param startIndex    index of the point the path must begin at
     * @return a permutation of 0..n-1 that begins with {@code startIndex}
     * @throws IllegalArgumentException if {@code startIndex} is out of range
     */
    List<Integer> solve(DurationMatrix matrix, int startIndex);
}
