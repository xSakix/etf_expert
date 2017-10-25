package org.xSakix.curvefittingga;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.functions.Functions;
import org.xSakix.gatools.FloatOperations;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CurveFitter {

    public static final int FRAME = 300;
    public static final int POP_SIZE = 100;
    public static final int M = 3;
    public static final int ITER_MAX = 2000;
    public static final double ERROR_TOL = 0.001;
    public static final double MUT_RATE = 0.1;

    public static void main(String[] args) throws IOException {

        //p(x) = w[0]+w[1]*x+w[2]*x^2+w[3]*x^3+....+w[M]*x^M
        //we are looking for w[0],w[1],w[2],w[3],...,w[M]
        //such that LSE < ERROR_TOL
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //data adjustment, find max and normalize data on max
        //data = adjustdata(data);

        double x[] = Arrays.copyOfRange(data, 0, data.length - FRAME);
        double t[] = Arrays.copyOfRange(data, 1, data.length - FRAME + 1);
        double testX[] = Arrays.copyOfRange(data, data.length - FRAME, data.length - 1);
        double testY[] = Arrays.copyOfRange(data, data.length - FRAME + 1, data.length);

        List<Individual> individuals = new ArrayList<Individual>(POP_SIZE);
        initializaPopulation(individuals);
        int iterations = 0;
        Individual best = null;

        DoubleArrayList fitnessHistory = new DoubleArrayList(ITER_MAX);

        while (true) {
            if (endCondition(iterations, best, x, t)) {
                break;
            }
            individuals.parallelStream().forEach(i -> i.computeFitness(x, t));
            Collections.sort(individuals, new Comparator<Individual>() {
                @Override
                public int compare(Individual o1, Individual o2) {
                    return Double.compare(o1.getFitness(), o2.getFitness());
                }
            });
            fitnessHistory.add(individuals.get(0).getFitness());
            if (best == null || best.getFitness() > individuals.get(0).getFitness()) {
                best = individuals.get(0);
            }
            List<Individual> individualList = new ArrayList<>(POP_SIZE);
            individualList.add(individuals.get(0));
            individualList.add(individuals.get(1));
            while (individualList.size() < POP_SIZE) {
                Individual parent1 = individuals.get(0);
                Individual parent2 = individuals.get(1);
                individuals.remove(parent1);
                individuals.remove(parent2);
                Individual child1 = new Individual(M);
                Individual child2 = new Individual(M);
                double w_cross12[] = new double[M];
                double w_cross21[] = new double[M];
                double w1[] = parent1.getW();
                double w2[] = parent2.getW();
                for (int i = 0; i < M; i++) {
                    w_cross12[i] = FloatOperations.cross(w1[i], w2[i]);
                    w_cross21[i] = FloatOperations.cross(w2[i], w1[i]);
                    if (Uniform.staticNextDoubleFromTo(0., 1.) < MUT_RATE) {
                        w_cross12[i] = FloatOperations.mutate(w_cross12[i]);
                        w_cross21[i] = FloatOperations.mutate(w_cross21[i]);
                    }
                }
                child1.setW(w_cross12);
                child2.setW(w_cross21);
                individualList.add(child1);
                individualList.add(child2);
            }
            individuals.clear();
            individuals.addAll(individualList);
            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", individuals.get(0).getFitness()));
            System.out.println(String.format("Best RMS = %f", best.getRms()));
            iterations++;
        }

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        //Plot2DPanel plot1 = new Plot2DPanel();
        Plot2DPanel plot2 = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("spy_plot",Color.blue, data);


        double y[] = new double[data.length-1];
        double xx[] = Arrays.copyOfRange(data,0,data.length);
        System.out.println("Training input length:"+x.length);
        System.out.println("Testing input length:"+xx.length);
        for (int i = 0; i < y.length; i++) {
            y[i] = best.evaluate(xx[i]);
        }
        plot.addLinePlot("curve_plot",Color.red, y);

        //plot2.addLinePlot("fitness",Arrays.copyOf(fitnessHistory.elements(),fitnessHistory.size()));

        Dimension dim = new Dimension(800,600);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        //frame.add(plot1);
        //frame.add(plot2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static double[] adjustdata(double[] data) {
        double dataTemp[] = new double[data.length];
        double max = Arrays.stream(data).max().getAsDouble();
        for(int i = 0;i < data.length;i++){
            dataTemp[i] = data[i]/max;
        }
        data = Arrays.copyOf(dataTemp,dataTemp.length);
        return data;
    }

    private static boolean endCondition(int iterations, Individual best, double[] x, double[] t) {
        return iterations > ITER_MAX || (best != null && best.computeFitness(x, t) < 0.001);
    }

    private static void initializaPopulation(List<Individual> individuals) {
        for (int i = 0; i < POP_SIZE; i++) {
            individuals.add(new Individual(M));
        }
    }
}
