package com.lukematthews.routeoptimiser.solver;

import com.lukematthews.routeoptimiser.domain.DurationMatrix;

import java.util.ArrayList;
import java.util.List;

public class NearestNeighboutSolver implements RouteSolver{
    @Override
    public List<Integer> solve(DurationMatrix matrix, int startIndex){
        int n = matrix.size();
        if(startIndex < 0 || startIndex >= n){
            throw new IllegalArgumentException(
                    "startIndex " + startIndex + " is out of range for " + n + " points"
            );
        }
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);

        int current = startIndex;
        visited[current] = true;
        order.add(current);

        while(order.size() < n){
            int next = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int candidate = 0; candidate < n; candidate++){
                if(visited[candidate]){
                    continue;
                }
                double duration = matrix.get(current, candidate);
                if(duration<best){
                    best = duration;
                    next = candidate;
                }
            }
            visited[next] = true;
            order.add(next);
            current=next;
        }
        return order;
    }
}
