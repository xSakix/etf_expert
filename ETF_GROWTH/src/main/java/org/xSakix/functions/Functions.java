package org.xSakix.functions;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;

import java.util.List;

public class Functions {

    public static double[] computeYorke(double c[],double r_c[]){

        for(int i = 0; i < 3;i++) {
            c[i] = computeYorke(c[i],r_c[i]);
        }

        return c;
    }

    public static double computeYorke(double c,double r_c){
        return c * r_c * (1. - c);
    }


    public static double[] computeYorke(double c[],double r_c[],List<DoubleArrayList> hist_c){
        double c_next[] = new double[3];

        for(int i = 0; i < 3;i++) {
            c_next[i] = c[i] * r_c[i] * (1. - c[i]);
            hist_c.get(i).add(c_next[i]);
        }

        return c_next;
    }

    public static double[] computeMayFeigenbaum(double c[],double r_c[]){

        c[0] =r_c[0] * (c[0] - Math.pow(c[0],2));
        c[1] =r_c[1] * (c[1] - Math.pow(c[1],2));
        c[2] =r_c[2] * (c[2] - Math.pow(c[2],2));

        return c;
    }

    public static double[] computeFeigenbaum(double c[],double r_c[]){

        c[0] =r_c[0] * Math.sin(Math.PI*c[0]);
        c[1] =r_c[1] * Math.sin(Math.PI*c[1]);
        c[2] =r_c[2] * Math.sin(Math.PI*c[2]);

        return c;
    }

    public static double computeChoice(double choice_min, double choice_max, boolean isConstant) {
        if (isConstant) {
            return choice_max;
        }

        return Uniform.staticNextDoubleFromTo(choice_min, choice_max);
    }
}
