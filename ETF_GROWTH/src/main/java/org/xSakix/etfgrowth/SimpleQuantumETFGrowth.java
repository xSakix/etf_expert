package org.xSakix.etfgrowth;

import org.math.plot.Plot2DPanel;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.individuals.QuantumSimpleIndividual;
import org.xSakix.etfreader.EtfReader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleQuantumETFGrowth {

    public static final int POP_SIZE =400;
    public static final int ITER_MAX = 400;


    private static void preselection(double[] data, List<QuantumSimpleIndividual> individuals, boolean debug) {
        System.out.println("Starting preselection...");
        DCAIndividual dca = new DCAIndividual(data);
        dca.simulate();

        List<QuantumSimpleIndividual> individualSel = new CopyOnWriteArrayList<QuantumSimpleIndividual>();

        double coef = 1.9;
        while(true){
            double finalCoef = coef;
            individuals.parallelStream().forEach(i -> {
                i.eval();
                if(i.getFitness() >= finalCoef *dca.total()){

                    individualSel.add(i);
                }
            });
            if(individualSel.size() >= POP_SIZE) {
                break;
            }
            individuals.clear();
            for (int i = 0; i < POP_SIZE*10; i++) {
                individuals.add(new QuantumSimpleIndividual(data));
            }
            coef -=0.05;
        }
        individuals.clear();
        individuals.addAll(individualSel);
        individualSel.clear();
        System.out.println("Preselection end.");
    }


    public static void main(String[] args) throws IOException, CloneNotSupportedException {

//        long time = System.currentTimeMillis();
//
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
//
//        File file = new File("result_qpso.csv");
//        if(!file.exists()){
//            file.createNewFile();
//        }else{
//            file.delete();
//            file.createNewFile();
//        }
//
//        for(int i = 0; i < 10;i++) {
            QuantumSimpleIndividual best = runQuantumETFGrowth(data,true);

            best.print();
//            Files.write(file.toPath(), (best.getFitness()+"\n").getBytes("UTF-8"), StandardOpenOption.APPEND);
//        }
//
//        System.out.println(String.format("Time=%f s",(System.currentTimeMillis()-time)/1000.));


    }

    public static QuantumSimpleIndividual runQuantumETFGrowth(double[] data, boolean debug) throws CloneNotSupportedException {
        List<QuantumSimpleIndividual> particles = new CopyOnWriteArrayList<>();


        for (int i = 0; i < POP_SIZE; i++) {
            particles.add(new QuantumSimpleIndividual(data));
        }
        long time = System.currentTimeMillis();
        preselection(data,particles,debug);
        if(debug) {
            System.out.println(String.format("Preselection time=%d", System.currentTimeMillis() - time));
        }

        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumSimpleIndividual best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }
            double sum = 0.;
            for(QuantumSimpleIndividual particle : particles){
                sum+=particle.getPw();
            }
            double c = sum/((double)particles.size());
            particles.parallelStream().forEach(p -> p.setC(c));

            particles.parallelStream().forEach(p -> {
                p.computeWeights();
                p.eval();
            });

            //sorts asc
            particles.sort((o1, o2) -> {
                if (o1.getFitness() > o2.getFitness())
                    return -1;
                if (o1.getFitness() < o2.getFitness())
                    return 1;
                return 0;
            });


            if (best == null || best.getFitness() < particles.get(0).getFitness()) {
                best = particles.get(0).clone();
                double Gw = best.getW();
                particles.parallelStream().forEach(p -> p.setGw(Gw));
            }

            fitnessHistory[iterations] = best.getFitness();
            if(debug) {
                System.out.println(String.format("Iteration = %d", iterations));
                System.out.println(String.format("Best fit weights = %s", best.getW()));
                System.out.println(String.format("Best fitness = %f", best.getFitness()));
                System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));
            }

            iterations++;
        }
        if(debug) {
            Plot2DPanel plot = new Plot2DPanel();
            plot.addLinePlot("curve_plot", Color.blue, fitnessHistory);
            Dimension dim = new Dimension(800, 600);

            JFrame frame = new JFrame("SPY");
            frame.setLayout(new GridLayout());
            frame.setSize(dim);
            frame.setMaximumSize(dim);
            frame.setMinimumSize(dim);
            frame.add(plot);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }


        return best;
    }

}
