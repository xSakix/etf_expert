package org.xSakix.portfolio;

import cern.jet.random.Uniform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


public class QuantumETFPortfolioParticle {

    private double w[];
    private double Pw[];
    private double Gw[];
    private double C[];
    private int M;
    private double fitness;
    private double lastFitness = Double.NaN;
    private double alpha;
    private double[][] data;
    private double cash = 300.;
    private double investment = 300.;
    private int[] shares;
    private double tr_cost = 2.;
    private List<double[]> shares_history;
    private double[] value;
    private double[] invested;


    public QuantumETFPortfolioParticle(double data[][]) {
        init(data);
    }

    private void init(double[][] data) {
        M = data.length ;
        this.data = Arrays.copyOf(data,data.length);
        shares = new int[M];
        this.w = new double[M];
        this.Pw = new double[M];
        this.Gw = new double[M];
        this.C = new double[M];
        //0 - % of first ETF
        //1 - % of second ETF
        //3 - rebalance period
        for(int i = 0; i < M;i++) {
            w[i] = Uniform.staticNextDoubleFromTo(0, 1.);
        }
        double sum = Arrays.stream(w).sum();
        for(int i = 0; i < M;i++) {
            w[i] = w[i]/sum;
        }

        for(int i = 0; i < M;i++){
            Pw[i] = w[i];
            Gw[i] = w[i];
        }
        this.alpha = 0.75;
        shares_history = new ArrayList<>(data.length);
        IntStream.range(0,data.length).forEach(i -> shares_history.add(new double[data[0].length]));
        value = new double[data[0].length];
        invested = new double[data[0].length];
    }

    public void computeWeights(){
        for(int i = 0; i < M ;i++) {
            computeOne(i);
        }
        double sum = Arrays.stream(w).sum();
        for(int i = 0; i < M;i++) {
            w[i] = w[i]/sum;
        }
    }

    private void computeOne(int i) {
        double phi = Uniform.staticNextDoubleFromTo(.0,1.);
        double p = phi*Pw[i]+(1-phi)*Gw[i];
        double u = Uniform.staticNextDoubleFromTo(0.,1.);
        if(Uniform.staticNextDoubleFromTo(0.,1.) < 0.5){
            w[i] = Math.abs(p+alpha*Math.abs(w[i]-C[i])*Math.log(1/u));
        }else{
            w[i] = Math.abs(p-alpha*Math.abs(w[i]-C[i])*Math.log(1/u));
        }
    }

    public double evaluate(){
        if(investment > cash){
            investment = cash;
        }
        for(int i=0; i < data[0].length;i++) {
            if(i % 30 == 0 ){
                cash += 300.;
                investment += 300.;
                double amount = cash;
                for(int j =0;j < M;j++) {
                    double c1 = w[j] * amount;
                    //buy
                    double price = data[j][i];
                    int s1 = (int) ((c1 - tr_cost) / price);
                    if (s1 > 0) {
                        cash -= ((double) s1 * price) - tr_cost;
                    }
                    shares[j] += s1;
                }
            }

            int finalI = i;
            IntStream.range(0,shares_history.size()).forEach(j -> shares_history.get(j)[finalI]=shares[j]);
            invested[i] = this.investment;
            value[i] = computeTotal();
        }

        return computeTotal();
    }

    private double computeTotal(){
        final double[] total = {cash};
        IntStream.range(0,M).forEach(j-> total[0] += (double)shares[j]*data[j][data[j].length-1]);
        return total[0];
    }


    public double computeFitness(){
        fitness = evaluate();

        if(Double.isNaN(lastFitness)){
            lastFitness = fitness;
        }
        if(fitness < lastFitness){
            Pw = Arrays.copyOf(w,w.length);
        }

        return fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public double[] getW() {
        return w;
    }

    public void setW(double[] ww) {
        this.w = Arrays.copyOf(ww,ww.length);
    }

    public double[] getPw() {
        return Pw;
    }

    public void setPw(double[] pw) {
        Pw = Arrays.copyOf(pw,pw.length);
    }

    public double[] getGw() {
        return Gw;
    }

    public void setGw(double[] gw) {
        Gw = Arrays.copyOf(gw,gw.length);
    }


    public void setC(double CC[]){
        this.C= Arrays.copyOf(CC,CC.length);
    }

    @Override
    public QuantumETFPortfolioParticle clone() throws CloneNotSupportedException {
        QuantumETFPortfolioParticle individual = new QuantumETFPortfolioParticle(this.data);

        individual.data = Arrays.copyOf(data,data.length);
        individual.w = Arrays.copyOf(w,w.length);
        individual.Pw = Arrays.copyOf(this.Pw,M);
        individual.Gw = Arrays.copyOf(this.Gw,M);
        individual.C = Arrays.copyOf(this.C,M);
        individual.cash = this.cash;
        individual.investment = this.investment;
        individual.shares = Arrays.copyOf(this.shares,this.shares.length);

        individual.alpha = this.alpha;
        individual.fitness = this.fitness;
        individual.lastFitness = this.lastFitness;
        individual.shares_history = new ArrayList<>(this.shares_history);

        individual.value = Arrays.copyOf(this.value,this.value.length);
        individual.invested = Arrays.copyOf(this.invested,this.invested.length);

        return individual;
    }

    public void reinitialize(){
        cash = investment = 300.;
        IntStream.range(0,M).forEach(j -> shares[j]=0);
        shares_history = new ArrayList<>(data.length);
        IntStream.range(0,data.length).forEach(i -> shares_history.add(new double[data[0].length]));
        this.value = new double[data[0].length];
        this.invested = new double[data[0].length];
    }

    public int[] getShares() {
        return shares;
    }

    public double getCash() {
        return cash;
    }

    public double getInvestment() {
        return investment;
    }

    public List<double[]> getShares_history() {
        return shares_history;
    }

    public double[] getValue() {
        return value;
    }

    public double[] getInvested() {
        return invested;
    }
}
