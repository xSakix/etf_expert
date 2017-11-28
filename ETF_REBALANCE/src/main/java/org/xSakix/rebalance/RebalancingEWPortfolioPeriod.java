package org.xSakix.rebalance;

import org.xSakix.individuals.DCAIndividual;
import org.xSakix.utils.DataLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class RebalancingEWPortfolioPeriod {
    static int POP_SIZE = 200;
    static int ITER_MAX = 100;


    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        List<QuantumETFRebalancingEqualyWeightedParticle> particles = new CopyOnWriteArrayList<>();
        //String tikets[] = new String[]{"BND","SUSM-LSE","IFSW-LSE"};
        //String tikets[] = new String[]{"TECS","BND","IAU","UUP"};
        String tikets[] = new String[]{"BND","IAU","SPY","URTH","UUP"};
        double[][] in_data = DataLoader.loadData(tikets);

        for(int i = 0;i < POP_SIZE;i++){
            particles.add(new QuantumETFRebalancingEqualyWeightedParticle(in_data));
        }

        DCAIndividual[] dcas = DataLoader.loadDCAForData(tikets, in_data);

        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumETFRebalancingEqualyWeightedParticle best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }
            double sum[] = new double[tikets.length];
            for(QuantumETFRebalancingEqualyWeightedParticle particle : particles){
                double pw[] = particle.getPw();
                for(int i = 0 ; i < pw.length;i++){
                    sum[i]+=pw[i];
                }
            }
            double c[] = new double[tikets.length];
            for(int i = 0;i < tikets.length;i++){
                c[i] = sum[i]/((double)particles.size());
            }
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
                double[] Gw = Arrays.copyOf(best.getW(),tikets.length);
                particles.parallelStream().forEach(p -> p.setGw(Gw));
            }


            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));

            QuantumETFRebalancingEqualyWeightedParticle finalBest = best;
            IntStream.range(0,tikets.length).forEach(j ->System.out.println("Num of shares of "+tikets[j]+"="+ finalBest.getShares()[j]));

            System.out.println("cash ="+best.getCash());
            System.out.println(String.format("Invested=%f",best.getInvestment()));
            System.out.println("Number of rebalances = "+best.getRebalances());

            particles.parallelStream().forEach(p->p.reinitialize());

            iterations++;
        }
        System.out.println("----RESULTS-----");
        System.out.println("DAYS:"+in_data[0].length);
        System.out.println(String.format("Best fitness = %f", best.getFitness()));
        IntStream.range(0,tikets.length).forEach(j -> System.out.println("individual "+tikets[j]+" = "+dcas[j].total()));

    }



}
