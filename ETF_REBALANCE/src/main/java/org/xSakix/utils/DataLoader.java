package org.xSakix.utils;

import org.apache.commons.lang.ArrayUtils;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.individuals.DCAIndividual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DataLoader {

    public static double[][] loadData(String[] tikets) throws IOException
    {
        return loadData(tikets,0);
    }

    public static double[][] loadData(String[] tikets, int maxDays) throws IOException {
        return loadData(tikets,maxDays,"c:\\downloaded_data\\USD\\",false);
    }

    public static double[][] loadData(String[] tikets, int maxDays, String directory, boolean revert) throws IOException {
        List<double[]> inputs = new ArrayList<>(tikets.length);
        for(String ticket : tikets) {
            inputs.add(EtfReader.readEtf( directory+ ticket + ".csv"));
        }
        int min = maxDays == 0? inputs.stream().mapToInt(p -> p.length).min().getAsInt() : maxDays;

        double[][] in_data = new double[tikets.length][min];
        int i = 0;
        for(double[] data : inputs){
            if(revert){
                ArrayUtils.reverse(data);
            }
            if(data.length > min){
                int rozdiel = data.length - min;
                data = Arrays.copyOfRange(data,rozdiel,data.length);
            }
            in_data[i++] = data;
        }

        return in_data;
    }

    public static DCAIndividual[] loadDCAForData(String[] tikets, double[][] in_data) {
        DCAIndividual dcas[] = new DCAIndividual[tikets.length];
        for(int i = 0;i < tikets.length;i++) {
            DCAIndividual dca = new DCAIndividual(in_data[i]);
            dca.simulate();
            dcas[i]=dca;
        }
        return dcas;
    }

    public static List<String> findWhichNumOfDataIsMoreOrEqual(int length) throws IOException {
        return findWhichNumOfDataIsMoreOrEqual( length,  "c:\\downloaded_data\\USD\\");
    }

    public static List<String> findWhichNumOfDataIsMoreOrEqual(int length, String directory) throws IOException {

        List<String> all = Files.list(Paths.get(directory)).filter(p -> !Files.isDirectory(p)).map(p -> p.getName(p.getNameCount()-1).toString().replace(".csv","")).collect(Collectors.toList());


        return all.parallelStream().filter(s ->{
            try {
                double data[] = EtfReader.readEtf(directory + s + ".csv");
                return data.length >= length;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        List<String> all = findWhichNumOfDataIsMoreOrEqual(1000);
        System.out.println(all);
    }

}
