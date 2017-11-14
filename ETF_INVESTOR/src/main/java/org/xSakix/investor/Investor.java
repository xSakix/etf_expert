package org.xSakix.investor;

import com.etfdatadownload.main.ETFDataDownloaderFactory;
import com.etfdatadownload.main.ETFDownLoadBuilder;
import com.etfdatadownload.main.ETFDownloadParams;
import com.etfdatadownload.main.IETFDataDownloader;
import org.xSakix.etfgrowth.ChaosInvestor;
import org.xSakix.etfgrowth.Individual;
import org.xSakix.etfgrowth.InvestorAction;
import org.xSakix.etfgrowth.QuantumIndividual;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.ga.ETFGrowthGA;
import org.xSakix.ga.QuantumETFGrowth;
import org.xSakix.particle.QuantumParticle;
import org.xSakix.particle.QuantumParticleCurveFit;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.etfdatadownload.main.Main.EMPTY_DATA;

public class Investor implements Runnable {

    private static final int M = 3;
    private static String DIR = "c:\\downloaded_data\\latest\\";
    private final String fileName;
    private String ticket;
    private double data[];
    private double w[];
    private InvestorAction result = InvestorAction.NONE;
    private double[] testData;

    public Investor(String ticket) {
        this.ticket = ticket;
        this.fileName = String.format("%s%s.csv", DIR, ticket);
    }


    public void run() {
        try {
            downloadHistoryData();
            loadData();
            computeCurveFitting();

            double prediction = predictNewValue();
            double[] data2 = addPredictedValueToDataSet(prediction);

            Individual best_of_all = computeGrowth(data2);
            ChaosInvestor chaosInvestor = computeChaosInvestor(data2, best_of_all);
            result = chaosInvestor.getAction();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double testRun(int startDay) throws IOException {
        File logFile = new File(String.format("%s_log.txt",ticket));
        if(logFile.exists()){
            logFile.delete();
        }
        logFile.createNewFile();

        double cash = 300.;
        double invested = cash;
        int shares = 0;
        int iterations = 0;
        double tr_cost = 4.0;
        double total = 0.;
        loadData();
        this.testData = Arrays.copyOf(data,data.length);

        while(startDay < this.testData.length) {
            try {
                if(iterations % 30 == 0){
                    cash+=300.;
                    invested+=300.;
                }
                prepareTestData(startDay);
                computeCurveFitting();

                double prediction = predictNewValue();
                double[] data2 = addPredictedValueToDataSet(prediction);

                Individual best_of_all = computeGrowth(data2);
                ChaosInvestor chaosInvestor = computeChaosInvestor(data2, best_of_all);
                result = chaosInvestor.getAction();
                String msg = String.format("Action = %s \n", result);
                System.out.println(msg);
                Files.write(logFile.toPath(),msg.getBytes("UTF-8"),StandardOpenOption.APPEND);


                if(result == InvestorAction.BUY){
                    int n_shares = (int) ((cash-tr_cost)/(testData[startDay+1]));
                    if(n_shares <= 0){
                        continue;
                    }
                    shares += n_shares;
                    cash -= (testData[startDay+1])*((double)n_shares)+tr_cost;
                    msg = String.format("BUY %d for price %f and prediction was %f \n",n_shares,testData[startDay+1],prediction);
                    System.out.println(msg);
                    Files.write(logFile.toPath(),msg.getBytes("UTF-8"),StandardOpenOption.APPEND);
                }else if(result == InvestorAction.SELL && shares > 0){
                    msg = String.format("SOLD %d for price %f and prediction was %f \n",shares,testData[startDay+1],prediction);
                    System.out.println(msg);
                    Files.write(logFile.toPath(),msg.getBytes("UTF-8"),StandardOpenOption.APPEND);
                    cash += ((double)shares)*testData[startDay+1]-tr_cost;
                    shares=0;
                }

                msg = String.format("Number of days run %d and invested sum %f \n",iterations,invested);
                System.out.println(msg);
                Files.write(logFile.toPath(),msg.getBytes("UTF-8"),StandardOpenOption.APPEND);
                total = shares*testData[startDay+1]+cash;
                msg = String.format("TOTAL:%f \n",total);

                Files.write(logFile.toPath(),msg.getBytes("UTF-8"),StandardOpenOption.APPEND);
                Files.write(logFile.toPath(),"---------------\n".getBytes("UTF-8"),StandardOpenOption.APPEND);

                startDay++;
                iterations++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        return total;
    }

    private void prepareTestData(int day) {
        this.data = Arrays.copyOfRange(this.testData,0,day);
    }

    private ChaosInvestor computeChaosInvestor(double[] data2, Individual best_of_all) {
        System.out.println(String.format("[START]Computing chaos investor for %s ...", ticket));

        ChaosInvestor chaosInvestor = new ChaosInvestor(best_of_all.getRC());
        chaosInvestor.computeCycles(data2.length);

        System.out.println(String.format("[END]Computing chaos investor for %s ...", ticket));

        return chaosInvestor;
    }

    private Individual computeGrowth(double[] data2) throws IOException, CloneNotSupportedException {
        System.out.println(String.format("[START]Computing growth GA for %s ...", ticket));

        QuantumIndividual best = QuantumETFGrowth.runQuantumETFGrowth(data);

        System.out.println(String.format("[END]Computing growth GA for %s ...", ticket));

        return best;
    }

    private double[] addPredictedValueToDataSet(double prediction) {
        System.out.println(String.format("[START]Adding predicted value to dataset for %s ...", ticket));

        double data2[] = new double[data.length + 1];
        for (int i = 0; i < data.length; i++) {
            data2[i] = data[i];
        }
        data2[data2.length - 1] = prediction;

        System.out.println(String.format("[END]Adding predicted value to dataset for %s ...", ticket));

        return data2;
    }

    private void computeCurveFitting() {
        System.out.println(String.format("[START]Computing curve fitting for %s ...", ticket));

        QuantumParticleCurveFit curveFit = new QuantumParticleCurveFit(M, data);
        w = curveFit.run();

        System.out.println(String.format("[END]Computing curve fitting for %s ...", ticket));
    }

    private double predictNewValue() {
        System.out.println(String.format("[START]Predicting new value for %s ...", ticket));

        QuantumParticle particle = new QuantumParticle(M);
        particle.setW(w);

        double val = particle.evaluate(data[data.length - 1]);

        System.out.println(String.format("[END]Predicting new value for %s ...", ticket));

        return val;
    }

    private void loadData() throws IOException {
        System.out.println(String.format("[START]Loading data for %s ...", ticket));

        data = EtfReader.readEtf(fileName);

        System.out.println(String.format("[END]Loading data for %s ...", ticket));
    }

    private void downloadHistoryData() throws IOException {
        System.out.println(String.format("[START]Downloading data for %s ...", ticket));

        Stock stock = null;
        try {
            stock = YahooFinance.get(ticket);
        } catch (Exception e) {
            // nothing
        }

        IETFDataDownloader downloader = ETFDataDownloaderFactory.getYahooDownloader();
        ETFDownloadParams params = ETFDownLoadBuilder.start().stock(stock).ETFTicket(ticket).frequency("d").build();
        String data = EMPTY_DATA;
        try {
            data = downloader.download(params);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (data == EMPTY_DATA) {
            System.out.println("Not writing file:" + ticket);
            return;
        }

        Path dataFile = Paths.get(fileName);

        if (Files.exists(dataFile)) {
            Files.delete(dataFile);
        }

        try {
            Files.write(dataFile, data.getBytes("UTF-8"), StandardOpenOption.CREATE_NEW);
            System.out.println("Written file:" + dataFile.toString());
        } catch (Exception e) {
            System.out.println("Writing file failed: " + dataFile.toString());
            e.printStackTrace();
        }
        System.out.println(String.format("[END]Downloading data for %s ...", ticket));

    }

    public static void main(String[] args) {
        /*String[] tickets = new String[]{
                "EWS",
                "EWA",
                "BND",
                "GREK",
                "SOCL",
                "ARKK",
                "SCIN",
                "ARKQ",
                "XME",
                "XLF",
                "COPX",
                "SLX",
                "KOL",
                "GDX",
                "SLVP",
                "GDXJ",
                "SIL",
                "SILJ",
                "RING",
                "PEY",
                "PSL",
                "XSD",
                "XLU",
                "PSCU",
                "XLP",
                "XBI",
                "BBH",
                "PBE",
                "FDN",
                "EWZ",
                "IAU",
                "ILF",
                "EWW",
                "EZA",
                "EWM",
                "EWY",
                "ADRE",
                "SLV",
                "EEM",
                "XLE",
                "EPP",
                "IDX",
                "IYE",
                "DBS",
                "DBP",
                "PNQI",
                "PXQ"};*/
//        String[] tickets = new String[]{
//                "SPY","IVV","VOO","IWB","VV","SCHX",
//                "ACWI","VT","IOO","DGT",
//                "TMF","HYXU","ICVT","CWB","UBT","FCVT","IHY","IBND","UJB","EDV","PICB","ZROZ","DSUM","LKOR","EMAG","CLY","EBND","SPLB","VCLT","ANGL","ILTB","EMLC","GHYG","LEMB","FALN"};

//        Map<String, InvestorAction> resultMap = new HashMap<>();
//        for (String ticket : tickets) {
//            Investor investor = new Investor(ticket);
//            investor.run();
//            resultMap.put(ticket, investor.result);
//            System.out.println(String.format("Action for ticket:%s", resultMap.get(ticket)));
//        }
//
//        resultMap.keySet().stream().forEach(k -> System.out.println(String.format("Action for ticket %s:%s", k,resultMap.get(k))));

        //1000 = 1997-01-13,76.500000
        Investor investor = new Investor("SPY");
        try {
            double total = investor.testRun(1000);
            System.out.println(String.format("TOTAL %f",total));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
