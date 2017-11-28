package org.xSakix.rebalance;

import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.utils.DataLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class RebalancingPercentagePeriod {
    static int POP_SIZE = 400;
    static int ITER_MAX = 100;


    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        List<QuantumETFRebalancingPercentageParticle> particles = new CopyOnWriteArrayList<>();
//        String tikets[] = new String[]{"BND","SUSM-LSE","IFSW-LSE"};
        String tikets[] = new String[]{"BND","IAU","SPY","URTH","UUP"};
//        String tikets[] = new String[]{"TECS", "BND", "IAU", "UUP"};
//        String tikets[] = new String[]{"SPY", "BND", "IAU", "UUP"};
        double[][] in_data = DataLoader.loadData(tikets);


        for (int i = 0; i < POP_SIZE; i++) {
            particles.add(new QuantumETFRebalancingPercentageParticle(in_data));
        }

        DCAIndividual[] dcas = DataLoader.loadDCAForData(tikets, in_data);
        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumETFRebalancingPercentageParticle best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }
            double sum[] = new double[tikets.length];
            particles.parallelStream().forEach(particle ->
                    IntStream.range(0, tikets.length).forEach(i -> sum[i] += particle.getPw()[i]));
            double c[] = new double[tikets.length];
            IntStream.range(0, tikets.length).forEach(i -> c[i] = sum[i] / (double) POP_SIZE);
            particles.parallelStream().forEach(p -> p.setC(c));

            particles.parallelStream().forEach(p -> {
                p.computeWeights();
                p.computeFitness();
            });

            //sorts asc
            particles.sort((o1, o2) -> {
                if (o1.getFitness() > o2.getFitness())
                    return -1;
                if (o1.getFitness() < o2.getFitness())
                    return 1;
                return 0;
            });

            fitnessHistory[iterations] = particles.get(0).getFitness();

            if (best == null || best.getFitness() < particles.get(0).getFitness()) {
                best = particles.get(0).clone();
                double[] Gw = Arrays.copyOf(best.getW(), tikets.length);
                particles.parallelStream().forEach(p -> p.setGw(Gw));
            }


            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
            System.out.println("Best fitness = "+ best.getFitness());
            System.out.println("Actual fitness = "+ particles.get(0).getFitness());
            QuantumETFRebalancingPercentageParticle finalBest = best;
            IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + tikets[j] + "=" + finalBest.getShares()[j]));
            System.out.println("cash =" + best.getCash());
            System.out.println("Number of rebalances = " + best.getRebalances());

            particles.parallelStream().forEach(p -> p.reinitialize());
            iterations++;
        }
        System.out.println("----RESULTS-----");
        System.out.println("DAYS:" + in_data[0].length);
        System.out.println(String.format("Best fitness = %f", best.getFitness()));
        IntStream.range(0, tikets.length).forEach(j -> System.out.println("individual " + tikets[j] + " = " + dcas[j].total()));
        System.out.println("Investment = " + best.getInvestment());

        List<double[]> shares_history = best.getShares_history();
        Plot2DPanel plot = new Plot2DPanel();
        IntStream.range(0, shares_history.size())
                 .forEach(j -> {plot.addLinePlot(tikets[j],
                                      new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                                      shares_history.get(j));
                     });
        plot.addLegend("SOUTH");


        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("value",Color.red,best.getValue());
        plot2.addLinePlot("invested",Color.blue,best.getInvested());

        Plot2DPanel plot3 = new Plot2DPanel();
        plot3.addLinePlot("fitness",Color.red,fitnessHistory);

        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("REBALANCE");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plot2);
        frame.add(plot3);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
