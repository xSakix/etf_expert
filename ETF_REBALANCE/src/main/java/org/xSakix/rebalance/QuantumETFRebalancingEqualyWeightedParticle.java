package org.xSakix.rebalance;

import cern.jet.random.Uniform;

import java.util.Arrays;
import java.util.stream.IntStream;


public class QuantumETFRebalancingEqualyWeightedParticle {

    private double w[];
    private double pd[];//portfolio distribution
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
    private int rebalances = 0;
    public boolean addCash = true;
    private double oneTimeInvestment = 0;


    public QuantumETFRebalancingEqualyWeightedParticle(double data[][]) {
        init(data);
    }

    public QuantumETFRebalancingEqualyWeightedParticle(double data[][],double cash, boolean addCash) {
        init(data);
        this.oneTimeInvestment = cash;
        this.cash = cash;
        this.addCash = false;
    }

    private void init(double[][] data) {
        M = data.length;
        this.data = Arrays.copyOf(data,data.length);
        this.pd = new double[data.length];
        shares = new int[data.length];
        this.w = new double[M];
        this.Pw = new double[M];
        this.Gw = new double[M];
        this.C = new double[M];
        //0 - % of first ETF
        //1 - % of second ETF
        //3 - rebalance period
        IntStream.range(0,M).forEach(j ->w[j] = Uniform.staticNextDoubleFromTo(0.,1.));


        IntStream.range(0,data.length).forEach(j -> pd[j] = 1./data.length);

        for(int i = 0; i < M;i++){
            Pw[i] = w[i];
            Gw[i] = w[i];
        }
        this.alpha = 0.75;

    }

    public void computeWeights(){
        for(int i = 0; i < M ;i++) {
            computeOne(i);
//            if(w[i] > 0.2){
//                w[i]=0.2;
//            }else if(w[i] < 0.001){
//                w[i] = 0.001;
//            }
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
            //rebalance -zjednodusene, spravime
            double cc[] = new double[data.length];
            double diff[] = new double[data.length];
            double d[] = new double[data.length];

            int finalI = i;
            IntStream.range(0,data.length).forEach(j->cc[j] =(double) shares[j] * data[j][finalI]);

            double sum = Arrays.stream(cc).sum();
            sum+=cash;

            double finalSum = sum;
            IntStream.range(0,data.length).forEach(j->diff[j] = cc[j] / finalSum);

            sum = Arrays.stream(diff).sum();
            IntStream.range(0,data.length).forEach(j->d[j] = diff[j] - pd[j]);

            boolean rebalance = false;
            for(int j = 0; j < data.length;j++){
                if( Math.abs(d[j]) > 0){
                    rebalance = true;
                    break;
                }
            }
            //boolean rebalance = diff1 != w[0] || diff2 != w[1];
            if(rebalance){
                rebalances++;
                cash= finalSum - (data.length)*tr_cost;
                IntStream.range(0,data.length).forEach(j -> shares[j]=0);
            }

            if(i % 30 == 0  || rebalance){
                if(i % 30 == 0 && addCash) {
                    cash += 300.;
                    investment += 300.;
                }
                double amount = cash;
                for(int j =0;j < data.length;j++) {
                    double k = 0.;
                    if(d[j] > 0){
                        k = pd[j]-w[j]*Math.abs(d[j]);
                        //k = pd[j]-w[j];
                    }else{
                        k = pd[j]+w[j]*Math.abs(d[j]);
                        //k = pd[j]+w[j];
                    }
                    double c1 =  k* amount;
                    //buy
                    double price = data[j][i];
                    int s1 = (int) ((c1 - tr_cost) / price);
                    if (s1 > 0) {
                        cash -= ((double) s1 * price) - tr_cost;
                    }
                    shares[j] += s1;
                }
            }
        }

        return computeTotal();
    }

    private double computeTotal(){
        final double[] total = {cash};
        IntStream.range(0,data.length).forEach(j-> total[0] += (double)shares[j]*data[j][data[j].length-1]);
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
    public QuantumETFRebalancingEqualyWeightedParticle clone() throws CloneNotSupportedException {
        QuantumETFRebalancingEqualyWeightedParticle individual = new QuantumETFRebalancingEqualyWeightedParticle(this.data);

        individual.w = Arrays.copyOf(w,w.length);
        individual.pd = Arrays.copyOf(pd,pd.length);
        individual.Pw = Arrays.copyOf(this.Pw,3);
        individual.Gw = Arrays.copyOf(this.Gw,3);
        individual.C = Arrays.copyOf(this.C,3);
        individual.cash = this.cash;
        individual.investment = this.investment;
        individual.shares = Arrays.copyOf(this.shares,this.shares.length);

        individual.alpha = this.alpha;
        individual.fitness = this.fitness;
        individual.lastFitness = this.lastFitness;

        individual.rebalances = this.rebalances;

        return individual;
    }

    public void reinitialize(){
        if(addCash) {
            cash = 300.;
            investment = cash;
        }else{
            cash = oneTimeInvestment;
            investment = oneTimeInvestment;
        }
        IntStream.range(0,data.length).forEach(j -> shares[j]=0);
        this.rebalances = 0;
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

    public int getRebalances() {
        return rebalances;
    }
}
