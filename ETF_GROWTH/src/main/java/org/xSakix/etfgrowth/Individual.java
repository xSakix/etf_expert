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

    private double data[];

    private double r_c[];

    private double c[];

    private double cash = 300.0;
    private double sum = cash;
    private int shares = 0;
    private List<DoubleArrayList> hist_c ;
    private IntArrayList hist_shares ;
    private DoubleArrayList hist_cash;
    private int num_hold = 0;
    private int num_buy = 0;
    private int num_sell = 0;
    private DoubleArrayList returns = new DoubleArrayList();
    private double year_total = 0.;

    public Individual(double[] data) {
        init();
        this.data = data;
        r_c = new double[]{
                Uniform.staticNextDoubleFromTo(3., 4.),
                Uniform.staticNextDoubleFromTo(3., 4.),
                Uniform.staticNextDoubleFromTo(3., 4.)
        };
    }

    private void init() {
        c = new double[]{0.01, 0.01, 0.01};
        cash = 300.0;
        sum = cash;
        shares = 0;
        hist_c = new ArrayList<>(3);
        for(int i = 0; i < 3;i++){
            hist_c.add(new DoubleArrayList());
        }
        hist_shares = new IntArrayList();
        hist_cash = new DoubleArrayList();
        num_hold = 0;
        num_buy = 0;
        num_sell = 0;
    }

    public void initialHeat() {
        for (int i = 0; i < 20; i++) {
            c = Arrays.copyOf(Functions.computeYorke(this.c, this.r_c, this.hist_c), 3);
        }
    }

    public void simulate() {
        for (int i = 0; i < data.length; i++) {
            double price = data[i];
            hist_shares.add(shares);
            hist_cash.add(cash + price * shares);

            //vypocitaj growth factor
            c = Arrays.copyOf(Functions.computeYorke(this.c, this.r_c, this.hist_c), 3);

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
                int num_shares = (int) Math.round(this.cash / price);
                this.cash -= price * num_shares;
                this.shares += num_shares;
                action_performed = true;
            }

            // sell akcia
            if (this.c[2] > choice) {
                this.num_sell += 1;
                this.cash += price * this.shares;
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
        individual.hist_c = new ArrayList<>(this.hist_c);
        individual.hist_shares = new IntArrayList(this.hist_shares.elements());
        individual.hist_cash = new DoubleArrayList(this.hist_cash.elements());
        individual.num_hold = this.num_hold;
        individual.num_buy = this.num_buy;
        individual.num_sell = this.num_sell;

        return individual;
    }

    public void print() {
         System.out.println( "total investment: " + this.sum);
         System.out.println( "r_hold: " + this.r_c[0]);
         System.out.println( "r_buy: " + this.r_c[1]);
         System.out.println( "r_sell: " + this.r_c[2]);
         System.out.println( "num of shares: " + this.shares);
         System.out.println( "available cash: " + this.cash);
         System.out.println( "total: " + this.total());
    }

    public double invested(){
        return this.sum;
    }

    public double arithmeticMeanOfReturns(){
        return Return.aritmeticMean(Arrays.copyOfRange(returns.elements(),0,returns.size()));
    }

    public double geometricMeanOfReturns(){
        return Return.geometricMean(Arrays.copyOfRange(returns.elements(),0,returns.size()));
    }
}
