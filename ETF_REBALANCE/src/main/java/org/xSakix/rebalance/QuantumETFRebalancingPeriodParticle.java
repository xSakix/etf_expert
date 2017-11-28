package org.xSakix.rebalance;

import cern.jet.random.Uniform;

import java.util.Arrays;


public class QuantumETFRebalancingPeriodParticle {

    private double w[];
    private double Pw[];
    private double Gw[];
    private double C[];
    private int M;
    private double fitness;
    private double lastFitness = Double.NaN;
    private double rms;
    private double alpha;
    private double[] data;
    private double[] data2;

    private double cash = 300.;
    private double investment = 300.;
    private int shares1 = 0;
    private int shares2 = 0;
    private double tr_cost = 4.;


    public QuantumETFRebalancingPeriodParticle(double data[], double data2[]) {
        init(data,data2);
    }

    private void init(double[] data,double data2[]) {
        M = 3;
        this.data = Arrays.copyOf(data,data.length);
        this.data2 = Arrays.copyOf(data2,data2.length);

        this.w = new double[M];
        this.Pw = new double[M];
        this.Gw = new double[M];
        this.C = new double[M];
        //0 - % of first ETF
        //1 - % of second ETF
        //3 - rebalance period
        w[0] = Uniform.staticNextDoubleFromTo(0,1.);
        w[1] = (1.-w[0]);
        w[2] = Uniform.staticNextDoubleFromTo(0.1,1.);

        for(int i = 0; i < M;i++){
            Pw[i] = w[i];
            Gw[i] = w[i];
        }
        this.alpha = 0.75;

    }

    public void computeWeights(){
        computeOne(0 );
        computeOne(2 );
        if(w[0] > 0.99){
            w[0] = 0.99;
        }else if(w[0] < 0.01){
            w[0] = 0.01;
        }
        w[1] = 1. - w[0];
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
        int reb = (int)(w[2]*365);
        for(int i=0; i < data.length;i++) {
            //rebalance -zjednodusene, spravime
            if( reb >0 && i % reb == 0){
                double c1 = (double)shares1*data[i];
                double c2 = (double)shares2*data2[i];
                cash+= c1+c2 - 2*tr_cost;
                shares2 = 0;
                shares1 = 0;
            }

            if(i % 30 == 0 || (reb > 0 && i % reb == 0)){
                if(i % 30 == 0) {
                    cash += 300.;
                    investment += 300.;
                }

                double c1 = w[0]*cash;
                double c2 = cash - c1;
                //buy
                double price1 = data[i];
                double price2 = data2[i];
                int s1 = (int) ((c1-tr_cost)/price1);
                int s2 = (int) ((c2-tr_cost)/price2);
                if(s1 > 0) {
                    cash -= ((double) s1 * price1)-tr_cost;
                }
                if(s2>0) {
                    cash -= ((double) s2 * price2)-tr_cost;
                }

                shares1 += s1;
                shares2 += s2;
            }
        }

        return cash+((double)shares1*data[data.length-1])+((double)shares2*data2[data2.length-1]);
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

    public double getRms() {
        return rms;
    }

    public void setC(double CC[]){
        this.C= Arrays.copyOf(CC,CC.length);
    }

    @Override
    public QuantumETFRebalancingPeriodParticle clone() throws CloneNotSupportedException {
        QuantumETFRebalancingPeriodParticle individual = new QuantumETFRebalancingPeriodParticle(this.data,this.data2);

        individual.w = Arrays.copyOf(w,w.length);
        individual.Pw = Arrays.copyOf(this.Pw,3);
        individual.Gw = Arrays.copyOf(this.Gw,3);
        individual.C = Arrays.copyOf(this.C,3);
        individual.cash = this.cash;
        individual.investment = this.investment;
        individual.shares1 = this.shares1;
        individual.shares2 = this.shares2;

        individual.alpha = this.alpha;
        individual.fitness = this.fitness;
        individual.lastFitness = this.lastFitness;

        return individual;
    }

    public void reinitialize(){
        cash = investment = 300.;
        shares1 = 0;
        shares2 = 0;
    }

    public int getShares1() {
        return shares1;
    }

    public int getShares2() {
        return shares2;
    }

    public double getCash() {
        return cash;
    }
}
