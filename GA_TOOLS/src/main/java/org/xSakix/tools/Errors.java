package org.xSakix.tools;

public class Errors {

    public static double leastSquareError(double expected[], double actual[]){
        double error = 0.0;
        for (int i = 0; i < expected.length; i++) {
            error += Math.pow(expected[i] - actual[i], 2.0);
        }
        error = 0.5 * error;

        return error;
    }

    public static double rootMeanSquareError(double error,double N){
        return Math.sqrt(2*error/N);
    }
}
