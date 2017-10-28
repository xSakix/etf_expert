package org.xSakix.particle.neural.quantum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuantumNet {
    List<List<QuantumNode>> net;

    public QuantumNet(int inputs, int outputs, int ...hidenLayers){
        net = new ArrayList<>(hidenLayers.length+1);

        int in = inputs;

        for(int numOfNodes : hidenLayers){
            List<QuantumNode> layer = new ArrayList<>(numOfNodes);
            for(int i = 0; i < numOfNodes;i++){
                layer.add(new QuantumNode(in));
            }
            in = numOfNodes;
            net.add(layer);
        }

        List<QuantumNode> layer = new ArrayList<>(outputs);
        for(int i = 0; i < outputs;i++){
            layer.add(new QuantumNode(in));
        }
        net.add(layer);
    }

    public QuantumNet(int inputs, double[] outputs, double[][]...hiddenLayers){
        net = new ArrayList<>(hiddenLayers.length+1);

        int in = inputs;

        for(double[][] wHiddenLayer : hiddenLayers){
            List<QuantumNode> layer = new ArrayList<>(wHiddenLayer.length);
            for(int i = 0; i < wHiddenLayer.length;i++){
                QuantumNode node = new QuantumNode(in);
                node.setW(wHiddenLayer[i]);
                layer.add(node);
            }
            in = wHiddenLayer.length;
            net.add(layer);
        }

        List<QuantumNode> layer = new ArrayList<>(1);
        QuantumNode node = new QuantumNode(in);
        node.setW(outputs);
        layer.add(node);
        net.add(layer);
    }

    public double eval(double x[]){
        double in[] = Arrays.copyOf(x,x.length);

        for(List<QuantumNode> layer : net){
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
        for(List<QuantumNode> layer : net){
            for(QuantumNode node :layer){
                node.setPw();
            }
        }
    }

    public void setGw(QuantumNet otherNet) {
        for(int i = 0; i < net.size();i++){
            for(int j =0; j < net.get(i).size();j++){
                this.net.get(i).get(j).setGw(otherNet.net.get(i).get(j).getW());
            }
        }
    }

    public void setC(QuantumNet otherNet) {
        for(int i = 0; i < net.size();i++){
            for(int j =0; j < net.get(i).size();j++){
                this.net.get(i).get(j).setC(otherNet.net.get(i).get(j).getC());
            }
        }
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

    public void addToC(QuantumNet otherNet) {
        for(int i = 0; i < net.size();i++){
            for(int j =0; j < net.get(i).size();j++){
                this.net.get(i).get(j).addToC(otherNet.net.get(i).get(j).getPw());
            }
        }
    }

    public void divCByParticlesSize(double size) {
        for(int i = 0; i < net.size();i++){
            for(int j =0; j < net.get(i).size();j++){
                this.net.get(i).get(j).divCBySize(size);
            }
        }
    }
}
