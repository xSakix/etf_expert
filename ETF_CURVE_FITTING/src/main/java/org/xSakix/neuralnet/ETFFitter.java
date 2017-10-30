package org.xSakix.neuralnet;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.tools.Errors;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ETFFitter {

    public static final int FRAME = 20;
    public static final int POP_SIZE = 100;
    public static final int ITER_MAX = 200;
    public static final int M = 2;
    private static final double FIT_TOL = 0.001;
    private static final double ERROR_TOL = 0.1;
    //    private static final double ALPHA = Uniform.staticNextDoubleFromTo(0.01,0.5);
    private static final double ALPHA = 0.03;

    private static double[] adjustData(double[] data, double max) {
        double dataTemp[] = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            dataTemp[i] = data[i] / max;
        }

        return Arrays.copyOf(dataTemp, dataTemp.length);
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Alpha= " + ALPHA);

        double orig_data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //data adjustment, find max and normalize data on max
        double max = Arrays.stream(orig_data).max().getAsDouble();
        double data[] = adjustData(orig_data, max);

        List<double[]> xx = new ArrayList<>();
        DoubleArrayList tt = new DoubleArrayList();
        for (int i = 0; i < data.length - M; i++) {
            double x[] = new double[M];
            for (int j = 0; j < M; j++) {
                x[j] = data[i + j];
            }
            xx.add(x);
            tt.add(data[i + M]);
        }
        double t[] = Arrays.copyOf(tt.elements(), tt.size());
        double x[][] = xx.toArray(new double[][]{});

        int layer = M * 2 + 1;

        Net net = new Net(ALPHA, M, 1, layer,layer*2+1);
        int it = 0;

        DoubleArrayList lseHistory = new DoubleArrayList();
        DoubleArrayList rmsHistory = new DoubleArrayList();
        double lse = Double.NaN;

        while (true) {
            if (lse != Double.NaN && lse < 0.001) {
            //if(it > 100){
                break;
            }
            double y_out[] = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                y_out[i] = net.eval(x[i]);
                net.backpropagate(y_out[i], t[i], x[i]);
                net.computeWeights();
            }
            lse = Errors.leastSquareError(t, y_out);
            lseHistory.add(lse);
            double rms = Errors.rootMeanSquareError(lse, t.length);
            rmsHistory.add(rms);
            System.out.println("LSE=" + lse);
            System.out.println("RMS=" + rms);
            System.out.println("--------------------");
            it++;
        }

        double y_out[] = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y_out[i] = net.eval(x[i]);
        }


        System.out.println(Arrays.toString(t));
        System.out.println(Arrays.toString(y_out));
        lse = Errors.leastSquareError(t, y_out);
        double rms = Errors.rootMeanSquareError(lse, t.length);
        System.out.println("LSE=" + lse);
        System.out.println("RMS =" + lse);
        System.out.println(net);
        System.out.println("--------------------");

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        // add a line plot to the PlotPanel
        plot.addLinePlot("spy_plot", Color.blue, data);
        plot.addLinePlot("spy_plot", Color.red, y_out);

        //
        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("lse",Color.red,Arrays.copyOfRange(lseHistory.elements(),0,lseHistory.size()));
        plot2.addLinePlot("rms",Color.red,Arrays.copyOfRange(rmsHistory.elements(),0,rmsHistory.size()));

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
}
