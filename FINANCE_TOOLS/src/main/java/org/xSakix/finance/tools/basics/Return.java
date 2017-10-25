package org.xSakix.finance.tools.basics;


import java.util.Arrays;

public class Return {

    public static double returnt(double price_t_plus_1, double price_t,double dividend_t){

        return (price_t_plus_1 - price_t+dividend_t)/price_t;
    }

    public static double grossReturn(double price_t_plus_1, double price_t,double dividend_t){
        return 1.+returnt(price_t_plus_1,price_t,dividend_t);
    }

    public static double aritmeticMean(double x[]){
        return Arrays.stream(x).sum()/((double)x.length);
    }

    public static double geometricMean(double x[]){
        double sum = 1.;
        for(int i = 0; i < x.length;i++) {
            if(x[i] == 0)
                continue;
            sum *= x[i];
        }
        return Math.pow(sum,1./x.length);
    }
}
