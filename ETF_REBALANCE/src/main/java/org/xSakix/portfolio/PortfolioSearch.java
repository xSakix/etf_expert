package org.xSakix.portfolio;

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

public class PortfolioSearch {
    static int POP_SIZE = 400;
    static int ITER_MAX = 100;
    static int MAX_DAYS = 0;


    public static void main(String[] args) throws IOException, CloneNotSupportedException {
//        String tikets[] = new String[]{"BND","SUSM-LSE","IFSW-LSE"};
//        String tikets[] = new String[]{"BND","IAU","SPY","URTH","UUP"};
//        String tikets[] = new String[]{"TECS", "BND", "IAU", "UUP"};

//        List<String> all = new CopyOnWriteArrayList<>(DataLoader.findWhichNumOfDataIsMoreOrEqual(MAX_DAYS));
//        System.out.println("FOUND: "+all.size());
//        String tikets[] = new String[2];
//        QuantumETFPortfolioParticle best = null;
//        for(String etf1 :  all) {
//            String chosen[] = new String[2];
//            chosen[0] = etf1;
//            all.remove(etf1);
//            for(String etf2 : all) {
//                chosen[1] = etf2;
//                System.out.println("Computing for "+Arrays.toString(chosen));
//                QuantumETFPortfolioParticle actual = computeForTickets(chosen,MAX_DAYS);
//                if(best == null || best.getFitness() < actual.getFitness()){
//                    best  = actual.clone();
//                    tikets = Arrays.copyOf(chosen,chosen.length);
//                }
//            }
//        }
        //String tikets[] = new String[]{"BND","IAU","SPY","URTH","UUP"};
        String tikets[] = new String[]{"BND","SUSM-LSE","IFSW-LSE"};
        QuantumETFPortfolioParticle best = computeForTickets(tikets);

        List<double[]> shares_history = best.getShares_history();
        Plot2DPanel plot = new Plot2DPanel();
        String[] finalTikets = tikets;
        IntStream.range(0, shares_history.size())
                 .forEach(j -> {plot.addLinePlot(finalTikets[j],
                                      new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                                      shares_history.get(j));
                     });
        plot.addLegend("SOUTH");


        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("value",Color.red,best.getValue());
        plot2.addLinePlot("invested",Color.blue,best.getInvested());


        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("REBALANCE");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plot2);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static QuantumETFPortfolioParticle computeForTickets(String[] tikets) throws IOException, CloneNotSupportedException {

        List<QuantumETFPortfolioParticle> particles = new CopyOnWriteArrayList<>();
        double[][] in_data = DataLoader.loadData(tikets,MAX_DAYS);


        for (int i = 0; i < POP_SIZE; i++) {
            particles.add(new QuantumETFPortfolioParticle(in_data));
        }

        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumETFPortfolioParticle best = null;

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
            QuantumETFPortfolioParticle finalBest = best;
            IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + tikets[j] + "=" + finalBest.getShares()[j]));
            System.out.println("cash =" + best.getCash());

            particles.parallelStream().forEach(p -> p.reinitialize());
            iterations++;
        }
        System.out.println("----RESULTS-----");
        System.out.println("DAYS:" + in_data[0].length);
        System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
        System.out.println(String.format("Best fitness = %f", best.getFitness()));
        QuantumETFPortfolioParticle finalBest = best;
        IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + tikets[j] + "=" + finalBest.getShares()[j]));
        System.out.println("Investment = " + best.getInvestment());
        System.out.println(in_data[0][in_data[0].length-1]);
        System.out.println(in_data[1][in_data[1].length-1]);
        return best;
    }
}
