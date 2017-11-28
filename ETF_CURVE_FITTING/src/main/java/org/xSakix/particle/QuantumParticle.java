package org.xSakix.particle;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import org.xSakix.tools.Errors;

import java.util.Arrays;

public class QuantumParticle {

    private double w[];
    private double Pw[];
    private double Gw[];
    private double C[];
    private int M;
    private double fitness;
    private double lastFitness = Double.NaN;
    private double rms;
    private double alpha;

    public QuantumParticle(int m) {
        init(m,-5.,5.);
    }

    public QuantumParticle(int m, double min, double max) {
        init(m, min, max);
    }

    private void init(int m, double min, double max) {
        M = m;
        this.w = new double[M];
        this.Pw = new double[M];
        this.Gw = new double[M];
        this.C = new double[M];
        for(int i = 0; i < M;i++){
            w[i] = Uniform.staticNextDoubleFromTo(min,max);
            Pw[i] = w[i];
            Gw[i] = w[i];
        }
        //this.alpha = Uniform.staticNextDoubleFromTo(0.,1.);
        this.alpha = 0.75;
        //this.alpha = Normal.staticNextDouble(0.5,1.);

    }

    public void computeWeights(){
        for(int i = 0;i < M;i++){
            double phi = Uniform.staticNextDoubleFromTo(.0,1.);
            double p = phi*Pw[i]+(1-phi)*Gw[i];
            double u = Uniform.staticNextDoubleFromTo(0.,1.);
            if(Uniform.staticNextDoubleFromTo(0.,1.) < 0.5){
                w[i] = p+alpha*Math.abs(w[i]-C[i])*Math.log(1/u);
            }else{
                w[i] = p-alpha*Math.abs(w[i]-C[i])*Math.log(1/u);
            }
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

    public void setC(double CC[]){
        this.C= Arrays.copyOf(CC,CC.length);
    }
}
