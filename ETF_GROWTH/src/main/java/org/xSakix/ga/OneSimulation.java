package org.xSakix.ga;

import org.xSakix.etfgrowth.Individual;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.finance.tools.basics.Return;

import java.io.IOException;



public class OneSimulation {
    public static void main(String[] args) throws IOException {
        String ticket = "SPY";
        double data[] = EtfReader.readEtf("c:\\WORK folders\\etf_expert\\py_code\\etf_data\\" + ticket + ".csv");

        System.out.println(data.length);

        Individual individual = new Individual(data);
        //individual.setRC(new double[]{3.7248023104723726, 3.7434059826819586, 3.6937761867181664});
        //individual.setRC(new double[]{3.5558226003907123, 3.965694510786344, 3.6113613485193024});
        //individual.setRC(new double[]{3.741572968728748, 3.828705006532029, 3.608757957979064});
        //individual.setRC(new double[]{3.4008433173876256, 3.635041022207588, 3.5476736903656274});
        //individual.setRC(new double[]{3.680271659977734, 3.7554224950727075, 3.2690604641102254});
        //individual.setRC(new double[]{3.6026555818529764, 3.9063400710300296, 3.652681255563083});
        //812216.6370829993
        individual.setRC(new double[]{3.792741215699916, 3.961690242868475, 3.69609636766504});
        //individual.setRC(new double[]{3.6166796826390666, 3.9945711385656337, 3.6422576390673185});
        individual.initialHeat();
        individual.simulate();
        individual.print();

        System.out.println(String.format("Return:%.3f %%",Return.returnt(individual.total(),individual.invested(),0.)*100.));
        System.out.println(String.format("Arithmetic mean of returns: %.3f %%",individual.arithmeticMeanOfReturns()*100.));
        System.out.println(String.format("Geometric mean of returns: %f %%",individual.geometricMeanOfReturns()*100.));
    }
}
