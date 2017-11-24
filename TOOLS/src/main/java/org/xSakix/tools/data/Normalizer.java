package org.xSakix.tools.data;

import java.util.Arrays;

public class Normalizer {

    public static double[] normalize(double[] data, double norm) {
        return Arrays.stream(data).map(value -> value/norm).toArray();
    }

    public static double[] normalizeMinMax(double[] data) {
        double min = Arrays.stream(data).min().getAsDouble();
        double max = Arrays.stream(data).max().getAsDouble();
        return Arrays.stream(data).map(value -> (value-min)/(max-min)).toArray();
    }

}
