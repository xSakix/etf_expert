package org.xSakix.individuals;

import cern.jet.random.Uniform;

public class QuantumSimpleIndividual extends SimpleIndividual{

    //particle
    private double Pw;
    private double Gw;
    private double C;
    private double alpha;

    private double fitness;
    private double lastFitness = Double.NaN;

    public QuantumSimpleIndividual(double data[]) {
        super(data);
        this.Pw = r_c;
        this.Gw = r_c;
        this.C = r_c;
        this.alpha = 0.75;

    }

    public double getFitness() {
        return this.fitness;
    }

    public double getW() {
        return this.getRC();
    }

    public void setW(double r_c_other) {
        this.setRC(r_c_other);
    }


    public double invested() {
        return this.sum;
    }


    public void eval() {
        initialHeat();
        simulate();

        fitness = total();
        if (Double.isNaN(lastFitness)) {
            lastFitness = fitness;
        }
        if (fitness < lastFitness) {
            Pw = r_c;
        }

        reinitialize();
    }

    public void computeWeights() {
        double phi = Uniform.staticNextDoubleFromTo(.0, 1.);
        double p = phi * Pw + (1 - phi) * Gw;
        double u = Uniform.staticNextDoubleFromTo(0., 1.);
        if (Uniform.staticNextDoubleFromTo(0., 1.) < 0.5) {
            r_c = p + alpha * Math.abs(r_c - C) * Math.log(1 / u);

        } else {
            r_c = p - alpha * Math.abs(r_c - C) * Math.log(1 / u);

        }
    }

    public double getPw() {
        return Pw;
    }

    public void setPw(double pw) {
        Pw = pw;
    }

    public double getGw() {
        return Gw;
    }

    public void setGw(double gw) {
        Gw = gw;
    }

    public double getC() {
        return C;
    }

    public void setC(double c) {
        C = c;
    }

    @Override
    public QuantumSimpleIndividual clone() throws CloneNotSupportedException {
        QuantumSimpleIndividual individual = new QuantumSimpleIndividual(this.data);

        individual.r_c = this.r_c;
        individual.Pw = this.Pw;
        individual.Gw = this.Gw;
        individual.C = this.C;
        individual.c = this.c;
        individual.cash = this.cash;
        individual.sum = this.sum;
        individual.shares = this.shares;

        individual.alpha = this.alpha;
        individual.fitness = this.fitness;
        individual.lastFitness = this.lastFitness;

        return individual;
    }
}
