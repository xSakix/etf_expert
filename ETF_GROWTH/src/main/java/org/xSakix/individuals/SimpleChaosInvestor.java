package org.xSakix.individuals;

import org.xSakix.functions.Functions;

public class SimpleChaosInvestor {

    private double r_c;

    private double c;

    public SimpleChaosInvestor(double r_c) {
        this.r_c = r_c;
        c = 0.01;
    }

    private void initialHeat() {
        for (int i = 0; i < 20; i++) {
            c = Functions.computeYorke(this.c, this.r_c);
        }
    }

    public void computeCycles(int cycles){
        initialHeat();
        for(int i = 0; i < cycles;i++){
            c = Functions.computeYorke(this.c, this.r_c);
        }
    }

    public InvestorAction getAction(){
        double buy_choice = 0.90;
        double sell_choice = 0.10;
        // buy akcia
        if (this.c >= buy_choice) {
          return InvestorAction.BUY;
        }
        // sell akcia
        else if (this.c <= sell_choice) {
            return InvestorAction.SELL;
        }

        return InvestorAction.HOLD;
    }

}
