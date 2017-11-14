package org.xSakix.tools.data;

import java.util.Arrays;

public class Normalizer {

    public static double[] normalize(double[] data, double norm) {
        return Arrays.stream(data).map(value -> value/norm).toArray();
    }

}
