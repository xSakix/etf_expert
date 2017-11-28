package org.xSakix.buyandhold;

import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.utils.DataLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class CryptoBuyAndHoldSimulator {

    private double[] pd;
    private double[][] data;
    private double cash = 0;
    private double investment = 0;
    private double[] shares;
    private double tr_cost = 0.0015;
    private List<double[]> shares_history;
    private double[] value;
    private double[] invested;
    private double[] returns;


    public CryptoBuyAndHoldSimulator(double[][] data) {
        this.data = data;
        this.data = Arrays.copyOf(data,data.length);
        this.pd = new double[data.length];
        shares = new double[data.length];

        IntStream.range(0,data.length).forEach(j -> pd[j] = 1./data.length);

        shares_history = new ArrayList<>(data.length);
        IntStream.range(0,data.length).forEach(i -> shares_history.add(new double[data[0].length]));
        value = new double[data[0].length];
        invested = new double[data[0].length];
        this.returns = new double[data[0].length];
    }

    public double evaluate(){
        if(investment > cash){
            investment = cash;
        }
        for(int i=0; i < data[0].length;i++) {

            if(i % 30 == 0){
                cash += 300.;
                investment += 300.;
                double amount = cash;
                for(int j =0;j < data.length;j++) {
                    double c1 = pd[j]* amount;
                    //buy
                    double price = data[j][i];
                    double s1 = (c1 - c1*tr_cost) / price;
                    cash -= (s1 * price) - tr_cost*c1;
                    shares[j] += s1;
                }
            }

            int finalI = i;
            IntStream.range(0,shares_history.size()).forEach(j -> shares_history.get(j)[finalI]=shares[j]);
            invested[i] = this.investment;
            value[i] = computeTotal();
            returns[i] = (value[i] - this.invested[i])/this.invested[i];
        }

        return computeTotal();
    }

    public double computeTotal(){
        final double[] total = {cash};
        IntStream.range(0,data.length).forEach(j-> total[0] += shares[j]*data[j][data[j].length-1]);
        return total[0];
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

    public double getInvestment() {
        return investment;
    }

    public double getCash() {
        return cash;
    }

    public double[] getShares() {
        return shares;
    }

    public double[] getReturns() {
        return returns;
    }

    @Override
    public CryptoBuyAndHoldSimulator clone() throws CloneNotSupportedException {
        CryptoBuyAndHoldSimulator individual = new CryptoBuyAndHoldSimulator(this.data);

        individual.pd = Arrays.copyOf(pd,pd.length);
        individual.cash = this.cash;
        individual.investment = this.investment;
        individual.shares = Arrays.copyOf(this.shares,this.shares.length);
        individual.returns = Arrays.copyOf(returns,returns.length);
        individual.shares_history = new ArrayList<>(this.shares_history);
        individual.value = Arrays.copyOf(value,value.length);
        individual.invested = Arrays.copyOf(invested,invested.length);

        return individual;
    }

    private static final int MAX_DAYS = 180;

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        //String tikets[] = new String[]{"SPY","UUP"};
//        String tikets[] = new String[]{"SPY","UUP"};
        //String tikets[] = new String[]{"BND","SUSM-LSE","IFSW-LSE"};
//        String tikets[] = new String[]{"UUP","SUSM-LSE","IFSW-LSE"};
//        String tikets[] = new String[]{"TECL","UUP"};
//
//        double[][] in_data = DataLoader.loadData(tikets,MAX_DAYS);
//        DCAIndividual[] dcas = DataLoader.loadDCAForData(tikets,in_data);
//
//        EWPSimulator simulator = new EWPSimulator(in_data);
//        double result = simulator.evaluate();
//        System.out.println("----RESULTS-----");
//        System.out.println("DAYS:" + in_data[0].length);
//        System.out.println("Result="+result);
//        System.out.println("Investment = " + simulator.getInvestment());
//        IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + tikets[j] + "=" + simulator.getShares()[j]));
//        System.out.println("cash =" + simulator.getCash());
//        System.out.println("Number of rebalances = " + simulator.getRebalances());
//        System.out.println("Returns = "+((result-simulator.getInvestment())/simulator.getInvestment())*100. + " %");
//        System.out.println("---DCA---");
//        for(int i = 0; i < dcas.length;i++){
//            System.out.println(String.format("DCA(%s) = %f",tikets[i],dcas[i].total()));
//        }
        File assetsAllocations = new File(String.valueOf(System.currentTimeMillis())+"_assets_allocations.csv");
        if(!assetsAllocations.exists()){
            assetsAllocations.createNewFile();
            String msg = "ticket1;ticket2;retunrs;avg.returns\n";
            Files.write(assetsAllocations.toPath(),msg.getBytes("UTF-8"), StandardOpenOption.APPEND);
        }

        List<String> all = new CopyOnWriteArrayList<>(DataLoader.findWhichNumOfDataIsMoreOrEqual(MAX_DAYS,"c:\\downloaded_data\\CRYPTO\\"));
        System.out.println("FOUND: "+all.size());
        String tikets[] = new String[2];
        CryptoBuyAndHoldSimulator best = null;
        double[][] in_data = null;
        DCAIndividual[] dcas = null;
        for(String etf1 :  all) {
            String chosen[] = new String[2];
            chosen[0] = etf1;
            all.remove(etf1);
            for(String etf2 : all) {
                chosen[1] = etf2;
                System.out.println("Computing for "+Arrays.toString(chosen));
                double[][] in_data_test = DataLoader.loadData(chosen,MAX_DAYS,"c:\\downloaded_data\\CRYPTO\\",true);
                CryptoBuyAndHoldSimulator simulator = new CryptoBuyAndHoldSimulator(in_data_test);
                double result = simulator.evaluate();
                double sum = Arrays.stream(simulator.returns).sum();
                double avg = sum/(double)simulator.returns.length;
                String msg = String.format("%s;%s;%f;%f\n",chosen[0],chosen[1],simulator.returns[simulator.returns.length-1],avg);
                Files.write(assetsAllocations.toPath(),msg.getBytes("UTF-8"), StandardOpenOption.APPEND);
                if(best == null || best.returns[best.returns.length-1] < simulator.returns[simulator.returns.length-1]){
                    best  = simulator.clone();
                    tikets = Arrays.copyOf(chosen,chosen.length);
                    in_data = Arrays.copyOf(in_data_test,in_data_test.length);
                    dcas = DataLoader.loadDCAForData(tikets,in_data_test);
                }
            }
        }

        System.out.println("----RESULTS-----");
        System.out.println("DAYS:" + in_data[0].length);
        System.out.println("Result="+best.computeTotal());
        System.out.println("Investment = " + best.getInvestment());
        String[] finalTikets2 = tikets;
        CryptoBuyAndHoldSimulator finalBest = best;
        IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + finalTikets2[j] + "=" + finalBest.getShares()[j]));
        System.out.println("cash =" + best.getCash());
        System.out.println("Returns = "+best.returns[best.returns.length-1]*100. + " %");
        System.out.println("---DCA---");
        for(int i = 0; i < dcas.length;i++){
            System.out.println(String.format("DCA(%s) = %f",tikets[i],dcas[i].total()));
        }

        List<double[]> shares_history = best.getShares_history();
        Plot2DPanel plot = new Plot2DPanel();
        String[] finalTikets = tikets;
        IntStream.range(0, shares_history.size())
                .forEach(j -> {plot.addLinePlot(finalTikets[j],
                        new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                        shares_history.get(j));
                });
        plot.addLegend("SOUTH");


        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("value",Color.red,best.getValue());
        plot2.addLinePlot("invested",Color.blue,best.getInvested());

        Plot2DPanel plot3 = new Plot2DPanel();
        String[] finalTikets1 = tikets;
        double[][] finalIn_data = in_data;
        IntStream.range(0, in_data.length)
                .forEach(j -> {plot3.addLinePlot(finalTikets1[j],
                        new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                        finalIn_data[j]);
                });
        plot3.addLegend("SOUTH");

        Plot2DPanel plot4 = new Plot2DPanel();
        for(int i = 0; i< tikets.length;i++){
            Color c = new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255));
            plot4.addLinePlot(tikets[i],c,dcas[i].getReturnsDaily());
        }
        plot4.addLinePlot("portfolio",Color.black,best.getReturns());
        plot4.addLegend("SOUTH");


        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("REBALANCE");
        frame.setLayout(new GridLayout(2,2));
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plot2);
        frame.add(plot3);
        frame.add(plot4);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
