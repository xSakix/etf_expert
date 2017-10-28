package org.xSakix.curvefittingga;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.gatools.FloatOperations;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ComputedCasesCurveFitPoly {

    public static final int FRAME = 10;

    public static void main(String[] args) throws IOException {

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");

        double w3[] = new double[]{0.0832316005399744, 0.9992361673228384, 2.4798822336E-6};
        //double w4[] = new double[]{8.0E-16, 1.0405022131026498, -5.49755813888E-4, 1.7179869184E-6};
        double w4[] = new double[]{-0.03019892200769778, 1.002993140993256, -3.181531346503724E-5, 9.058001364948748E-8};
        double w2[] = new double[]{0.0492581209245362, 0.9998617997234333};
        double w5[] = new double[]{-0.19301101846284507, 1.0099907602368714, -1.2954303654558636E-4, 6.367456574589222E-7, -1.0508740353217935E-9};
        //w5=-1.6910046134224208, 1.0672289893236564, -8.526703445085376E-4, 4.338403036572137E-6, -7.659993410245851E-9
        Individual best = new Individual(w3.length);
        best.setW(w3);

        Individual best4 = new Individual(w4.length);
        best4.setW(w4);

        Individual best2 = new Individual(w2.length);
        best2.setW(w2);

        Individual best5 = new Individual(w5.length);
        best5.setW(w5);

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("spy_plot", Color.blue, data);


        double y2[] = new double[data.length];
        double y3[] = new double[data.length];
        double y4[] = new double[data.length];
        double y5[] = new double[data.length];
        double xx[] = Arrays.copyOfRange(data, 0, data.length);
        System.out.println("Testing input length:" + xx.length);
        for (int i = 0; i < y3.length; i++) {
            if (i < data.length - FRAME) {
                y2[i] = best2.evaluate(xx[i]);
                y3[i] = best.evaluate(xx[i]);
                y4[i] = best4.evaluate(xx[i]);
                y5[i] = best5.evaluate(xx[i]);
            }else {
                y2[i] = best2.evaluate(y2[i - 1]);
                y3[i] = best.evaluate(y3[i - 1]);
                y4[i] = best4.evaluate(y4[i - 1]);
                y5[i] = best5.evaluate(y5[i - 1]);
            }
        }
        plot.addLinePlot("curve_plot", Color.red, y3);
        plot.addLinePlot("curve_plot", Color.green, y4);
        plot.addLinePlot("curve_plot", Color.yellow, y2);
        plot.addLinePlot("curve_plot", Color.black, y5);

        System.out.println("------------------RESULTS-----------------");

        for (int i = data.length - FRAME; i < data.length; i++) {
            double error2 = Math.abs(data[i] - y2[i]);
            double error3 = Math.abs(data[i] - y3[i]);
            double error4 = Math.abs(data[i] - y4[i]);
            double error5 = Math.abs(data[i] - y5[i]);
            System.out.println(String.format("error(2) = %.3f-%.3f = %.3f", data[i], y2[i], error2));
            System.out.println(String.format("error(3) = %.3f-%.3f = %.3f", data[i], y3[i], error3));
            System.out.println(String.format("error(4) = %.3f-%.3f = %.3f", data[i], y4[i], error4));
            System.out.println(String.format("error(5) = %.3f-%.3f = %.3f", data[i], y5[i], error5));
        }



        Dimension dim = new Dimension(800, 600);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
