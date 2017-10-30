package org.xSakix.neuralnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Net {
    List<List<Node>> net;

    Net(double alpha,int inputs,int outputs,int ...hidenLayers){
        net = new ArrayList<>(hidenLayers.length+1);

        int in = inputs;

        for(int numOfNodes : hidenLayers){
            List<Node> layer = new ArrayList<>(numOfNodes);
            for(int i = 0; i < numOfNodes;i++){
                layer.add(new Node(in,alpha));
            }
            in = numOfNodes;
            net.add(layer);
        }

        List<Node> layer = new ArrayList<>(outputs);
        for(int i = 0; i < outputs;i++){
            layer.add(new Node(in,alpha));
        }
        net.add(layer);
    }

    public Net(double alpha,int inputs,double[] outputs, double[][]...hiddenLayers){
        net = new ArrayList<>(hiddenLayers.length+1);

        int in = inputs;

        for(double[][] wHiddenLayer : hiddenLayers){
            List<Node> layer = new ArrayList<>(wHiddenLayer.length);
            for(int i = 0; i < wHiddenLayer.length;i++){
                Node node = new Node(in,alpha);
                node.setW(wHiddenLayer[i]);
                layer.add(node);
            }
            in = wHiddenLayer.length;
            net.add(layer);
        }

        List<Node> layer = new ArrayList<>(1);
        Node node = new Node(in,alpha);
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


    public void backpropagate(double y,double t, double x[]) {


        for(int i = net.size()-1; i >= 0;i--){
            double dy = 0.;
            double outPrevious[] = null;
            if(i == 0){
                outPrevious = x;
            }else {
                outPrevious = collecPreviousLayerOutput(i);
            }
            for(int j = 0;j < net.get(i).size();j++){
                Node nodeIJ = net.get(i).get(j);

                if(i == net.size()-1){
                    dy = nodeIJ.computeOutputsDy(t,y);
                }else{
                    double weightsOut[] = new double[net.get(i+1).size()];
                    double dxOut[] = new double[net.get(i+1).size()];

                    for(int k = 0; k < net.get(i+1).size();k++){
                        weightsOut[k] = net.get(i+1).get(k).getW()[j];
                        dxOut[k] = net.get(i+1).get(k).getDx();
                    }
                    dy = nodeIJ.computeDy(weightsOut,dxOut);
                }
                nodeIJ.computeDetlaX(dy);
                nodeIJ.computeDw(outPrevious);
            }
        }
    }

    public double[] collecPreviousLayerOutput(int layer){
        assert layer > 0;

        double output[] = new double[net.get(layer-1).size()];
        for(int i = 0;i <net.get(layer-1).size();i++){
            output[i] = net.get(layer-1).get(i).out;
        }

        return output;
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
