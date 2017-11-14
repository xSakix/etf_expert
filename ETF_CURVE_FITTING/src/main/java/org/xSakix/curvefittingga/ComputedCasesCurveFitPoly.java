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

        //double w3[] = new double[]{0.0832316005399744, 0.9992361673228384, 2.4798822336E-6};
        //double w3[] = new double[]{0.14987186869679725, 0.8686072067223694, 7.660119942374608E-4};
        //double w4[] = new double[]{8.0E-16, 1.0405022131026498, -5.49755813888E-4, 1.7179869184E-6};
        //double w4[] = new double[]{-0.03019892200769778, 1.002993140993256, -3.181531346503724E-5, 9.058001364948748E-8};
        //double w2[] = new double[]{0.0492581209245362, 0.9998617997234333};
        double w5[] = new double[]{-0.19301101846284507, 1.0099907602368714, -1.2954303654558636E-4, 6.367456574589222E-7, -1.0508740353217935E-9};
        //w5=-1.6910046134224208, 1.0672289893236564, -8.526703445085376E-4, 4.338403036572137E-6, -7.659993410245851E-9
//        Individual best = new Individual(w3.length);
//        best.setW(w3);
//
//        Individual best4 = new Individual(w4.length);
//        best4.setW(w4);
//
//        Individual best2 = new Individual(w2.length);
//        best2.setW(w2);

        Individual best5 = new Individual(w5.length);
        best5.setW(w5);

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("spy_plot", Color.blue, data);


//        double y2[] = new double[data.length];
//        double y3[] = new double[data.length];
//        double y4[] = new double[data.length];
        double y5[] = new double[data.length];
        double xx[] = Arrays.copyOfRange(data, 0, data.length);
        System.out.println("Testing input length:" + xx.length);
        for (int i = 0; i < data.length; i++) {
            if (i < data.length - FRAME) {
//                y2[i] = best2.evaluate(xx[i]);
//                y3[i] = best.evaluate(xx[i]);
//                y4[i] = best4.evaluate(xx[i]);
                y5[i] = best5.evaluate(xx[i]);
            } else {
//                y2[i] = best2.evaluate(y2[i - 1]);
//                y3[i] = best.evaluate(y3[i - 1]);
//                y4[i] = best4.evaluate(y4[i - 1]);
                y5[i] = best5.evaluate(y5[i - 1]);
            }
        }
//        plot.addLinePlot("curve_plot", Color.red, y3);
//        plot.addLinePlot("curve_plot", Color.green, y4);
//        plot.addLinePlot("curve_plot", Color.yellow, y2);
//        plot.addLinePlot("curve_plot", Color.black, y5);

        System.out.println("------------------RESULTS FEEDBACK-----------------");

        for (int i = data.length - FRAME; i < data.length; i++) {
//            double error2 = Math.abs(data[i] - y2[i]);
//            double error3 = Math.abs(data[i] - y3[i]);
//            double error4 = Math.abs(data[i] - y4[i]);
            double error5 = Math.abs(data[i] - y5[i]);
//            System.out.println(String.format("error(2) = %.3f-%.3f = %.3f", data[i], y2[i], error2));
//            System.out.println(String.format("error(3) = %.3f-%.3f = %.3f", data[i], y3[i], error3));
//            System.out.println(String.format("error(4) = %.3f-%.3f = %.3f", data[i], y4[i], error4));
            System.out.println(String.format("error(5) = %.3f-%.3f = %.3f", data[i], y5[i], error5));
        }

        System.out.println("---------------RESULT SPY CUR VALS-------------------");

        double vals[] = new double[]{257.18, 256.47, 256.47, 255.99, 256.18, 256.60, 257.48, 256.70, 254.83, 255.90, 255.23, 255.21, 255.14, 254.66, 254.51, 254.60, 254.63, 254.15, 253.54, 252.69, 252.32, 251.49, 250.34, 249.73, 249.88, 249.42, 249.15, 249.05, 249.88, 250.07, 250.00, 249.61, 248.69, 249.80, 249.72, 249.63, 248.04, 246.54, 247.25, 246.84, 247.26, 247.92, 246.72, 244.83, 243.06, 245.17, 244.90, 245.00, 244.33, 243.57, 242.64, 242.90, 246.24, 247.11, 246.98, 245.59, 244.02, 246.29, 246.47, 247.51, 247.49, 247.52, 247.31, 247.47, 247.46, 247.37, 246.65, 247.96, 247.75, 247.68, 246.79, 246.44, 247.28, 246.02, 245.06, 245.47, 244.42, 244.02, 243.30, 242.37, 242.11, 241.21, 241.89, 242.63, 242.88, 242.28, 243.66, 242.50, 243.04, 243.90, 242.91, 242.96, 243.46, 244.25, 243.59, 242.77, 242.68, 244.86};
        double cur_vals[]  = new double[vals.length];
        for(int i = vals.length-1,j=0;i >= 0 ;i--,j++) {
            cur_vals[j] = vals[i];
        }
        double comp[] = new double[cur_vals.length];
        double comp2[] = new double[cur_vals.length];
        double comp3[] = new double[cur_vals.length];
        for(int i = 0; i < 3;i++){
            comp[i]=comp2[i]=comp3[i]=cur_vals[i];
        }
        for (int i = 3; i < cur_vals.length; i++) {
            comp[i] = best5.evaluate(cur_vals[i-1]);
            comp2[i] = best5.evaluate(best5.evaluate(cur_vals[i-2]));
            comp3[i] = best5.evaluate(best5.evaluate(best5.evaluate(cur_vals[i-3])));
            double error_one_day = Math.abs(cur_vals[i] - comp[i]);
            double error_two_days = Math.abs(cur_vals[i] - comp2[i]);
            double error_three_days = Math.abs(cur_vals[i] - comp3[i]);
            System.out.println(String.format("error(one day) = %.3f-%.3f = %.3f", cur_vals[i],comp[i],  error_one_day));
            System.out.println(String.format("error(two days) = %.3f-%.3f = %.3f", cur_vals[i],comp2[i],  error_two_days));
            System.out.println(String.format("error(three days) = %.3f-%.3f = %.3f", cur_vals[i],comp3[i],  error_three_days));
        }
        Plot2DPanel plotCur = new Plot2DPanel();
        plotCur.addLinePlot("cur",Color.blue,cur_vals);
        plotCur.addLinePlot("cur",Color.red,comp);
        plotCur.addLinePlot("cur",Color.green,comp2);
        plotCur.addLinePlot("cur",Color.black,comp3);


        Dimension dim = new Dimension(800, 600);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plotCur);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
