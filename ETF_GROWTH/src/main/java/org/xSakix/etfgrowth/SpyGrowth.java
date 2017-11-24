package org.xSakix.etfgrowth;

import org.xSakix.individuals.Individual;
import org.xSakix.etfreader.EtfReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class SpyGrowth {

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        long time = System.currentTimeMillis();
        String ticket = "SPY";

        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\" + ticket + ".csv");

        File file = new File("result_ga.csv");
        if(!file.exists()){
            file.createNewFile();
        }else{
            file.delete();
            file.createNewFile();
        }

        for(int i = 0; i < 10;i++) {
            ETFGrowthGA ga = new ETFGrowthGA();
            Individual best_of_all = ga.run(data);
            best_of_all.print();
            Files.write(file.toPath(), (best_of_all.total()+"\n").getBytes("UTF-8"), StandardOpenOption.APPEND);
        }



        System.out.println(String.format("Time=%f s",(System.currentTimeMillis()-time)/1000.));

//        Plot2DPanel plot = new Plot2DPanel();
//        plot.addLinePlot("curve_plot", Color.blue,etfgrowth.getHist_totals());
//        Plot2DPanel plot2 = new Plot2DPanel();
//        plot2.addLinePlot("curve_plot", Color.red,etfgrowth.mut_rate_history);
//        Plot2DPanel plot3 = new Plot2DPanel();
//        plot3.addLinePlot("curve_plot", Color.black,etfgrowth.s_rate_history);
//        Dimension dim = new Dimension(800, 600);
//
//        JFrame frame = new JFrame("SPY");
//        frame.setLayout(new GridLayout());
//        frame.setSize(dim);
//        frame.setMaximumSize(dim);
//        frame.setMinimumSize(dim);
//        frame.add(plot);
//        frame.add(plot2);
//        frame.add(plot3);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
