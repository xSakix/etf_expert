package org.xSakix.individuals;

import cern.jet.random.Uniform;
import org.xSakix.finance.tools.basics.Return;

import java.util.Arrays;

public class QuantumIndividual extends Individual{

    //particle
    private double Pw[];
    private double Gw[];
    private double C[];
    private double alpha;

    private double fitness;
    private double lastFitness = Double.NaN;

    public QuantumIndividual(double data[]) {
        super(data);
        this.Pw = Arrays.copyOf(r_c, r_c.length);
        this.Gw = Arrays.copyOf(r_c, r_c.length);
        this.C = Arrays.copyOf(r_c, r_c.length);
        this.alpha = 0.75;
    }

    public double getFitness() {
        return this.fitness;
    }

    public double[] getW() {
        return this.getRC();
    }

    public void setW(double r_c_other[]) {
        this.setRC(r_c_other);
    }


    public double invested() {
        return this.sum;
    }

    public double arithmeticMeanOfReturns() {
        return Return.aritmeticMean(Arrays.copyOfRange(returns.elements(), 0, returns.size()));
    }

    public double geometricMeanOfReturns() {
        return Return.geometricMean(Arrays.copyOfRange(returns.elements(), 0, returns.size()));
    }

    public void eval() {
        initialHeat();
        simulate();

        fitness = total();
        if (Double.isNaN(lastFitness)) {
            lastFitness = fitness;
        }
        if (fitness < lastFitness) {
            Pw = Arrays.copyOf(r_c, r_c.length);
        }

        reinitialize();
    }

    public void computeWeights() {
        for (int i = 0; i < r_c.length; i++) {
            double phi = Uniform.staticNextDoubleFromTo(.0, 1.);
            double p = phi * Pw[i] + (1 - phi) * Gw[i];
            double u = Uniform.staticNextDoubleFromTo(0., 1.);
            if (Uniform.staticNextDoubleFromTo(0., 1.) < 0.5) {
                r_c[i] = p + alpha * Math.abs(r_c[i] - C[i]) * Math.log(1 / u);

            } else {
                r_c[i] = p - alpha * Math.abs(r_c[i] - C[i]) * Math.log(1 / u);

            }

        }
    }

    public double[] getPw() {
        return Pw;
    }

    public void setPw(double[] pw) {
        Pw = Arrays.copyOf(pw, pw.length);
    }

    public double[] getGw() {
        return Gw;
    }

    public void setGw(double[] gw) {
        Gw = Arrays.copyOf(gw, gw.length);
    }

    public double[] getC() {
        return C;
    }

    public void setC(double[] c) {
        C = Arrays.copyOf(c, c.length);
    }

    @Override
    public QuantumIndividual clone() throws CloneNotSupportedException {
        QuantumIndividual individual = new QuantumIndividual(this.data);

        individual.r_c = Arrays.copyOf(this.r_c,3);
        individual.Pw = Arrays.copyOf(this.Pw,3);
        individual.Gw = Arrays.copyOf(this.Gw,3);
        individual.C = Arrays.copyOf(this.C,3);
        individual.c = Arrays.copyOf(this.c,3);
        individual.cash = this.cash;
        individual.sum = this.sum;
        individual.shares = this.shares;

        individual.alpha = this.alpha;
        individual.fitness = this.fitness;
        individual.lastFitness = this.lastFitness;

        return individual;
    }
}
