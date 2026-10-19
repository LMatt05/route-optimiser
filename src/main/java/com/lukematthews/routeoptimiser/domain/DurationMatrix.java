package com.lukematthews.routeoptimiser.domain;

import java.util.List;
import java.util.Objects;

public final class DurationMatrix {
    private final double[][] durations;

    public DurationMatrix(double[][] durations){
        Objects.requireNonNull(durations, "durations must not be null");
        int n = durations.length;
        if (n == 0){
            throw new IllegalArgumentException("matrix must not be empty");
        }
        this.durations = new double[n][];
        for (int i = 0; i < n; i++){
            if(durations[i] == null || durations [i].length != n ){
                throw new IllegalArgumentException("matrix must be square (" + n + "x" + n + ")");
            }
            for (double value : durations[i]){
                if (Double.isNaN(value) || Double.isInfinite(value) || value < 0){
                    throw new IllegalArgumentException(
                            "durations must be finite and non-negative, but found " + value
                    );
                }
            }
            this.durations[i] = durations[i].clone();
        }
    }

    public int size() {
        return durations.length;
    }

    /** Travel time in seconds from point {@code from} to point {@code to}. */
    public double get(int from, int to){
        return durations[from][to];
    }

    /** Total travel time along the given visiting order (open path, no return leg). */
    public double pathDuration(List<Integer> order){
        double total = 0;
        for (int i = 0; i < order.size() - 1; i++) {
            total += get(order.get(i), order.get(i+1));
        }
        return total;
    }
}
