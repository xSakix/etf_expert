package org.xSakix.particle.neural.quantum;

import cern.jet.random.Uniform;

import java.util.Arrays;

public class QuantumNode {
    int inputs;
    double w[];
    private double Pw[];
    private double Gw[];
    private double C[];
    private double alpha;

    public QuantumNode(int inputs, double alpha) {
        this.inputs = inputs;
        w = new double[inputs];
        Pw = new double[inputs];
        Gw = new double[inputs];
        C= new double[inputs];

        for(int i = 0; i < inputs;i++){
            w[i]= Uniform.staticNextDoubleFromTo(-5.,5.);
            Pw[i]=w[i];
            Gw[i]=w[i];
        }
        //this.alpha = Uniform.staticNextDoubleFromTo(0.5,1.2);
        this.alpha = alpha;

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

    public void computeWeights(){
        for(int i = 0;i < w.length;i++){

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

    public void setPw() {
        this.Pw = Arrays.copyOf(w,w.length);
    }

    public double[] getPw() {
        return Pw;
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

    public void setC(double CC[]){
        this.C= Arrays.copyOf(CC,CC.length);
    }

    public double[] getC() {
        return C;
    }

    public void addToC(double[] pw) {
        for(int i = 0; i< C.length;i++){
            C[i]+= pw[i];
        }
    }

    public void divCBySize(double size) {
        for(int i = 0; i < C.length;i++){
            C[i] = C[i]/size;
        }
    }
}
