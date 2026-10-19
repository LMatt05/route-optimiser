package com.lukematthews.routeoptimiser.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DurationMatrixTest {
    private final double[][] valid = {
            {0, 10 ,20},
            {12, 0, 15},
            {25, 18, 0}
    };

    @Test
    void reutrnsValuesAndSize(){
        DurationMatrix matrix = new DurationMatrix(valid);
        assertEquals(3, matrix.size());
        assertEquals(10, matrix.get(0,1));
        assertEquals(12, matrix.get(1,0)); //asymmetry preserved
    }

    @Test
    void rejectsNullAndEmpty(){
        assertThrows(NullPointerException.class, () -> new DurationMatrix(null));
        assertThrows(IllegalArgumentException.class, () -> new DurationMatrix(new double[0][0]));
    }

    @Test
    void rejectsNonSquare(){
        double[][] ragged = {{0,1}, {1}};
        assertThrows(IllegalArgumentException.class, () -> new DurationMatrix(ragged));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidValues(double bad){
        double[][] data = {{0, bad}, {1,0}};
        assertThrows(IllegalArgumentException.class, () -> new DurationMatrix(data));
    }

    @Test
    void isNotAffectedByLaterChangesToInputArray(){
        double[][] input = {{0,5}, {5,0}};
        DurationMatrix matrix = new DurationMatrix(input);
        input[0][1] = 999;
        assertEquals(5, matrix.get(0,1));
    }

    @Test
    void pathDurationSumsConsecutiveLegs(){
        DurationMatrix matrix = new DurationMatrix(valid);
        assertEquals(10 + 15, matrix.pathDuration(List.of(0,1,2)));
        assertEquals(20+18, matrix.pathDuration(List.of(0,2,1)));
    }

    @Test
    void pathDurationOfSinglePointIsZero(){
        assertEquals(0, new DurationMatrix(valid).pathDuration(List.of(0)));
    }
}