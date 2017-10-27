package org.xSakix.particle;

import cern.jet.random.Uniform;
import org.xSakix.tools.Errors;

import java.util.Arrays;

public class Particle {

    private double w[];
    private double Pw[];
    private double Gw[];
    private double V[];
    private double inertia;
    private double c1,c2;
    private int M;
    private double fitness;
    private double lastFitness = Double.NaN;
    private double rms;
    private double constriction;

    public Particle(int m) {
        init(m,-5.,5.);
    }

    public Particle(int m, double min, double max) {
        init(m, min, max);
    }

    private void init(int m, double min, double max) {
        M = m;
        this.w = new double[M];
        this.V = new double[M];
        this.Pw = new double[M];
        this.Gw = new double[M];
        for(int i = 0; i < M;i++){
            w[i] = Uniform.staticNextDoubleFromTo(min,max);
            V[i] = Uniform.staticNextDoubleFromTo(min,max);
            Pw[i] = w[i];
            Gw[i] = w[i];
        }
        this.inertia = Uniform.staticNextDoubleFromTo(0.,1.);
        this.c1 = Uniform.staticNextDoubleFromTo(2.,3.);
        this.c2 = Uniform.staticNextDoubleFromTo(2.,3.);
        double phi = c1+c2;
        this.constriction = 2./(Math.abs(2.-phi-Math.sqrt(Math.pow(phi,2)-4.*phi)));
    }

    public void computeVelocity(){
        for(int i = 0;i < M;i++){
            double p1 = c1*Uniform.staticNextDoubleFromTo(0.,1.)*(Pw[i]-w[i]);
            double p2 = c2*Uniform.staticNextDoubleFromTo(0.,1.)*(Gw[i] - w[i]);
            //V[i] = inertia*V[i]+p1+p2; //PSO-IN
            V[i] = constriction*(V[i]+p1+p2); //PSO-CO
        }
    }

    public void computeWeights(){
        for(int i = 0;i < M;i++){
            w[i] = w[i]+V[i];
        }
    }

    public double evaluate(double x){

        double y = w[M-1];
        for (int i = M-2;i >= 0;i--){
            y = y*x+w[i];
        }

        return y;
    }

    public double computeFitness(double x[], double t[]){
        double y[] = new double[x.length];
        for(int i = 0;i < x.length;i++){
            y[i] = evaluate(x[i]);
        }
        fitness = Errors.leastSquareError(t,y);
        rms = Errors.rootMeanSquareError(fitness,x.length);

        if(Double.isNaN(lastFitness)){
            lastFitness = fitness;
        }
        if(fitness < lastFitness){
            Pw = Arrays.copyOf(w,w.length);
        }

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

    public double[] getPw() {
        return Pw;
    }

    public void setPw(double[] pw) {
        Pw = Arrays.copyOf(pw,pw.length);
    }

    public double[] getGw() {
        return Gw;
    }

    public void setGw(double[] gw) {
        Gw = Arrays.copyOf(gw,gw.length);
    }

    public double getRms() {
        return rms;
    }
}
