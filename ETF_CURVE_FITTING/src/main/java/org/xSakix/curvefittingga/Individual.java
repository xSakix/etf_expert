package org.xSakix.curvefittingga;

import cern.jet.random.Uniform;
import org.xSakix.tools.Errors;

import java.util.Arrays;

public class Individual {

    private double w[];
    private int M;
    private double fitness;
    private double rms;

    public Individual(int m) {
        M = m;
        this.w = new double[M];
        for(int i = 0; i < M;i++){
            w[i] = Uniform.staticNextDoubleFromTo(-5.,5.);
            //w[i] = Uniform.staticNextDoubleFromTo(0.,1.);
        }
        //w[0] = 0.;
    }

    public Individual(int m,double min, double max) {
        M = m;
        this.w = new double[M];
        for(int i = 0; i < M;i++){
            w[i] = Uniform.staticNextDoubleFromTo(min,max);
            //w[i] = Uniform.staticNextDoubleFromTo(0.,1.);
        }
        //w[0] = 0.;
    }

    public double evaluate(double x){

        double y = w[M-1];
        for (int i = M-2;i >= 0;i--){
            y = y*x+w[i];
        }
//        double y = 0.;
//        for(int i = 0; i < M;i++){
//            y += w[i]*Math.pow(x,i);
//        }

        return y;
    }

    public double computeFitness(double x[], double t[]){
        double y[] = new double[x.length];
        for(int i = 0;i < x.length;i++){
            y[i] = evaluate(x[i]);
        }
        fitness = Errors.leastSquareError(t,y);
        rms = Errors.rootMeanSquareError(fitness,x.length);
        return fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public double[] getW() {
        return w;
    }

    public void setW(double[] ww) {
        this.w = Arrays.copyOf(ww,ww.length);
    }

    public double getRms() {
        return rms;
    }
}
