package org.xSakix.etfreader;

import cern.colt.list.DoubleArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class EtfReader {

    public static double[] readEtf(String etfFile) throws IOException {

        Path path = Paths.get(etfFile);
        List<Double> found = Files.lines(path).map(line -> Double.valueOf(line.split(",")[1])).collect(Collectors.toList());
        double result[] = new double[found.size()];
        for(int i = 0; i < result.length;i++){
            result[i] = found.get(i);
        }
        return result;
    }

}
