package org.xSakix.rebalance;

import org.xSakix.etfreader.EtfReader;
import org.xSakix.individuals.DCAIndividual;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RebalancePeriod {
    static int POP_SIZE = 400;
    static int ITER_MAX = 400;


    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        List<QuantumETFRebalancingPeriodParticle> particles = new CopyOnWriteArrayList<>();
        String tiket1= "AGG";
        String tiket2 = "SPY";
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\"+tiket1+".csv");
        double data2[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\"+tiket2+".csv");

        data = Arrays.copyOfRange(data,0, data.length/4);

        if(data.length > data2.length){
            int rozdiel = data.length - data2.length;
            data = Arrays.copyOfRange(data,rozdiel,data.length);
        }else if(data2.length > data.length){
            int rozdiel = data2.length - data.length;
            data2 = Arrays.copyOfRange(data2,rozdiel,data2.length);
        }

        for(int i = 0;i < POP_SIZE;i++){
            particles.add(new QuantumETFRebalancingPeriodParticle(data,data2));
        }

        DCAIndividual dca1 = new DCAIndividual(data);
        dca1.simulate();
        DCAIndividual dca2 = new DCAIndividual(data2);
        dca2.simulate();

        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumETFRebalancingPeriodParticle best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }
            double sum[] = new double[3];
            for(QuantumETFRebalancingPeriodParticle particle : particles){
                double pw[] = particle.getPw();
                for(int i = 0 ; i < pw.length;i++){
                    sum[i]+=pw[i];
                }
            }
            double c[] = new double[3];
            for(int i = 0;i < 3;i++){
                c[i] = sum[i]/((double)particles.size());
            }
            particles.parallelStream().forEach(p -> p.setC(c));

            particles.stream().forEach(p -> {
                p.computeWeights();
                p.evaluate();
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
                double[] Gw = Arrays.copyOf(best.getW(),3);
                particles.parallelStream().forEach(p -> p.setGw(Gw));
            }


            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));
            System.out.println("Num of shares of "+tiket1+"="+best.getShares1());
            System.out.println("Num of shares of "+tiket2+"="+best.getShares2());
            System.out.println("cash ="+best.getCash());

            particles.parallelStream().forEach(p->p.reinitialize());

            iterations++;
        }
        System.out.println("----RESULTS-----");
        System.out.println(String.format("Best fitness = %f", best.getFitness()));
        System.out.println("individual 1 = "+dca1.total());
        System.out.println("individual 2 = "+dca2.total());
    }
}
