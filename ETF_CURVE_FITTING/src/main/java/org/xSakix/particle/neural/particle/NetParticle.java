package org.xSakix.particle.neural.particle;

import org.xSakix.tools.Errors;

public class NetParticle {

    private Net net;
    private double fitness;
    private double lastFitness = Double.NaN;
    private double rms;

    public NetParticle(int inputs,int ...hidenLayers) {
        this.net = new Net(inputs,1,hidenLayers);
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

    public Net getGw(){
        return this.net;
    }

    public void setGw(Net otherNet){
        this.net.setGw(otherNet);
    }

    public void computeVelocity() {
        this.net.computeVelocity();
    }

    public void computeWeights() {
        this.net.computeWeights();
    }


}
