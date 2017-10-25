package org.xSakix.ga;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import org.xSakix.etfgrowth.Individual;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.gatools.FloatOperations;
import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ETFGrowthGA {

    public static String logName = "result.csv";

    public Individual createChild(Individual parent1, Individual parent2,double data[]){
        Individual child = new Individual(data);
        double rc[] = new double[3];

        double rc1[] = parent1.getRC();
        double rc2[] = parent2.getRC();

        for(int i = 0; i < 3;i++) {
            rc[i] = FloatOperations.cross(rc1[i], rc2[i]);
        }

        child.setRC(rc);

        return child;
    }

    public void mutateChild(Individual individual){
        double rc[] = new double[3];
        double rc_individual[] = individual.getRC();

        for(int i = 0;i < 3;i++){
            rc[i] = FloatOperations.mutate(rc_individual[i]);
        }

        individual.setRC(rc);
    }

    private class MaxTuple{
        double max = 0.;
        Individual best = null;
    }

    private MaxTuple findBest(List<Individual> individuals){
        MaxTuple result = new MaxTuple();
        for(Individual individual : individuals){
            if(individual.total() > result.max){
                result.max = individual.total();
                result.best = individual;
            }
        }

        return result;
    }

    public void print_sim_report(Individual individual) {
        individual.print();
    }

    public void run(String ticket) throws IOException, CloneNotSupportedException {

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\" + ticket + ".csv");
        int population_max = 100;
        
        List<Individual> individuals = new CopyOnWriteArrayList<Individual>();

        DoubleArrayList hist_totals = new DoubleArrayList();

        int iterations = 0;

        Individual best_of_all = null;


        for (int i = 0; i< population_max;i++) {
            individuals.add(new Individual(data));
        }

        while (true) {

            for (Individual individual : individuals) {
                individual.initialHeat();
                individual.simulate();
            }

            MaxTuple result = findBest(individuals);
            hist_totals.add(result.max);
            //System.out.println(iterations + "=" + result.max);
            //System.out.println();

            if (best_of_all == null || best_of_all.total() < result.max) {
                best_of_all = result.best.clone();
                //System.out.println("best of all:" + best_of_all.total());
            }

            if (iterations > 1000) {
            //if (best_of_all.total() > 800000.0) {
                print_sim_report(best_of_all);
                break;
            }

            individuals.remove(result.best);
            MaxTuple result2 = findBest(individuals);
            individuals.remove(result2.best);

            List<Individual> individuals2 = new CopyOnWriteArrayList<>();
            result.best.reinitialize();
            result2.best.reinitialize();

            individuals2.add(result.best);
            individuals2.add(result2.best);

//            System.out.println(Arrays.toString(result.best.getRC()));
//            System.out.println(Arrays.toString(result2.best.getRC()));

            while (individuals2.size() < population_max) {
                Individual child1 = createChild(result.best, result2.best, data);
                if (Uniform.staticNextDoubleFromTo(0., 1.) < 0.05) {
                    mutateChild(child1);
                }

                Individual child2 = createChild(result2.best, result.best, data);
                if (Uniform.staticNextDoubleFromTo(0., 1.) < 0.05) {
                    mutateChild(child2);
                }

                individuals2.add(child1);
                individuals2.add(child2);
                result = findBest(individuals);
                individuals.remove(result.best);
                result2 = findBest(individuals);
                individuals.remove(result2.best);
            }

            individuals.clear();
            individuals.addAll(individuals2);

            //System.out.println("----------------------------------------");
            iterations++;
        }
        File file = new File(logName);
        if(!file.exists()){
            file.createNewFile();
        }
        String content = ticket+","+Arrays.toString(best_of_all.getRC())+"\r\n";
        Files.write(file.toPath(),content.getBytes("UTF-8"), StandardOpenOption.APPEND);
    }

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        ETFGrowthGA ga = new ETFGrowthGA();

        Path dir = Paths.get("c:\\downloaded_data\\USD\\");

        System.out.println(dir);

        Files.list(dir).forEach(f -> {
            try {
                ga.run(f.getFileName().toString().replace(".csv", ""));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });

    }

}
