package org.xSakix.rebalance;

import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.buyandhold.CryptoBuyAndHoldSimulator;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.utils.DataLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;


public class QuantumStaticCryptoParticle {

    private int M;
    private double Pw[];
    private double Gw[];
    private double C[];
    private double fitness;
    private double lastFitness = Double.NaN;
    private double alpha;
    private double[] w;
    double[][] in_data;

    public QuantumStaticCryptoParticle(double[][] in_data) {
        init(in_data);
    }

    private void init(double[][] in_data) {
        this.in_data = in_data;
        
        M = 1;
        this.w = new double[M];
        this.Pw = new double[M];
        this.Gw = new double[M];
        this.C = new double[M];

        for(int i = 0; i < M;i++){
            w[i] = Uniform.staticNextDoubleFromTo(0.,1.);
            Pw[i] = w[i];
            Gw[i] = w[i];
        }
        this.alpha = 0.75;
        

    }

    public void computeWeights(){
        for(int i = 0; i < M;i++) {
            computeOne(i);
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
        StaticWeightCryptoEWPSimulator simulator = new StaticWeightCryptoEWPSimulator(in_data,w);
        double result =  simulator.evaluate();
        return result;
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
    public QuantumStaticCryptoParticle clone() throws CloneNotSupportedException {
        QuantumStaticCryptoParticle individual = new QuantumStaticCryptoParticle(in_data);

        individual.w = Arrays.copyOf(w,w.length);
        individual.Pw = Arrays.copyOf(this.Pw,3);
        individual.Gw = Arrays.copyOf(this.Gw,3);
        individual.C = Arrays.copyOf(this.C,3);

        individual.alpha = this.alpha;
        individual.fitness = this.fitness;
        individual.lastFitness = this.lastFitness;

        return individual;
    }

    private static final int MAX_DAYS=1407;
    private static final int POP_SIZE = 400;
    private static final int ITER_MAX = 400;

    public static void main(String[] args) throws IOException, CloneNotSupportedException {

        List<QuantumStaticCryptoParticle> particles = new CopyOnWriteArrayList<>();
        String[] chosen = new String[]{"btc","vtc"};
        double[][] in_data = DataLoader.loadData(chosen,MAX_DAYS,"c:\\downloaded_data\\CRYPTO\\",true);

        for(int i = 0;i < POP_SIZE;i++){
            particles.add(new QuantumStaticCryptoParticle(in_data));
        }

        int iterations = 0;

        double fitnessHistory[] = new double[ITER_MAX];
        QuantumStaticCryptoParticle best = null;

        while (true) {
            if (iterations >= ITER_MAX) {
                break;
            }
            int M = in_data[0].length;
            double sum[] = new double[M];
            for(QuantumStaticCryptoParticle particle : particles){
                double pw[] = particle.getPw();
                for(int i = 0 ; i < pw.length;i++){
                    sum[i]+=pw[i];
                }
            }
            double c[] = new double[M];
            for(int i = 0;i < M;i++){
                c[i] = sum[i]/((double)particles.size());
            }
            particles.parallelStream().forEach(p -> p.setC(c));

            particles.stream().forEach(p -> {
                p.computeWeights();
                p.computeFitness();
            });

            //sorts asc
            particles.sort((o1, o2) -> {
                if (o1.getFitness() > o2.getFitness())
                    return -1;
                if (o1.getFitness() < o2.getFitness())
                    return 1;
                return 0;
            });

            fitnessHistory[iterations] = particles.get(0).getFitness();

            if (best == null || best.getFitness() < particles.get(0).getFitness()) {
                best = particles.get(0).clone();
                double[] Gw = Arrays.copyOf(best.getW(),M);
                particles.parallelStream().forEach(p -> p.setGw(Gw));
            }


            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", Arrays.toString(best.getW())));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));

            iterations++;
        }

        CryptoBuyAndHoldSimulator bah = null;
        StaticWeightCryptoEWPSimulator simulator = new StaticWeightCryptoEWPSimulator(in_data,best.getW());
        double result = simulator.evaluate();
        DCAIndividual[] dcas = DataLoader.loadDCAForData(chosen,in_data);
        bah = new CryptoBuyAndHoldSimulator(in_data);
        bah.evaluate();

        System.out.println("----RESULTS-----");
        System.out.println("DAYS:" + in_data[0].length);
        System.out.println("Result="+simulator.computeTotal());
        System.out.println("Investment = " + simulator.getInvestment());
        String[] finalTikets2 = chosen;
        StaticWeightCryptoEWPSimulator finalBest = simulator;
        IntStream.range(0, chosen.length).forEach(j -> System.out.println("Num of shares of " + finalTikets2[j] + "=" + finalBest.getShares()[j]));
        System.out.println("cash =" + simulator.getCash());
        System.out.println("Number of rebalances = " + simulator.getRebalances());
        System.out.println("Returns = "+simulator.getReturns()[simulator.getReturns().length-1]*100. + " %");
        System.out.println("---DCA---");
        for(int i = 0; i < dcas.length;i++){
            System.out.println(String.format("DCA(%s) = %f",chosen[i],dcas[i].total()));
        }
        System.out.println("----B&H---");
        System.out.println("Result="+bah.computeTotal());
        System.out.println("Investment = " + bah.getInvestment());
        CryptoBuyAndHoldSimulator finalBah = bah;
        IntStream.range(0, chosen.length).forEach(j -> System.out.println("Num of shares of " + finalTikets2[j] + "=" + finalBah.getShares()[j]));
        System.out.println("cash =" + bah.getCash());
        System.out.println("Returns = "+bah.getReturns()[bah.getReturns().length-1]*100. + " %");
        System.out.println("----% vahy----");
        System.out.println("AVG % = "+simulator.getW()[0]*100);

        List<double[]> shares_history = simulator.getShares_history();
        Plot2DPanel plot = new Plot2DPanel();
        String[] finalTikets = chosen;
        IntStream.range(0, shares_history.size())
                .forEach(j -> {plot.addLinePlot(finalTikets[j],
                        new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                        shares_history.get(j));
                });
        plot.addLegend("SOUTH");


        List<double[]> shares_history2 = bah.getShares_history();
        Plot2DPanel plot5 = new Plot2DPanel();
        IntStream.range(0, shares_history2.size())
                .forEach(j -> {plot5.addLinePlot(finalTikets[j],
                        new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                        shares_history2.get(j));
                });
        plot5.addLegend("SOUTH");


        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("value",Color.red,simulator.getValue());
        plot2.addLinePlot("invested",Color.blue,simulator.getInvested());
        plot2.addLinePlot("value b&h",Color.black,bah.getValue());
        plot2.addLegend("SOUTH");

        Plot2DPanel plot3 = new Plot2DPanel();
        String[] finalTikets1 = chosen;
        double[][] finalIn_data = in_data;
        IntStream.range(0, in_data.length)
                .forEach(j -> {plot3.addLinePlot(finalTikets1[j],
                        new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                        finalIn_data[j]);
                });
        plot3.addLegend("SOUTH");

        Plot2DPanel plot4 = new Plot2DPanel();
        for(int i = 0; i< chosen.length;i++){
            Color c = new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255));
            plot4.addLinePlot(chosen[i],c,dcas[i].getReturnsDaily());
        }
        plot4.addLinePlot("portfolio",Color.black,simulator.getReturns());
        plot4.addLinePlot("b&h portfolio",Color.red,simulator.getReturns());
        plot4.addLegend("SOUTH");


        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("REBALANCE");
        frame.setLayout(new GridLayout(2,3));
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plot5);
        frame.add(plot2);
        frame.add(plot3);
        frame.add(plot4);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }
}
