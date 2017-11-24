package org.xSakix.neuralnet;

import cern.colt.list.DoubleArrayList;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.nn.Net;
import org.xSakix.nn.NetConfig;
import org.xSakix.nn.WhichFunctionEnum;
import org.xSakix.tools.Errors;
import org.xSakix.tools.data.Normalizer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ETFFitter {

    public static final int ITER_MAX = 1000;
    public static final int M = 4;
    private static final double ALPHA = 0.03;
    private static final double MOMENTUM = 0.9;



    public static void main(String[] args) throws IOException {
        System.out.println("Alpha= " + ALPHA);

        double orig_data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");

        double norm = 1000.;
        double data[] = Normalizer.normalize(orig_data, norm);
        double data2[] = Arrays.copyOfRange(data,data.length/2,data.length);
        data = Arrays.copyOfRange(data,0,data.length/2);
        //double data[] = Normalizer.normalizeMinMax(orig_data);

        List<double[]> xx = new ArrayList<>();
        DoubleArrayList tt = new DoubleArrayList();
        for (int i = 0; i < data.length - M; i++) {
            double in[] = new double[M];
            double x[] = new double[M];
            for (int j = 0; j < M; j++) {
                x[j] = data[i + j];
            }
//            x[0] = in[0];
//            x[1] = (in[1]-in[0])/in[0];
//            x[2] = (in[2]-in[0])/in[0];
//            x[3] = (in[3]-in[0])/in[0];
            //System.out.println(Arrays.toString(x));
            xx.add(x);
            tt.add(data[i + M]);
        }
        double t[] = Arrays.copyOf(tt.elements(), tt.size());
        double x[][] = xx.toArray(new double[][]{});

        NetConfig config = new NetConfig();
        config.alpha = ALPHA;
        config.momentum = MOMENTUM;
        config.n_inputs = M;
        config.n_outputs=1;
        config.hidenLayers = new int[]{5};
        config.func = WhichFunctionEnum.RELU;

        Net net = new Net(config);
        net.initWeights(Arrays.stream(data).min().getAsDouble(), Arrays.stream(data).max().getAsDouble());
        int it = 0;

        DoubleArrayList lseHistory = new DoubleArrayList();
        DoubleArrayList rmsHistory = new DoubleArrayList();
        double lse = Double.NaN;

        while (true) {
            if(endCondition(it,lseHistory)){
                break;
            }
            double y_out[] = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                y_out[i] = net.eval(x[i]);
                net.backpropagate(y_out[i], t[i], x[i]);
                net.computeWeights();
            }
            for (int i = 0; i < x.length; i++) {
                y_out[i] = net.eval(x[i]);
            }
            lse = Errors.leastSquareError(t,y_out);
            lseHistory.add(lse);
            double rms = Errors.rootMeanSquareError(lse, t.length);
            rmsHistory.add(rms);
            if(it % 100 == 0) {
                System.out.println("it = " + it);
                System.out.println("LSE=" + lse);
                System.out.println("RMS=" + rms);
                System.out.println("--------------------");
            }
            it++;
        }

        double y_out[] = new double[data2.length-M];
        double y_out2[] = new double[data2.length-M];
        for (int i = M,j=0; i < data2.length; i++,j++) {
            double in[] = new double[M];
            in = Arrays.copyOfRange(data2,i-M,i);
            y_out[j] = net.eval(in);
            if(j < M){
                y_out2[j] = net.eval(in);
            }else{
                in = Arrays.copyOfRange(y_out2,j-M,j);
                y_out2[j] = net.eval(in);
            }
            if(i < 10) {
                System.out.println(String.format("%f,%f,%f", data2[i], y_out[j], y_out2[j]));
            }
        }
        data2 = Arrays.copyOfRange(data2,1,data2.length);
        lse = Errors.leastSquareError(Arrays.copyOfRange(data2,M,data.length), y_out);

        double rms = Errors.rootMeanSquareError(lse, t.length);
        System.out.println("LSE=" + lse);
        System.out.println("RMS =" + rms);
        lse = Errors.leastSquareError(Arrays.copyOfRange(data2,M,data.length), y_out2);
         rms = Errors.rootMeanSquareError(lse, t.length);
        System.out.println("LSE2=" + lse);
        System.out.println("RMS2 =" + rms);
        System.out.println(net);
        System.out.println("--------------------");

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        // add a line plot to the PlotPanel

        plot.addLinePlot("spy_plot", Color.blue, data2);
        plot.addLinePlot("spy_plot", Color.red, y_out);
        plot.addLinePlot("spy_plot", Color.black, y_out2);

        //
        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("lse", Color.red, Arrays.copyOfRange(lseHistory.elements(), 0, lseHistory.size()));
        plot2.addLinePlot("rms", Color.red, Arrays.copyOfRange(rmsHistory.elements(), 0, rmsHistory.size()));

        //
        Dimension dim = new Dimension(800, 600);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plot2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static boolean endCondition(int iterations,  DoubleArrayList lseHistory) {
        return iterations > ITER_MAX;
    }

}
