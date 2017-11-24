package org.xSakix.etfgrowth;

import org.xSakix.individuals.Individual;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.finance.tools.basics.Return;

import java.io.IOException;



public class OneSimulation {
    public static void main(String[] args) throws IOException {
//        String ticket = "SPY";
//        double data[] = EtfReader.readEtf("c:\\WORK folders\\etf_expert\\py_code\\etf_data\\" + ticket + ".csv");

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\latest\\EWS.csv");

        System.out.println(data.length);

        Individual individual = new Individual(data);
        //SPY=812216.6370829993
        //individual.setRC(new double[]{3.792741215699916, 3.961690242868475, 3.69609636766504});
        //EWS=
        individual.setRC(new double[]{3.8410837695175823, 3.8164607010536833, 3.6137613710273344});
        individual.initialHeat();
        individual.simulate();
        individual.print();

        System.out.println(String.format("Return:%.3f %%",Return.returnt(individual.total(),individual.invested(),0.)*100.));
        System.out.println(String.format("Arithmetic mean of returns: %.3f %%",individual.arithmeticMeanOfReturns()*100.));
        System.out.println(String.format("Geometric mean of returns: %f %%",individual.geometricMeanOfReturns()*100.));
    }
}
