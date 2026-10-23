package com.lukematthews.routeoptimiser.testsupport;

import com.lukematthews.routeoptimiser.domain.DurationMatrix;

public class TestMatrices {
    private TestMatrices(){
    }

    public static DurationMatrix line (double... positions){
        int n = positions.length;
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = Math.abs(positions[i] - positions[j]);
            }
        }
        return new DurationMatrix(d);
    }
}
