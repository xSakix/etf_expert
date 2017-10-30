package org.xSakix.particle.neural.quantum;

import org.xSakix.tools.Errors;

public class QuantumNetParticle {

    private QuantumNet net;
    private double fitness;
    private double lastFitness = Double.NaN;
    private double rms;

    public QuantumNetParticle(double alpha,int inputs, int ...hidenLayers) {
        this.net = new QuantumNet(alpha,inputs,1,hidenLayers);
    }


    public double evaluate(double x[]){
        return net.eval(x);
    }

    public double computeFitness(double x[][], double t[], double norm){
        double y[] = new double[t.length];
        for(int i = 0;i < x.length;i++){
            y[i] = evaluate(x[i])*norm;
        }
        fitness = Errors.leastSquareError(t,y);
        rms = Errors.rootMeanSquareError(fitness,x.length);

        if(Double.isNaN(lastFitness)){
            lastFitness = fitness;
        }
        if(fitness < lastFitness){
            net.setPw();
        }

        return fitness;
    }

    public double computeFitness(double x[][], double t[]){
       return computeFitness(x,t,1.);
    }

    public double getFitness() {
        return fitness;
    }

    public double getRms() {
        return rms;
    }

    public QuantumNet getGw(){
        return this.net;
    }

    public void setGw(QuantumNet otherNet){
        this.net.setGw(otherNet);
    }

    public void computeWeights() {
        this.net.computeWeights();
    }


    public QuantumNet getNet() {
        return net;
    }

    public void setC(QuantumNet sumNet) {
        this.net.setC(sumNet);
    }
}
