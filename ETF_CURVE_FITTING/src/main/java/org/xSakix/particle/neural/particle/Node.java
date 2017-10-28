package org.xSakix.particle.neural.particle;

import cern.jet.random.Uniform;

import java.util.Arrays;

public class Node {
    double constriction;
    int inputs;
    double w[];
    private double Pw[];
    private double Gw[];
    private double V[];
    private double inertia;
    private double c1,c2;

    public Node(int inputs) {
        this.inputs = inputs;
        w = new double[inputs];
        Pw = new double[inputs];
        Gw = new double[inputs];
        V = new double[inputs];
        for(int i = 0; i < inputs;i++){
            w[i]= Uniform.staticNextDoubleFromTo(-5.,5.);
            V[i] = Uniform.staticNextDoubleFromTo(-5.,5.);
            Pw[i]=w[i];
            Gw[i]=w[i];
        }
        this.inertia = Uniform.staticNextDoubleFromTo(0.,1.);
        this.c1 = Uniform.staticNextDoubleFromTo(2.,3.);
        this.c2 = Uniform.staticNextDoubleFromTo(2.,3.);
        double phi = c1+c2;
        this.constriction = 2./(Math.abs(2.-phi-Math.sqrt(Math.pow(phi,2)-4.*phi)));

    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public double compute(double x[]){
        assert x.length == this.inputs;
        double sum = 0.;
        for(int i = 0;i < inputs;i++){
            sum += w[i]*x[i];
        }
        return sigmoid(sum);
    }

    public void computeVelocity(){
        for(int i = 0;i < inputs;i++){
            double p1 = c1*Uniform.staticNextDoubleFromTo(0.,1.)*(Pw[i] - w[i]);
            double p2 = c2*Uniform.staticNextDoubleFromTo(0.,1.)*(Gw[i] - w[i]);
            //V[i] = inertia*V[i]+p1+p2; //PSO-IN
            V[i] = constriction*(V[i]+p1+p2); //PSO-CO
        }
    }

    public void computeWeights(){
        for(int i = 0;i < inputs;i++){
            w[i] = w[i]+V[i];
        }
    }

    public void setPw() {
        this.Pw = Arrays.copyOf(w,w.length);
    }

    public double[] getW() {
        return this.w;
    }

    public void setGw(double[] w) {
        this.Gw = Arrays.copyOf(w,w.length);
    }

    public void setW(double ww[]){
        this.w = Arrays.copyOf(ww,ww.length);
    }
}
