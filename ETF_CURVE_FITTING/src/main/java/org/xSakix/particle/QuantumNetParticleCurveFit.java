package org.xSakix.particle;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.particle.neural.particle.NetParticle;
import org.xSakix.particle.neural.quantum.QuantumNet;
import org.xSakix.particle.neural.quantum.QuantumNetParticle;
import org.xSakix.tools.Errors;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuantumNetParticleCurveFit {

    public static final int FRAME = 20;
    public static final int POP_SIZE = 50;
    public static final int ITER_MAX = 10000;
    public static final int M = 4;
    private static final double ALPHA = 0.75;



    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws IOException {

        System.out.println("Alpha= "+ALPHA);

        double orig_data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        double norm = 1000.;
        double data[] = Arrays.stream(orig_data).map(d -> d/norm).toArray();

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
        int hidden[] = new int[]{4,4};

        List<QuantumNetParticle> particles = new ArrayList<>(POP_SIZE);
        preselect(hidden, particles,x,t,norm);

        double[] fitnessHistory = new double[ITER_MAX];

        int iterations = 0;

        QuantumNetParticle best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }

            QuantumNet sumNet = new QuantumNet(ALPHA,M,1,hidden);
            particles.stream().forEach(p -> sumNet.addToC(p.getNet()));
            sumNet.divCByParticlesSize(particles.size());

            particles.parallelStream().forEach(p -> {
                p.setC(sumNet);
                p.computeWeights();
                p.computeFitness(x,t,norm);
            });

            Collections.sort(particles, Comparator.comparingDouble(QuantumNetParticle::getFitness));

            fitnessHistory[iterations]=particles.get(0).getFitness();

            if (best == null || best.getFitness() > particles.get(0).getFitness()) {
                best = particles.get(0);
                best.setGw(best.getGw());
                for(int i = 1; i < POP_SIZE;i++){
                    particles.get(i).setGw(best.getGw());
                }
            }
            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best     = %s", best.getGw().toString()));
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
        double y[] = new double[data.length-M];
        double yy[] = new double[data.length-M];
        for (int i = data.length-FRAME; i < data.length-M; i++) {
            //y[i] = best.evaluate(x[i])*norm;
            y[i] = best.evaluate(x[i]);
            yy[i] = t[i];
            double error = Math.abs( yy[i]-y[i]);
            System.out.println(String.format("error = %.3f-%.3f = %.3f", yy[i],y[i], error));
        }
        System.out.println(String.format("LSE=%f", Errors.leastSquareError(yy,y)));

        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("fitness",fitnessHistory);

        Dimension dim = new Dimension(800, 600);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void preselect(int[] hidden, List<QuantumNetParticle> particles, double[][] x, double[] t, double norm) {
        List<QuantumNetParticle> selected = new CopyOnWriteArrayList<>();

        while(selected.size() < POP_SIZE) {
            for (int i = 0; i < POP_SIZE; i++) {
                particles.add(new QuantumNetParticle(ALPHA, M, hidden));
            }
            particles.parallelStream().forEach(p->{
                p.computeFitness(x,t,norm);
                System.out.println(p.getFitness());
                if(p.getFitness() < 5.0 ){
                    selected.add(p);
                }
            });
            particles.clear();
            System.out.println(selected.size());
        }
        particles.addAll(selected);
        selected.clear();
    }


}
