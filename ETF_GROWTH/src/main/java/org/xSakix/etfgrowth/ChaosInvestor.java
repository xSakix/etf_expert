package org.xSakix.etfgrowth;

import org.xSakix.etfreader.EtfReader;
import org.xSakix.functions.Functions;

import java.io.IOException;
import java.util.Arrays;

public class ChaosInvestor {

    private double r_c[];

    private double c[];

    public ChaosInvestor(double[] r_c) {
        this.r_c = Arrays.copyOf(r_c,r_c.length);
        c = new double[]{0.01, 0.01, 0.01};
    }

    private void initialHeat() {
        for (int i = 0; i < 20; i++) {
            c = Arrays.copyOf(Functions.computeYorke(this.c, this.r_c), 3);
        }
    }

    public void computeCycles(int cycles){
        initialHeat();
        for(int i = 0; i < cycles;i++){
            c = Arrays.copyOf(Functions.computeYorke(this.c, this.r_c), 3);
        }
    }

    public InvestorAction getAction(){
        double choice = Functions.computeChoice(0.1, 0.9, true);
        if (this.c[0] > choice) {
            return InvestorAction.HOLD;
        }
        // buy akcia
        if (this.c[1] > choice) {
           return InvestorAction.BUY;
        }

        // sell akcia
        if (this.c[2] > choice) {
            return InvestorAction.SELL;
        }

        return InvestorAction.HOLD;
    }

    public static void main(String[] args) throws IOException {
        ChaosInvestor ews = new ChaosInvestor(new double[]{3.8410837695175823, 3.8164607010536833, 3.6137613710273344});
        ChaosInvestor ewa = new ChaosInvestor(new double[]{2.3973905509011666, 3.814406957525075, 3.7383083265430215});
        ChaosInvestor agg = new ChaosInvestor(new double[]{3.9079379787440263, 3.901237042216055, 3.759697663728203});

        int lenEws = EtfReader.readEtf("c:\\downloaded_data\\latest\\EWS.csv").length;
        int lenEwa = EtfReader.readEtf("c:\\downloaded_data\\latest\\EWA.csv").length;
        int lenAgg = EtfReader.readEtf("c:\\downloaded_data\\latest\\AGG.csv").length;
        ews.computeCycles(lenEws);
        ewa.computeCycles(lenEwa);
        agg.computeCycles(lenAgg);
        System.out.println("Action ews:"+ews.getAction());
        System.out.println("Action ewa:"+ewa.getAction());
        System.out.println("Action agg:"+agg.getAction());
    }
}
