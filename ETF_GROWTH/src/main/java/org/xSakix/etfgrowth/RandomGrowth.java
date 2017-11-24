package org.xSakix.etfgrowth;

import org.xSakix.individuals.DCAIndividual;
import org.xSakix.individuals.Individual;
import org.xSakix.etfreader.EtfReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RandomGrowth {

    public static String logName = "result.csv";
    private double[] hist_totals;
    int iter_max=2000;


    public void print_sim_report(Individual individual) {
        individual.print();
    }

    public Individual run(double[] data) throws IOException, CloneNotSupportedException {

        int population_max = 1000;

        List<Individual> individuals = new CopyOnWriteArrayList<>();

        hist_totals = new double[iter_max];

        int iterations = 0;

        Individual best_of_all = null;

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
            individuals.clear();
            for(int i  =0; i < population_max;i++){
                individuals.add(new Individual(data));
            }


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
                individualSel.add(new Individual(data));
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

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        long time = System.currentTimeMillis();
        String ticket = "SPY";

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\" + ticket + ".csv");

        File file = new File("result_random.csv");
        if(!file.exists()){
            file.createNewFile();
        }else{
            file.delete();
            file.createNewFile();
        }

        for(int i = 0; i < 10;i++) {
            RandomGrowth ga = new RandomGrowth();
            Individual best_of_all = ga.run(data);
            best_of_all.print();
            Files.write(file.toPath(), (best_of_all.total()+"\n").getBytes("UTF-8"), StandardOpenOption.APPEND);
        }

        /*System.out.println(String.format("Time=%f s",(System.currentTimeMillis()-time)/1000.));

        Plot2DPanel plot = new Plot2DPanel();
        plot.addLinePlot("curve_plot", Color.blue,etfgrowth.getHist_totals());
        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/

    }


}
