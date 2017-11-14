package org.xSakix.tools.data;

import cern.jet.random.Uniform;

public class OverSampler {

    public static double[] oversample(double data[], int factor) {
        double sample[] = new double[data.length * factor];
        for (int i = 0; i < data.length - 1; i++) {
            sample[i * factor] = data[i];
            sample[(i + 1) * factor] = data[i + 1];
            for (int j = 1; j < factor; j++) {
                sample[(i * factor) + j] = Uniform.staticNextDoubleFromTo(data[i], data[i + 1]);
            }
        }
        return sample;
    }
}
