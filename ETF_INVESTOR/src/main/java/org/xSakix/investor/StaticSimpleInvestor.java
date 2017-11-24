package org.xSakix.investor;

import org.math.plot.Plot2DPanel;
import org.xSakix.individuals.DCAIndividual;
import org.xSakix.individuals.InvestorAction;
import org.xSakix.individuals.QuantumSimpleIndividual;
import org.xSakix.individuals.SimpleChaosInvestor;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.etfgrowth.SimpleQuantumETFGrowth;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class StaticSimpleInvestor {

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        int period = 365;

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        double input_data[] = Arrays.copyOfRange(data,0,data.length-period);
        double test_data[] = Arrays.copyOfRange(data,data.length-period,data.length);
        QuantumSimpleIndividual best = SimpleQuantumETFGrowth.runQuantumETFGrowth(test_data,false);
        SimpleChaosInvestor chaosInvestor = new SimpleChaosInvestor(best.getRC());



        double cash=300.;
        int shares =0;
        double invested = cash;
        double tr_cost = 4.;
        double[] totals = new double[test_data.length];

        for(int i = 0; i < test_data.length;i++) {
            if(i % 30 == 0){
                cash+=300.;
                invested+= 300.;
            }
            chaosInvestor.computeCycles(input_data.length+i);
            InvestorAction result = chaosInvestor.getAction();

            if(result == InvestorAction.BUY && cash > test_data[i] ){
                int n_shares = (int) ((cash-tr_cost)/(test_data[i]));
                if(n_shares <= 0){
                    continue;
                }
                shares += n_shares;
                cash -= (test_data[i])*((double)n_shares)+tr_cost;
                System.out.println(String.format("BUY %d for price %f | TOTAL=%f",n_shares,test_data[i],(shares*test_data[i]+cash)));
            }else if(result == InvestorAction.SELL && shares > 0 ){
                double data2[] = Arrays.copyOfRange(data,0,data.length-period+i);
                System.out.println(String.format("SELL %d for price %f | TOTAL=%f", shares, test_data[i], (shares * test_data[i] + cash)));
                cash += ((double) shares) * test_data[i] - tr_cost;
                shares = 0;
            }

            totals[i] = shares*test_data[i]+cash;
            if(totals[i] < 1.){
                System.out.println(shares+","+test_data[i]+","+cash);
            }
        }

        DCAIndividual dca = new DCAIndividual(test_data);
        dca.simulate();
        System.out.println("INVESTED = "+invested);
        System.out.println("DCA = "+dca.total());
        System.out.println("CHAOS = "+(shares*data[data.length-1]+cash));


        Plot2DPanel plot = new Plot2DPanel();
        plot.addLinePlot("dca-totals", Color.blue, dca.getTotals());
        plot.addLinePlot("investor-totals", Color.red, totals);
        plot.addLinePlot("test data", Color.black, Arrays.stream(test_data).map(d -> d*10.).toArray());

        Dimension dim = new Dimension(800, 600);

        JFrame frame = new JFrame("SPY");
        frame.setLayout(new GridLayout());
        frame.setSize(dim);
        frame.setMaximumSize(dim);
        frame.setMinimumSize(dim);
        frame.add(plot);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
