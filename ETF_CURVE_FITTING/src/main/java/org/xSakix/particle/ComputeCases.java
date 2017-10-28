package org.xSakix.particle;

import cern.colt.list.DoubleArrayList;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.particle.neural.particle.Net;
import org.xSakix.tools.Errors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComputeCases {

    public static final int M = 4;
    public static final int FRAME = 10;

    public static void main(String[] args) throws IOException {
        double data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //data adjustment, find max and normalize data on max
        double max = Arrays.stream(data).max().getAsDouble();
        data = adjustData(data,max);

        List<double[]> xx = new ArrayList<>();
        DoubleArrayList tt = new DoubleArrayList();
        for(int i =0 ;i < data.length-M;i++){
            double x[] = new double[M];
            for(int j = 0; j < M;j++) {
                x[j] = data[i+j];
            }
            xx.add(x);
            tt.add(data[i+M]);
        }
        double x[][] =  xx.toArray(new double[][]{});
        double t[] = Arrays.copyOf(tt.elements(),tt.size());

        double[][] wHid1 = new double[][]{
                {-1.879017353640567, 2.06621139060664, -0.36021776507853875, -1.6953722514022664},
                {-6.218601936311227, 1.0700008078042118, 3.3071808548542982, 6.708011802415526},
                {-3.552990885355443, 6.814147698471435, -0.3672439990133211, -4.113356873788857},
                {2.00723190119623, -4.679502375182783, 1.9513280877329913, -1.4956714080226392}};

        double[][] wHid2 = new double[][]{
                {2.518599197190949, 3.955264922374599, 6.27841082784244, 3.957825562503923},
                {4.779478182348378, -1.0786321275438138, 3.846375817501427, 3.364865206342665},
                {-2.1730426593995387, -2.512964875365095, -0.8802997018227652, 0.8759262745068671},
                {0.6090168951895654, -2.365799637137681, -4.891141274873037, 5.85895659506265},
                {-5.971440015847315, -0.604944053094296, -0.6883837620378862, -5.355822271176411},
                {-3.4746018485879273, 5.811073353034368, -1.4721098665994872, -3.180877010046091},
                {-2.186395796185305, 1.019515540361786, 0.7981824584850613, 3.2870900789417505},
                {2.172713584372049, -2.037640400527063, -0.7290657730400455, -1.7216515536958799},
                {1.1044704560617757, 9.167195583443354, -16.697120111215067, 10.111487071718727}
        };

        double[] out = new double[]{2.9737024539093513, -6.686712236529342, -3.6294098036643123, -1.2878865847485657, 9.393747835115821, 2.263239204941128, -3.869544006798587, 0.556442017164125, 4.205808056936213};


        Net best = new Net(4,out,wHid1,wHid2);

        System.out.println("------------------RESULTS-----------------");
        double y[] = new double[data.length-M];
        double yy[] = new double[data.length-M];
        for (int i = data.length-FRAME; i < data.length-M; i++) {
            y[i] = best.eval(x[i])*max;
            yy[i] = t[i]*max;
            double error = Math.abs( yy[i]-y[i]);
            System.out.println(String.format("error = %.3f-%.3f = %.3f", yy[i],y[i], error));
        }
        System.out.println(String.format("LSE=%f",Errors.leastSquareError(yy,y)));
    }

    private static double[] adjustData(double[] data, double max) {
        double dataTemp[] = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            dataTemp[i] = data[i] / max;
        }

        return Arrays.copyOf(dataTemp, dataTemp.length);
    }
}
