package org.xSakix.particle;

import cern.colt.list.DoubleArrayList;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class QuantumParticleCurveFit {

    public static final int FRAME = 10;
    public static final int POP_SIZE = 100000;
    public static final int ITER_MAX = 200;
    public static final int M = 5;

    public static void main(String[] args) throws IOException {
        //p(x) = w[0]+w[1]*x+w[2]*x^2+w[3]*x^3+....+w[M]*x^M
        //we are looking for w[0],w[1],w[2],w[3],...,w[M]
        //such that LSE < ERROR_TOL
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //data adjustment, find max and normalize data on max
        //data = adjustdata(data);

        double x[] = Arrays.copyOfRange(data, 0, data.length - FRAME);
        double t[] = Arrays.copyOfRange(data, 1, data.length - FRAME + 1);

        List<QauntumParticle> particles = new ArrayList<QauntumParticle>(POP_SIZE);

        double max = 1.;
        initializaParticles(particles,
                -max,
                max);

        DoubleArrayList fitnessHistory = new DoubleArrayList(ITER_MAX);

        int iterations = 0;

        QauntumParticle best = null;

        while (true) {
            if (endCondition(iterations, best, x, t,fitnessHistory)) {
                break;
            }

            particles.parallelStream().forEach(p -> {
                p.computeWeights();
                p.computeFitness(x,t);
            });

            Collections.sort(particles, new Comparator<QauntumParticle>() {
                @Override
                public int compare(QauntumParticle o1, QauntumParticle o2) {
                    return Double.compare(o1.getFitness(), o2.getFitness());
                }
            });

            fitnessHistory.add(particles.get(0).getFitness());

            if (best == null || best.getFitness() > particles.get(0).getFitness()) {
                best = particles.get(0);
                best.setGw(best.getW());
                for(int i = 1; i < POP_SIZE;i++){
                    particles.get(i).setGw(best.getW());
                }
            }
            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));
            System.out.println(String.format("Best RMS = %f", best.getRms()));

            iterations++;
        }

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        //Plot2DPanel plot1 = new Plot2DPanel();
        Plot2DPanel plot2 = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("spy_plot", Color.blue, data);


        double y[] = new double[data.length];
        double xx[] = Arrays.copyOfRange(data, 0, data.length);
        System.out.println("Training input length:" + x.length);
        System.out.println("Testing input length:" + xx.length);
        for (int i = 0; i < y.length; i++) {
            if (i < data.length - FRAME)
                y[i] = best.evaluate(xx[i]);
            else
                y[i] = best.evaluate(y[i - 1]);
        }
        plot.addLinePlot("curve_plot", Color.red, y);

        System.out.println("------------------RESULTS-----------------");

        for (int i = data.length - FRAME; i < data.length; i++) {
            double error = Math.abs(data[i] - y[i]);
            System.out.println(String.format("error = %.3f-%.3f = %.3f", data[i], y[i], error));
        }


        plot2.addLinePlot("fitness",Arrays.copyOf(fitnessHistory.elements(),fitnessHistory.size()));

        Dimension dim = new Dimension(800, 600);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        //frame.add(plot1);
        frame.add(plot2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static boolean endCondition(int iterations, QauntumParticle best, double[] x, double[] t, DoubleArrayList fitnessHistory) {
        //return iterations > ITER_MAX || (best != null && best.computeFitness(x, t) < 0.001);
        int size = fitnessHistory.size();
        return size > 10 && Math.abs(fitnessHistory.get(size-1) - fitnessHistory.get(size-10)) < 0.001;
    }

    private static void initializaParticles(List<QauntumParticle> particles, double min, double max) {
        for (int i = 0; i < POP_SIZE; i++) {
            particles.add(new QauntumParticle(M, min, max));
        }
    }
}
