package org.xSakix.rebalance;

import cern.jet.random.Uniform;
import org.math.plot.Plot2DPanel;
import org.xSakix.buyandhold.ETFBuyAndHoldSimulator;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.utils.DataLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class TestingResults {

    public static void main(String[] args) throws IOException {
        //testQPSO_EWP();
        //"BND","SUSM-LSE","IFSW-LSE"
        testEWP(180,"IFSW-LSE","BND","SUSM-LSE");
        //testEWP(180,"IJT","ILF");
    }
    private static void testEWP(int maxDays,String ...etfs) throws IOException {

        String tikets[] = Arrays.copyOf(etfs, etfs.length);
        System.out.println("Computing for "+Arrays.toString(tikets));
        double[][] in_data = DataLoader.loadData(tikets,maxDays);
        EWPSimulator ewpSimulator = new EWPSimulator(in_data);
        double result = ewpSimulator.evaluate();
        ETFBuyAndHoldSimulator bah = new ETFBuyAndHoldSimulator(in_data);
        bah.evaluate();

        double sum = Arrays.stream(ewpSimulator.getReturns()).sum();
        double avg = sum/(double)ewpSimulator.getReturns().length;
        DCAIndividual[] dcas = DataLoader.loadDCAForData(tikets,in_data);

        System.out.println("----RESULTS-----");
        System.out.println("DAYS:" + in_data[0].length);
        System.out.println("Result="+ewpSimulator.computeTotal());
        System.out.println("Investment = " + ewpSimulator.getInvestment());
        String[] finalTikets2 = tikets;
        EWPSimulator finalBest = ewpSimulator;
        IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + finalTikets2[j] + "=" + finalBest.getShares()[j]));
        System.out.println("cash =" + ewpSimulator.getCash());
        System.out.println("Number of rebalances = " + ewpSimulator.getRebalances());
        System.out.println("Returns = "+ewpSimulator.getReturns()[ewpSimulator.getReturns().length-1]*100. + " %");
        System.out.println("---DCA---");
        for(int i = 0; i < dcas.length;i++){
            System.out.println(String.format("DCA(%s) = %f",tikets[i],dcas[i].total()));
        }
        System.out.println("---B&H---");
        System.out.println("result = "+bah.computeTotal());
        ETFBuyAndHoldSimulator finalBah = bah;
        IntStream.range(0, tikets.length).forEach(j -> System.out.println("Num of shares of " + finalTikets2[j] + "=" + finalBah.getShares()[j]));
        System.out.println("cash =" + bah.getCash());
        System.out.println("Returns = "+bah.getReturns()[bah.getReturns().length-1]*100. + " %");

        List<double[]> shares_history = ewpSimulator.getShares_history();
        Plot2DPanel plot = new Plot2DPanel();
        String[] finalTikets = tikets;
        IntStream.range(0, shares_history.size())
                .forEach(j -> {plot.addLinePlot(finalTikets[j],
                        new Color(Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255), Uniform.staticNextIntFromTo(0,255)),
                        shares_history.get(j));
                });
        plot.addLegend("SOUTH");


        Plot2DPanel plot2 = new Plot2DPanel();
        plot2.addLinePlot("value",Color.red,ewpSimulator.getValue());
        plot2.addLinePlot("B&H value",Color.black,bah.getValue());
        plot2.addLinePlot("invested",Color.blue,ewpSimulator.getInvested());
        plot2.addLegend("SOUTH");

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
        plot4.addLinePlot("portfolio",Color.black,ewpSimulator.getReturns());
        plot4.addLinePlot("B&H portfolio",Color.red,bah.getReturns());
        plot4.addLegend("SOUTH");

        Plot2DPanel plot5 = new Plot2DPanel();
        plot5.addLinePlot("portfolio",Color.black,ewpSimulator.getAvgReturns());
        plot5.addLinePlot("B&H portfolio",Color.red,bah.getAvgReturns());
        plot5.addLegend("SOUTH");


        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("REBALANCE");
        frame.setLayout(new GridLayout(2,3));
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.add(plot2);
        frame.add(plot3);
        frame.add(plot4);
        frame.add(plot5);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void testQPSO_EWP() throws IOException {
        String tiket1= "BND";
//        String tiket2 = "SUSM-LSE";
//        String tiket3 = "IFSW-LSE";
        String tiket2 = "SPY";
        String tiket3 = "EWA";
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\"+tiket1+".csv");
        double data2[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\"+tiket2+".csv");
        double data3[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\"+tiket3+".csv");
        int min = Arrays.stream(new int[]{data.length,data2.length,data3.length}).min().getAsInt();
        //data = Arrays.copyOfRange(data,0, data.length/4);

        if(data.length > min){
            int rozdiel = data.length - min;
            data = Arrays.copyOfRange(data,rozdiel,data.length);
        }
        if(data2.length > min){
            int rozdiel = data2.length - min;
            data2 = Arrays.copyOfRange(data2,rozdiel,data2.length);
        }

        if(data3.length > min){
            int rozdiel = data3.length - min;
            data3 = Arrays.copyOfRange(data3,rozdiel,data3.length);
        }

        double[][] in_data = new double[3][data.length];
        in_data[0] = data;
        in_data[1] = data2;
        in_data[2] = data3;

        QuantumETFRebalancingEqualyWeightedParticle particle = new QuantumETFRebalancingEqualyWeightedParticle(in_data);
//        particle.setW(new double[]{0.1799827192302179});
        particle.setW(new double[]{0.17111});
        double fit = particle.evaluate();

        System.out.println("----RESULTS-----");
        System.out.println("DAYS:" + in_data[0].length);
        System.out.println(String.format("Best fit weights = %s", Arrays.toString(particle.getW())));
        System.out.println(String.format("Best fitness = %f", fit));
        System.out.println(String.format("Invested=%f",particle.getInvestment()));
        System.out.println("Num of shares of "+tiket1+"="+particle.getShares()[0]);
        System.out.println("Num of shares of "+tiket2+"="+particle.getShares()[1]);
        System.out.println("Num of shares of "+tiket3+"="+particle.getShares()[2]);
        System.out.println("cash ="+particle.getCash());
        System.out.println("Number of rebalances = "+particle.getRebalances());
    }
}
