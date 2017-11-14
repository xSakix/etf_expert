package org.xSakix.particle;

import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.tools.Errors;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class QuantumParticleCurveFit {

    public static final int FRAME = 10;
    int POP_SIZE = 400;
    int ITER_MAX = 400;
    private final double[] data;
    private int m;
    double x[];
    double t[];

    public QuantumParticleCurveFit(int m, double data[]) {
        this.m = m;
        this.POP_SIZE = m*1000;
        this.ITER_MAX = m*50;
        this.data = data;

        x = Arrays.copyOfRange(data, 0, data.length-1);
        t = Arrays.copyOfRange(data, 1, data.length );
    }

    public double[] run() {
        List<QuantumParticle> particles = new ArrayList<QuantumParticle>(POP_SIZE);

        double max = 1.;
        initializaParticles(particles,
                -max,
                max);

        int iterations = 0;

        while (true) {
            //if (iterations > ITER_MAX) {
            if(iterations > ITER_MAX){
                break;
            }

            double sum[] = new double[m];
            for (QuantumParticle particle : particles) {
                double pw[] = particle.getPw();
                for (int i = 0; i < pw.length; i++) {
                    sum[i] += pw[i];
                }
            }
            double c[] = new double[m];
            for (int i = 0; i < m; i++) {
                c[i] = sum[i] / ((double) particles.size());
            }
            particles.parallelStream().forEach(p -> p.setC(c));

            particles.parallelStream().forEach(p -> {
                p.computeWeights();
                p.computeFitness(x, t);
            });

            Collections.sort(particles, (o1, o2) -> Double.compare(o1.getFitness(), o2.getFitness()));

            particles.parallelStream().forEach(p -> p.setGw(particles.get(0).getW()));


            iterations++;
        }
        System.out.println(String.format("Iteration = %d", iterations));
        System.out.println(String.format("Best     = %s", Arrays.toString(particles.get(0).getW())));
        System.out.println(String.format("Best fitness = %f", particles.get(0).getFitness()));
        System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));
        System.out.println(String.format("Best RMS = %f", particles.get(0).getRms()));

        return particles.get(0).getW();
    }

    private void initializaParticles(List<QuantumParticle> particles, double min, double max) {
        for (int i = 0; i < POP_SIZE; i++) {
            particles.add(new QuantumParticle(m, min, max));
        }
    }


    public static void main(String[] args) throws IOException {
        //double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //double data[] = EtfReader.readEtf("c:\\downloaded_data\\latest\\EWS.csv");
//        double data[] = EtfReader.readEtf("c:\\downloaded_data\\latest\\EWA.csv");
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\latest\\AGG.csv");

        int m = 5;

        QuantumParticleCurveFit curveFit = new QuantumParticleCurveFit(m,data);
        double w[] = curveFit.run();
        QuantumParticle best = new QuantumParticle(m);
        best.setW(w);
        Plot2DPanel plot = new Plot2DPanel();

        plot.addLinePlot("spy_plot", Color.blue, data);


        double y[] = new double[data.length];
        double xx[] = Arrays.copyOfRange(data, 0, data.length);
        System.out.println("Testing input length:" + xx.length);
        for (int i = 0; i < y.length; i++) {
            if (i < data.length - FRAME)
                y[i] = best.evaluate(xx[i]);
            else
                y[i] = best.evaluate(y[i - 1]);
        }
        plot.addLinePlot("curve_plot", Color.red, y);

        System.out.println("------------------RESULTS-----------------");
        double yy[] = new double[FRAME];
        for (int i = data.length - FRAME, j = 0; i < data.length; i++, j++) {
            yy[j] = data[i];
            double error = Math.abs(data[i] - y[i]);
            System.out.println(String.format("error = %.3f-%.3f = %.3f", yy[j], y[i], error));
        }
        System.out.println(String.format("LSE=%f", Errors.leastSquareError(yy, Arrays.copyOfRange(y, y.length - FRAME, y.length))));


        Dimension dim = new Dimension(800, 600);

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
