package org.xSakix.gatools;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;

import java.math.BigInteger;
import java.util.stream.IntStream;

public class FloatOperations {

    public static double cross(double f1, double f2){
        return cross(f1,f2,1);
    }

    public static double cross(double f1, double f2, int numIndexes){
        double mantise = Math.pow(10., 16);

        double mod = 1.;

        if(f1 <0. ){
            mod = -1.;
        }

        long i1 =  Math.abs(Math.round(f1* mantise));
        long i2 =  Math.abs(Math.round(f2* mantise));
        String b1 = Long.toBinaryString(i1);
        while (b1.length() < 64){
            b1 = "0"+b1;
        }

        String b2 = Long.toBinaryString(i2);
        while (b2.length() < 64){
            b2 = "0"+b2;
        }


        char result[] = new char[64];
        //int index = Uniform.staticNextIntFromTo(0,63);
        int index= 31;
        try {
            b1.getChars(0, index, result, 0);
            b2.getChars(index, b2.length(), result, index);
        }catch(ArrayIndexOutOfBoundsException exc){
            System.err.println(String.format("Index=%d",index));
            exc.printStackTrace();
        }

        long value = 0L;
        try {
            value = new BigInteger(String.valueOf(result), 2).longValue();
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        return mod*(double)(value/ mantise);
    }

    public static double crossRand(double f1, double f2){
        double mantise = Math.pow(10., 16);

        double mod = 1.;

        if(f1 <0. ){
            mod = -1.;
        }

        long i1 =  Math.abs(Math.round(f1* mantise));
        long i2 =  Math.abs(Math.round(f2* mantise));
        String b1 = Long.toBinaryString(i1);
        while (b1.length() < 64){
            b1 = "0"+b1;
        }

        String b2 = Long.toBinaryString(i2);
        while (b2.length() < 64){
            b2 = "0"+b2;
        }


        char result[] = new char[64];
        for(int i =0;i < result.length;i++){
            if(Uniform.staticNextDoubleFromTo(0.,1.) <= 0.5){
                result[i] = b1.charAt(i);
            }else{
                result[i] = b2.charAt(i);
            }
        }

        long value = 0L;
        try {
            value = new BigInteger(String.valueOf(result), 2).longValue();
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        return mod*(double)(value/ mantise);
    }

    public static double mutate(double f1) {
        double mantise = Math.pow(10., 16);

        double mod = 1.;

        if(f1 <0. ){
            mod = -1.;
        }

        long i1 = Math.abs(Math.round(f1 * mantise));
        String b1 = Long.toBinaryString(i1);
        while (b1.length() < 64){
            b1 = "0"+b1;
        }

        char result[] = new char[64];
        int index = Uniform.staticNextIntFromTo(0, 63);
        if(index == 0){
            mod *= -1.;
        }
        b1.getChars(0, index, result, 0);
        try {
            if (b1.charAt(index) == '0') {
                result[index] = '1';
            } else {
                result[index] = '0';
            }
        }catch(StringIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        try {
            b1.getChars(index + 1, b1.length(), result, index + 1);
        }catch(ArrayIndexOutOfBoundsException e){
            System.err.println(String.format("Index=%d",index));
            e.printStackTrace();
        }

        long value = 0L;
        try {
            value = new BigInteger(String.valueOf(result), 2).longValue();
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        return mod*(double)(value/ mantise);
    }

    public static double gausianMutation(double f, double factor){
        return f+= factor* Normal.staticNextDouble(0.,1.);
    }

    public static void main(String[] args) {
        double v = 3.6631106649954814;
        IntStream.range(0,10).forEach(i -> System.out.println(gausianMutation(v,0.2)));
    }
}
