package org.xSakix.ga;

import cern.colt.list.DoubleArrayList;
import org.math.plot.Plot2DPanel;
import org.xSakix.etfgrowth.DCAIndividual;
import org.xSakix.etfgrowth.Individual;
import org.xSakix.etfgrowth.QuantumIndividual;
import org.xSakix.etfreader.EtfReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuantumETFGrowth {

    public static final int POP_SIZE = 500;
    public static final int ITER_MAX = 800;


    private static void preselection(double[] data, int population_max, List<QuantumIndividual> individuals, DCAIndividual dca) {
        System.out.println("Starting preselection...");

        List<QuantumIndividual> individualSel = new CopyOnWriteArrayList<QuantumIndividual>();

        while(individualSel.size() < population_max){
            for (int i = 0; i < population_max; i++) {
                individuals.add(new QuantumIndividual(data));
            }
            individuals.parallelStream().forEach(i -> {
                i.eval();
                if(i.getFitness() >= dca.total()){

                    individualSel.add(i);
                }
            });
            individuals.clear();
        }
        individuals.clear();
        individuals.addAll(individualSel);
        individualSel.clear();
        System.out.println("Preselection end.");
    }


    public static void main(String[] args) throws IOException, CloneNotSupportedException {

        long time = System.currentTimeMillis();

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");

        File file = new File("result_qpso.csv");
        if(!file.exists()){
            file.createNewFile();
        }else{
            file.delete();
            file.createNewFile();
        }

        for(int i = 0; i < 10;i++) {
            QuantumIndividual best = runQuantumETFGrowth(data);

            best.print();
            Files.write(file.toPath(), (best.getFitness()+"\n").getBytes("UTF-8"), StandardOpenOption.APPEND);
        }

        System.out.println(String.format("Time=%f s",(System.currentTimeMillis()-time)/1000.));

//        Plot2DPanel plot = new Plot2DPanel();
//        plot.addLinePlot("curve_plot", Color.blue,fitnessHistory);
//        Dimension dim = new Dimension(800, 600);
//
//        JFrame frame = new JFrame("SPY");
//        frame.setLayout(new GridLayout());
//        frame.setSize(dim);
//        frame.setMaximumSize(dim);
//        frame.setMinimumSize(dim);
//        frame.add(plot);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static QuantumIndividual runQuantumETFGrowth(double[] data) throws CloneNotSupportedException {
        List<QuantumIndividual> particles = new CopyOnWriteArrayList<>();

        DCAIndividual dca = new DCAIndividual(data);
        dca.simulate();

        preselection(data,POP_SIZE,particles,dca);

        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumIndividual best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }
            double sum[] = new double[3];
            for(QuantumIndividual particle : particles){
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

            particles.parallelStream().forEach(p -> {
                p.computeWeights();
                p.eval();
            });

            //sorts asc
            Collections.sort(particles, (o1, o2) -> {
                if(o1.getFitness() > o2.getFitness())
                    return -1;
                if(o1.getFitness() < o2.getFitness())
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

            iterations++;
        }
        return best;
    }

}
