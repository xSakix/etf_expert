package org.xSakix.individuals;

import cern.jet.random.Uniform;
import org.xSakix.functions.Functions;

public class SimpleIndividual {

    protected static final double TRANSACTION_COST = 4.0;
    protected double data[];

    protected double r_c;

    protected double c;

    protected double cash = 300.0;
    protected double sum = cash;
    protected int shares = 0;
    protected int num_hold = 0;
    protected int num_buy = 0;
    protected int num_sell = 0;
    private double y[];

    protected double Pselect = 0.;


    public SimpleIndividual(double[] data) {
        init();
        this.data = data;
        r_c = Uniform.staticNextDoubleFromTo(3., 4.);
        this.y = new double[data.length];
    }

    private void init() {
        c = 0.01;
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


            double buy_choice = 0.90;
            double sell_choice = 0.10;
            this.y[i] = c;
            // buy akcia
            if (this.c >= buy_choice) {
                this.num_buy += 1;
                int num_shares = (int) Math.round((this.cash-TRANSACTION_COST) / price);
                this.cash = this.cash - (price * num_shares)-TRANSACTION_COST;
                this.shares += num_shares;
            }
            // sell akcia
            else if (this.c <= sell_choice) {
                this.num_sell += 1;
                this.cash += (price * this.shares)-TRANSACTION_COST;
                this.shares = 0;
            }else{
                this.num_hold += 1;
            }
        }
    }

    public double total() {
        return cash + data[data.length - 1] * shares;
    }

    public double getRC() {
        return this.r_c;
    }

    public void setRC(double r_c_other) {
        this.r_c = r_c_other;
    }

    public void reinitialize() {
        init();
    }

    @Override
    public SimpleIndividual clone() throws CloneNotSupportedException {
        SimpleIndividual individual = new SimpleIndividual(data);

        individual.r_c = this.r_c;
        individual.c = this.c;
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
         System.out.println( "r_c: " + this.r_c);
         System.out.println( "num of shares: " + this.shares);
         System.out.println( "available cash: " + this.cash);
         System.out.println( "fitness: " + this.total());
    }

    public double invested() {
        return this.sum;
    }

    public double getPselect() {
        return Pselect;
    }

    public void setPselect(double pselect) {
        Pselect = pselect;
    }

    public double[] getY() {
        return y;
    }
}
