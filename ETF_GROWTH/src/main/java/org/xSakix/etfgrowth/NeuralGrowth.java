package org.xSakix.etfgrowth;

import org.xSakix.etfreader.EtfReader;
import org.xSakix.functions.Functions;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.individuals.QuantumSimpleIndividual;
import org.xSakix.individuals.SimpleNeuralNetIndividual;
import org.xSakix.tools.Errors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NeuralGrowth {

    private static final int ITER_MAX = 100;
    private static final int POP_MAX=2;

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");

        DCAIndividual dca = new DCAIndividual(data);
        dca.simulate();
        double bench = dca.total();

        QuantumSimpleIndividual ind =  SimpleQuantumETFGrowth.runQuantumETFGrowth(data,false);


        List<SimpleNeuralNetIndividual> individuals = new ArrayList<>(POP_MAX);
        while(individuals.size() < POP_MAX){
            individuals.add(new SimpleNeuralNetIndividual(data));
        }
        SimpleNeuralNetIndividual best = null;
        for(int iter = 0; iter < ITER_MAX;iter++){
            individuals.parallelStream().forEach(i->i.simulate());
            Collections.sort(individuals, (o1, o2) -> {
                if(o1.total() > o2.total())
                    return -1;
                if(o1.total() < o2.total())
                    return 1;
                return 0;
            });
            System.out.println(String.format("best(%d)=%f",iter,individuals.get(0).total()));
            System.out.println(String.format("worst(%d)=%f",iter,individuals.get(individuals.size()-1).total()));
            /*double y[] = null;
            if(individuals.get(0).total() > ind.getFitness()) {
                System.out.println("y from NN");
                y = individuals.get(0).y();
            }else{
                System.out.println("y from qpso");
                y = ind.getY();
            }
            System.out.println("LSE="+Errors.leastSquareError(individuals.get(0).y(),ind.getY()));*/
            if(best == null || best.total() < individuals.get(0).total()){
                best = individuals.get(0).clone();
            }


            //double[] finalY = y;
            int finalIter = iter;
            individuals.stream().forEach(i->{
                double[] yi = i.y();
                double penalty = (ind.getFitness()-i.total())/yi.length;
                System.out.println(String.format("Penalty(%d)=%f", finalIter,penalty));
                double sum = Arrays.stream(yi).sum();
                double[] y_pen = Arrays.stream(yi).map(v -> (1./penalty)*(v/sum)).toArray();
                i.retrain(y_pen);
                i.reinitialize();
            });
            System.out.println("--------");

        }

        System.out.println("Best="+best.total());
        System.out.println("DCA="+dca.total());
        System.out.println("QPSO="+ind.getFitness());
    }
}
