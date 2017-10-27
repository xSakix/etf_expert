package org.xSakix.particle.neural;

import cern.jet.random.Uniform;
import org.xSakix.tools.Errors;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetParticle {


    private class Node{
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
    }

    public class Net{
        List<List<Node>> net;

        Net(int inputs,int outputs,int ...hidenLayers){
            net = new ArrayList<>(hidenLayers.length+1);

            int in = inputs;

            for(int numOfNodes : hidenLayers){
                List<Node> layer = new ArrayList<>(numOfNodes);
                for(int i = 0; i < numOfNodes;i++){
                    layer.add(new Node(in));
                }
                in = numOfNodes;
                net.add(layer);
            }

            List<Node> layer = new ArrayList<>(outputs);
            for(int i = 0; i < outputs;i++){
                layer.add(new Node(in));
            }
            net.add(layer);
        }

        public double eval(double x[]){
            double in[] = Arrays.copyOf(x,x.length);

            for(List<Node> layer : net){
                double out[] = new double[layer.size()];
                for(int i = 0;i < layer.size();i++){
                    out[i] = layer.get(i).compute(in);
                }
                in = Arrays.copyOf(out,out.length);
            }

            //not general, expecting one output always
            return in[0];
        }

        public void setPw(){
            for(List<Node> layer : net){
                for(Node node :layer){
                    node.setPw();
                }
            }
        }

        public void setGw(Net otherNet) {
            for(int i = 0; i < net.size();i++){
                for(int j =0; j < net.get(i).size();j++){
                    this.net.get(i).get(j).setGw(otherNet.net.get(i).get(j).getW());
                }
            }
        }

        public void computeVelocity() {
            net.parallelStream().forEach(p -> p.parallelStream().forEach(pp-> pp.computeVelocity()));
        }

        public void computeWeights() {
            net.parallelStream().forEach(p -> p.parallelStream().forEach(pp-> pp.computeWeights()));
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("[");
            net.stream().forEach(layer -> {
                builder.append("[");
                layer.stream().forEach(node -> {
                    builder.append(Arrays.toString(node.w));
                    builder.append(",");
                });
                builder.append("]");
            });
            builder.append("]");

            return builder.toString();
        }
    }


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

    public double computeFitness(double x[][], double t[]){
        double y[] = new double[t.length];
        for(int i = 0;i < x.length;i++){
            y[i] = evaluate(x[i]);
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
