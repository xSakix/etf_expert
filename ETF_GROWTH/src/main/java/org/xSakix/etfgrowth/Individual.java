package org.xSakix.etfgrowth;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.jet.random.Uniform;
import org.xSakix.finance.tools.basics.Return;
import org.xSakix.functions.Functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Individual {

    protected static final double TRANSACTION_COST = 4.0;
    protected double data[];

    protected double r_c[];

    protected double c[];

    protected double cash = 300.0;
    protected double sum = cash;
    protected int shares = 0;
    protected int num_hold = 0;
    protected int num_buy = 0;
    protected int num_sell = 0;
    protected DoubleArrayList returns = new DoubleArrayList();
    protected double year_total = 0.;

    protected double Pselect = 0.;


    public Individual(double[] data) {
        init();
        this.data = data;
        r_c = new double[]{
                Uniform.staticNextDoubleFromTo(2.9, 4.),
                Uniform.staticNextDoubleFromTo(2.9, 4.),
                Uniform.staticNextDoubleFromTo(2.9, 4.)
        };
    }

    private void init() {
        c = new double[]{0.01, 0.01, 0.01};
        cash = 300.0;
        sum = cash;
        shares = 0;
        num_hold = 0;
        num_buy = 0;
        num_sell = 0;
    }

    public void initialHeat() {
        for (int i = 0; i < 20; i++) {
            c =Functions.computeYorke(this.c, this.r_c);
        }
    }

    public void simulate() {
        for (int i = 0; i < data.length; i++) {
            double price = data[i];

            //vypocitaj growth factor
            c = Functions.computeYorke(this.c, this.r_c);

            // kazdy mesiac (30dni) investujeme 300
            if (i % 30 == 0) {
                this.cash += 300.;
                this.sum += 300.;
            }

            if(i % 356 == 0){
                if(year_total == 0){
                    year_total = sum;
                }
                double returnt = Return.returnt(this.total(),year_total,0.);
                returns.add(returnt);
                year_total = this.total();
            }

            // nahodny vyber
            double choice = Functions.computeChoice(0.1, 0.9, true);
            boolean action_performed = false;
            // hold akcia
            if (this.c[0] > choice) {
                this.num_hold += 1;
                action_performed = true;
                continue;
            }
            // buy akcia
            if (this.c[1] > choice) {
                this.num_buy += 1;
                int num_shares = (int) Math.round((this.cash-TRANSACTION_COST) / price);
                this.cash = this.cash - (price * num_shares)-TRANSACTION_COST;
                this.shares += num_shares;
                action_performed = true;
            }

            // sell akcia
            if (this.c[2] > choice) {
                this.num_sell += 1;
                this.cash += (price * this.shares)-TRANSACTION_COST;
                this.shares = 0;
                action_performed = true;
            }

            // no-action = hold action
            if (!action_performed) {
                this.num_hold += 1;
            }
        }
    }

    public double total() {
        return cash + data[data.length - 1] * shares;
    }

    public double[] getRC() {
        return Arrays.copyOf(this.r_c, 3);
    }

    public void setRC(double r_c_other[]) {
        this.r_c = Arrays.copyOf(r_c_other, 3);
    }

    public void reinitialize() {
        init();
    }

    @Override
    public Individual clone() throws CloneNotSupportedException {
        Individual individual = new Individual(data);

        individual.r_c = Arrays.copyOf(this.r_c,3);
        individual.c = Arrays.copyOf(this.c,3);
        individual.cash = this.cash;
        individual.sum = this.sum;
        individual.shares = this.shares;
        individual.num_hold = this.num_hold;
        individual.num_buy = this.num_buy;
        individual.num_sell = this.num_sell;

        return individual;
    }

    public void print() {
         System.out.println( "fitness investment: " + this.sum);
         System.out.println( "r_hold: " + this.r_c[0]);
         System.out.println( "r_buy: " + this.r_c[1]);
         System.out.println( "r_sell: " + this.r_c[2]);
         System.out.println( "num of shares: " + this.shares);
         System.out.println( "available cash: " + this.cash);
         System.out.println( "fitness: " + this.total());
    }

    public double invested() {
        return this.sum;
    }

    public double arithmeticMeanOfReturns(){
        return Return.aritmeticMean(Arrays.copyOfRange(returns.elements(),0,returns.size()));
    }

    public double geometricMeanOfReturns(){
        return Return.geometricMean(Arrays.copyOfRange(returns.elements(),0,returns.size()));
    }

    public double getPselect() {
        return Pselect;
    }

    public void setPselect(double pselect) {
        Pselect = pselect;
    }
}
