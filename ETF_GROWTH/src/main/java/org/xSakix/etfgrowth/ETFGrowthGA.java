package org.xSakix.etfgrowth;

import cern.jet.random.Uniform;
import cern.jet.stat.Descriptive;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.individuals.Individual;
import org.xSakix.gatools.FloatOperations;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ETFGrowthGA {

    public static final double RATE_OF_CHANGE = 10.;
    public static String logName = "result.csv";
    private double[] hist_totals;
    int iter_max=2000;
    int population_max = 1000;
    public double mut_rate_history[] = new double[iter_max];
    public double s_rate_history[] = new double[iter_max];

    public Individual createChild(Individual parent1, Individual parent2, double data[]) {
        Individual child = new Individual(data);
        double rc[] = new double[3];

        double rc1[] = parent1.getRC();
        double rc2[] = parent2.getRC();

        for (int i = 0; i < 3; i++) {
//            rc[i] = FloatOperations.crossRand(rc1[i], rc2[i]);
            rc[i] = FloatOperations.cross(rc1[i], rc2[i]);
        }

        child.setRC(rc);

        return child;
    }

    public void mutateChild(Individual individual) {
        double rc[] = new double[3];
        double rc_individual[] = individual.getRC();

        for (int i = 0; i < 3; i++) {
            //rc[i] = FloatOperations.mutate(rc_individual[i]);
            rc[i] = FloatOperations.gausianMutation(rc_individual[i],0.2);
        }

        individual.setRC(rc);
    }


    public void print_sim_report(Individual individual) {
        individual.print();
    }

    public Individual run(double[] data) throws IOException, CloneNotSupportedException {



        List<Individual> individuals = new CopyOnWriteArrayList<>();

        hist_totals = new double[iter_max];

        int iterations = 0;
        int success_rate = (int) RATE_OF_CHANGE;

        Individual best_of_all = null;
        double last_avg_best = 0.;
        double mut_rate = 0.9;

        DCAIndividual dca = new DCAIndividual(data);
        dca.simulate();

        //preselection, atleast 1/3 has to have better then dca results
        preselection(data, population_max, individuals, dca);


        while (true) {

            if (iterations >= iter_max) {
                print_sim_report(best_of_all);
                break;
            }

            individuals.parallelStream().forEach(i -> {
                i.initialHeat();
                i.simulate();
            });


            individuals.sort((o1, o2) -> {
                if(o1.total() > o2.total())
                    return -1;
                if(o1.total() < o2.total())
                    return 1;
                return 0;
            });

            hist_totals[iterations] = individuals.get(0).total();
            System.out.println(iterations + "=" + individuals.get(0).total());
            System.out.println();

            if (best_of_all == null || best_of_all.total() < individuals.get(0).total()) {
                best_of_all = individuals.get(0).clone();
                System.out.println("best of all:" + best_of_all.total());
            }

            //double avgBest = Arrays.stream(individuals.stream().mapToDouble(i -> i.total()).toArray()).sum()/individuals.size();
            double avgBest = individuals.get(0).total();
            avgBest = Math.round(avgBest*100.)/100.;
            //System.out.println(String.format("avg=%f, last avg=%f",avgBest,last_avg_best));

            if(avgBest > last_avg_best  ){
                System.out.println(String.format("avg=%f, last avg=%f",avgBest,last_avg_best));
                last_avg_best=avgBest;
                if(success_rate == 0){
                    success_rate= (int) RATE_OF_CHANGE;
                }
                success_rate++;
                if(success_rate > RATE_OF_CHANGE){
                    success_rate = (int) RATE_OF_CHANGE;
                }
            }else if(avgBest <= last_avg_best){
                success_rate--;
                if(success_rate < 0){
                    success_rate = 0;
                }
            }
            System.out.println(String.format("success = %d",success_rate));
            if(success_rate/RATE_OF_CHANGE > 1./RATE_OF_CHANGE) {
                mut_rate *= 1.5;
                if(mut_rate > 0.9){
                    mut_rate = 0.9;
                }
            }

            if(success_rate/RATE_OF_CHANGE < 1./RATE_OF_CHANGE){
                mut_rate /=1.5;
                if(mut_rate < 0.1){
                    mut_rate = 0.1;
                }
            }
            mut_rate_history[iterations] = mut_rate;
            System.out.println(mut_rate);

            final double[] sum = {0.,0.};
            individuals.parallelStream().forEach(i -> sum[0] += i.total());
            double normal_sum = sum[0];
            individuals.parallelStream().forEach(i -> sum[1] += (i.total()*i.total()));
            double sum_quares = sum[1];
            individuals.parallelStream().forEach(i -> i.setPselect(i.total() / sum[0]));
            double variance = Descriptive.variance(population_max,normal_sum,sum_quares);
            System.out.println(String.format("variance=%f",variance));
            double std = Descriptive.standardDeviation(Math.abs(variance));
            System.out.println(String.format("std=%f",std));
            double avgBefore = Arrays.stream(individuals.stream().mapToDouble(i -> i.total()).toArray()).sum()/individuals.size();


            //selection
            //round robin P(individual) = fitness(individual)/sum(fitness(individual))
            List<Individual> selected = new CopyOnWriteArrayList<>();

//            selected.add(best_of_all);
            while (selected.size() < population_max) {
                individuals.parallelStream().forEach(i -> {
                    if (i.getPselect() > Uniform.staticNextDoubleFromTo(0., 1.) && selected.size() < population_max ) {
                        selected.add(i);
                    }
                });
            }

            double avgAfter = Arrays.stream(selected.stream().mapToDouble(i -> i.total()).toArray()).sum()/selected.size();
            s_rate_history[iterations] = (avgAfter - avgBefore)/std;
            if(Double.isNaN(s_rate_history[iterations])){
                s_rate_history[iterations] = 0.;
            }
            System.out.println(String.format("S=%f, avgafter=%f,avgbefore=%f,std=%f",s_rate_history[iterations],avgAfter,avgBefore,std));


            List<Individual> individuals2 = new ArrayList<>();
            List<Individual> chosen = individuals.subList(0,individuals.size()/10);
            chosen.parallelStream().forEach(ch -> ch.reinitialize());
            individuals2.addAll(chosen);
            for(int i  =0; i < individuals.size()/10;i++){
                individuals2.add(new Individual(data));
            }
            while (individuals2.size() < population_max) {
                int indexParent1 = Uniform.staticNextIntFromTo(0,selected.size()-1);
                int indexParent2 = Uniform.staticNextIntFromTo(0,selected.size()-1);
                Individual first = selected.get(indexParent1);
                Individual last = selected.get(indexParent2);
                Individual child1 = createChild(first, last, data);
                Individual child2 = createChild(last, first, data);
                if (Uniform.staticNextDoubleFromTo(0., 1.) <= mut_rate) {
                    mutateChild(child1);
                    mutateChild(child2);
                }
                individuals2.add(child1);
                individuals2.add(child2);

            }

            individuals.clear();
            individuals.addAll(individuals2);
            selected.clear();
            individuals2.clear();

            System.out.println("----------------------------------------");
            iterations++;
        }

        System.out.println(Arrays.toString(best_of_all.getRC()));

        return best_of_all;
    }

    private void preselection(double[] data, int population_max, List<Individual> individuals, DCAIndividual dca) {
        List<Individual> individualSel = new CopyOnWriteArrayList<Individual>();

        while(individualSel.size() < population_max){
            for (int i = 0; i < population_max; i++) {
                individuals.add(new Individual(data));
            }
            individuals.parallelStream().forEach(i -> {
                i.initialHeat();
                i.simulate();
                if(i.total() >= dca.total()){
                    i.reinitialize();
                    individualSel.add(i);
                }
            });
            individuals.clear();
        }
        individuals.clear();
        individuals.addAll(individualSel);
        individualSel.clear();
    }

    public double[] getHist_totals() {
        return hist_totals;
    }

//    public static void main(String[] args) throws IOException, CloneNotSupportedException {
//        ETFGrowthGA etfgrowth = new ETFGrowthGA();
//
//        Path dir = Paths.get("c:\\downloaded_data\\USD\\");
//
//        System.out.println(dir);
//
//        Files.list(dir).forEach(f -> {
//            try {
//                String ticket = f.getFileName().toString().replace(".csv", "");
//                double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\" + ticket + ".csv");
//
//                Individual best_of_all = etfgrowth.run(data);
//
//                File file = new File(logName);
//                if (!file.exists()) {
//                    file.createNewFile();
//                }
//                String content = ticket + "," + Arrays.toString(best_of_all.getRC()) + "\r\n";
//                Files.write(file.toPath(), content.getBytes("UTF-8"), StandardOpenOption.APPEND);
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (CloneNotSupportedException e) {
//                e.printStackTrace();
//            }
//        });
//
//    }


}
