package org.xSakix.particle;

import cern.colt.list.DoubleArrayList;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.particle.neural.NetParticle;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class NetParticleCurveFit {

    public static final int FRAME = 10;
    public static final int POP_SIZE = 1000;
    public static final int ITER_MAX = 100;
    public static final int M = 4;

    private static double[] adjustdata(double[] data, double max) {
        double dataTemp[] = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            dataTemp[i] = data[i] / max;
        }

        return Arrays.copyOf(dataTemp, dataTemp.length);
    }

    public static void main(String[] args) throws IOException {
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //data adjustment, find max and normalize data on max
        double max = Arrays.stream(data).max().getAsDouble();
        data = adjustdata(data,max);

        List<double[]> xx = new ArrayList<>();
        DoubleArrayList tt = new DoubleArrayList();
        for(int i =0 ;i < data.length-M;i++){
            double x[] = new double[M];
            for(int j = 0; j < M;j++) {
                x[j] = data[i+j];
            }
            xx.add(x);
            tt.add(data[i+M]);
        }
        double t[] = Arrays.copyOf(tt.elements(),tt.size());
        double x[][] =  xx.toArray(new double[][]{});

        List<NetParticle> particles = new ArrayList<NetParticle>(POP_SIZE);
        for(int i = 0; i < POP_SIZE;i++){
            particles.add(new NetParticle(4,4,9));
        }

        DoubleArrayList fitnessHistory = new DoubleArrayList(ITER_MAX);

        int iterations = 0;

        NetParticle best = null;


        while (true) {
            if (best!=null && best.getFitness() < 0.1) {
                break;
            }

            particles.parallelStream().forEach(p -> {
                p.computeVelocity();
                p.computeWeights();
                p.computeFitness(x,t);
            });

            Collections.sort(particles, new Comparator<NetParticle>() {
                @Override
                public int compare(NetParticle o1, NetParticle o2) {
                    return Double.compare(o1.getFitness(), o2.getFitness());
                }
            });

            fitnessHistory.add(particles.get(0).getFitness());

            if (best == null || best.getFitness() > particles.get(0).getFitness()) {
                best = particles.get(0);
                best.setGw(best.getGw());
                for(int i = 1; i < POP_SIZE;i++){
                    particles.get(i).setGw(best.getGw());
                }
            }
            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", best.getGw().toString()));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));
            System.out.println(String.format("Best RMS = %f", best.getRms()));

            iterations++;
        }

//        // create your PlotPanel (you can use it as a JPanel)
//        Plot2DPanel plot = new Plot2DPanel();
//        //Plot2DPanel plot1 = new Plot2DPanel();
//        Plot2DPanel plot2 = new Plot2DPanel();
//
//        // add a line plot to the PlotPanel
//        plot.addLinePlot("spy_plot", Color.blue, data);
//
//
//        double y[] = new double[data.length];
//        double xx[] = Arrays.copyOfRange(data, 0, data.length);
//        System.out.println("Training input length:" + x.length);
//        System.out.println("Testing input length:" + xx.length);
//        for (int i = 0; i < y.length; i++) {
//            if (i < data.length - FRAME)
//                y[i] = best.evaluate(xx[i]);
//            else
//                y[i] = best.evaluate(y[i - 1]);
//        }
//        plot.addLinePlot("curve_plot", Color.red, y);
//
        System.out.println("------------------RESULTS-----------------");

        for (int i = data.length-FRAME; i < data.length-M; i++) {
            double error = Math.abs(data[i]*max - best.evaluate(x[i])*max);
            System.out.println(String.format("error = %.3f-%.3f = %.3f", data[i]*max, best.evaluate(x[i])*max, error));
        }
//
//
//        plot2.addLinePlot("fitness",Arrays.copyOf(fitnessHistory.elements(),fitnessHistory.size()));
//
//        Dimension dim = new Dimension(800, 600);
//
//        // put the PlotPanel in a JFrame, as a JPanel
//        JFrame frame = new JFrame("SPY");
//        frame.setLayout(new GridLayout());
//        frame.setSize(dim);
//        frame.setMaximumSize(dim);
//        frame.setMinimumSize(dim);
//        frame.add(plot);
//        //frame.add(plot1);
//        frame.add(plot2);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static boolean endCondition(int iterations, Particle best, double[] x, double[] t) {
        return iterations > ITER_MAX || (best != null && best.computeFitness(x, t) < 0.001);
    }

}
