package org.xSakix.individuals;

import cern.colt.list.DoubleArrayList;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.finance.tools.basics.Return;

import java.io.IOException;
import java.util.Arrays;

public class DCAIndividual {

    private double data[];

    private double cash = 0;
    private double sum = cash;
    private int shares = 0;
    private DoubleArrayList returns = new DoubleArrayList();
    private DoubleArrayList totals = new DoubleArrayList();
    private double year_total = 0.;
    private double[] returnsDaily;

    private static final double TRANSACTION_COST = 2.0;

    public DCAIndividual(double[] data) {
        init();
        this.data = data;
        this.returnsDaily = new double[this.data.length];
    }

    private void init() {
        cash = 300.0;
        sum = cash;
        shares = 0;
    }


    public void simulate() {
        for (int i = 0; i < data.length; i++) {
            double price = data[i];

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

            // buy akcia
            if (this.cash >= price) {
                int num_shares = (int) Math.round((this.cash-TRANSACTION_COST) / price);
                this.cash = this.cash - (price * num_shares)-TRANSACTION_COST;
                this.shares += num_shares;
            }

            double total = shares * price + cash;
            totals.add(total);
            returnsDaily[i] = (total - sum)/sum;
        }
    }

    public double total() {
        return cash + data[data.length - 1] * shares;
    }

    public double[] getTotals(){
        return Arrays.copyOfRange(this.totals.elements(),0,this.totals.size());
    }

    public void print() {
         System.out.println( "fitness investment: " + this.sum);
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


    public static void main(String[] args) throws IOException {
        String ticket = "SPY";

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\" + ticket + ".csv");
        data = Arrays.copyOfRange(data,1000,data.length);
        DCAIndividual dca = new DCAIndividual(data);
        dca.simulate();
        dca.print();
        System.out.println("avg returns (arithmetic): "+dca.arithmeticMeanOfReturns());
        System.out.println("avg returns (geometric): "+dca.geometricMeanOfReturns());
    }

    public double[] getReturnsDaily() {
        return returnsDaily;
    }
}
