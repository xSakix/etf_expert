package org.xSakix.individuals;
import cern.jet.random.Uniform;
import org.xSakix.nn.Net;
import org.xSakix.nn.NetConfig;
import org.xSakix.nn.WhichFunctionEnum;


public class SimpleNeuralNetIndividual {

    protected static final double TRANSACTION_COST = 4.0;
    public static final double NORM = 1000.;
    protected double data[];
    protected double cash = 300.0;
    protected double sum = cash;
    protected int shares = 0;
    protected int num_hold = 0;
    protected int num_buy = 0;
    protected int num_sell = 0;
    protected double y[];
    protected Net net;

    public SimpleNeuralNetIndividual(double[] data) {
        this.data = data;
        init();
        NetConfig config = new NetConfig();
        config.alpha = 0.03;
        config.momentum = 0.75;
        config.hidenLayers = new int[]{3,7,15,31};
        config.n_inputs = 1;
        config.n_outputs =2;
        config.func = WhichFunctionEnum.TANH;
        config.reinforced=true;
        this.net = new Net(config);

    }

    private void init() {
        cash = 300.0;
        sum = cash;
        shares = 0;
        num_hold = 0;
        num_buy = 0;
        num_sell = 0;
        this.y = new double[data.length];
    }

    public void simulate() {
        for (int i = 0; i < data.length; i++) {
            double price = data[i];
            // kazdy mesiac (30dni) investujeme 300
            if (i % 30 == 0) {
                this.cash += 300.;
                this.sum += 300.;
            }


            double buy_choice = 0.90;
            double sell_choice = 0.10;
            y[i] = net.eval(new double[]{price/ NORM});
            // buy akcia
            if (y[i] >= buy_choice) {
                this.num_buy += 1;
                int num_shares = (int) Math.round((this.cash-TRANSACTION_COST) / price);
                this.cash = this.cash - (price * num_shares)-TRANSACTION_COST;
                this.shares += num_shares;
            }
            // sell akcia
            else if (y[i] <= sell_choice) {
                this.num_sell += 1;
                this.cash += (price * this.shares)-TRANSACTION_COST;
                this.shares = 0;
            }else{
                this.num_hold += 1;
            }
        }
    }

    public double total() {
        return cash + data[data.length - 1] * shares;
    }

    public void reinitialize() {
        init();
    }

    public double[] y(){
        return this.y;
    }

    public void retrain(double y_other[]){
        for (int i = 0; i < data.length; i++) {
            double out = net.eval(new double[]{data[i] / 1000.});
            net.backpropagate(out, y_other[i], new double[]{data[i]/1000.});
            net.computeWeights();
        }
    }

    @Override
    public SimpleNeuralNetIndividual clone() throws CloneNotSupportedException {
        SimpleNeuralNetIndividual individual = new SimpleNeuralNetIndividual(data);

        individual.cash = this.cash;
        individual.sum = this.sum;
        individual.shares = this.shares;
        individual.num_hold = this.num_hold;
        individual.num_buy = this.num_buy;
        individual.num_sell = this.num_sell;

        return individual;
    }

    public void print() {
         System.out.println( "fitness investment: " + this.sum);
         System.out.println( "num of shares: " + this.shares);
         System.out.println( "available cash: " + this.cash);
         System.out.println( "fitness: " + this.total());
    }

    public double invested() {
        return this.sum;
    }

}
