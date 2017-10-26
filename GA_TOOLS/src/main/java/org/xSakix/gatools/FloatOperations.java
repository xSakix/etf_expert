package org.xSakix.gatools;

import cern.jet.random.Uniform;

import java.math.BigInteger;

public class FloatOperations {

    public static double cross(double f1, double f2){
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
        int index = Uniform.staticNextIntFromTo(0,63);
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
}
