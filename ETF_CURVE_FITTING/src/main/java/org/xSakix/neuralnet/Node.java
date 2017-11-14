package org.xSakix.neuralnet;

import cern.jet.random.Uniform;

import java.util.Arrays;

public class Node {
    int inputs;
    double w[];
    double out;
    double alpha;// = Uniform.staticNextDoubleFromTo(0.1,0.5);
    double momentum;// = Uniform.staticNextDoubleFromTo(0.,0.5);
    private double dx = Double.NaN;
    private double[] dwLast;
    private double[] dw;

    public Node(int inputs,double alpha, double momentum) {
        this.inputs = inputs;
        w = new double[inputs];
        dw = new double[inputs];
        dwLast = new double[inputs];
        for(int i = 0; i < inputs;i++){
            w[i]= Uniform.staticNextDoubleFromTo(-5.,5.);
        }
        this.alpha = alpha;

    }

    public void initWeights(double min, double max){
        for(int i = 0; i < inputs;i++){
            //w[i]= 0.5*(min+max);
            w[i] = Uniform.staticNextDoubleFromTo(-max,max);
        }
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

   // private double dsigmoid(double x) {return sigmoid(x)*(1.-sigmoid(x));}

    public double compute(double x[]){
        assert x.length == this.inputs;
        double sum = 0.;
        for(int i = 0;i < inputs;i++){
            sum += w[i]*x[i];
        }
        out = sigmoid(sum);
        return out;
    }

    public double computeOutputsDy(double d, double y){
        return d-y;
    }

    public double computeDy(double weightsOut[],double dxOut[]){
        double dy =0.;

        for(int i = 0; i < weightsOut.length;i++){
            dy+= dxOut[i]*weightsOut[i];
        }

        return dy;
    }

    public void computeDetlaX(double deltaY){

        this.dx =out*(1.-out)*deltaY;
    }

    public void computeDw(double outPreviousLayer[]){
        this.dwLast = Arrays.copyOf(this.dw,this.dw.length);
        for(int i = 0;i < inputs;i++){
            this.dw[i] = alpha*dx*outPreviousLayer[i]+momentum*dwLast[i];
        }

    }

    public void computeWeights(){
        for (int i = 0; i < w.length;i++){
            w[i]+=dw[i];
        }
    }

    public double[] getW() {
        return this.w;
    }

    public void setW(double ww[]){
        this.w = Arrays.copyOf(ww,ww.length);
    }

    public double getDx() {
        return dx;
    }
}
