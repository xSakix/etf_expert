package org.xSakix.particle.neural.particle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Net {
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

    public Net(int inputs,double[] outputs, double[][]...hiddenLayers){
        net = new ArrayList<>(hiddenLayers.length+1);

        int in = inputs;

        for(double[][] wHiddenLayer : hiddenLayers){
            List<Node> layer = new ArrayList<>(wHiddenLayer.length);
            for(int i = 0; i < wHiddenLayer.length;i++){
                Node node = new Node(in);
                node.setW(wHiddenLayer[i]);
                layer.add(node);
            }
            in = wHiddenLayer.length;
            net.add(layer);
        }

        List<Node> layer = new ArrayList<>(1);
        Node node = new Node(in);
        node.setW(outputs);
        layer.add(node);
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
