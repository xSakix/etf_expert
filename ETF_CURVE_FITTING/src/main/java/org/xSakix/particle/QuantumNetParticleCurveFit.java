package org.xSakix.particle;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Uniform;
import org.xSakix.etfreader.EtfReader;
import org.xSakix.particle.neural.particle.NetParticle;
import org.xSakix.particle.neural.quantum.QuantumNet;
import org.xSakix.particle.neural.quantum.QuantumNetParticle;
import org.xSakix.tools.Errors;

import java.io.IOException;
import java.util.*;

public class QuantumNetParticleCurveFit {

    public static final int FRAME = 20;
    public static final int POP_SIZE = 100;
    public static final int ITER_MAX = 200;
    public static final int M = 3;
    private static final double FIT_TOL = 0.001;
    private static final double ERROR_TOL = 0.1;
    private static final double ALPHA = Uniform.staticNextDoubleFromTo(0.1,1.2);

    private static double[] adjustData(double[] data, double max) {
        double dataTemp[] = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            dataTemp[i] = data[i] / max;
        }

        return Arrays.copyOf(dataTemp, dataTemp.length);
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Alpha= "+ALPHA);

        double orig_data[] = EtfReader.readEtf("c:\\downloaded_data\\USD\\SPY.csv");
        //data adjustment, find max and normalize data on max
        double max = Arrays.stream(orig_data).max().getAsDouble();
        double data[] = adjustData(orig_data,max);

        List<double[]> xx = new ArrayList<>();
        DoubleArrayList tt = new DoubleArrayList();
        for(int i =0 ;i < data.length-M;i++){
            double x[] = new double[M];
            for(int j = 0; j < M;j++) {
                x[j] = data[i+j];
            }
            xx.add(x);
            tt.add(orig_data[i+M]);
        }
        double t[] = Arrays.copyOf(tt.elements(),tt.size());
        double x[][] =  xx.toArray(new double[][]{});

        List<QuantumNetParticle> particles = new ArrayList<>(POP_SIZE);
        for(int i = 0; i < POP_SIZE;i++){
            particles.add(new QuantumNetParticle(ALPHA,M,M*2+1,(M*2+1)*2+1));
        }

        DoubleArrayList fitnessHistory = new DoubleArrayList(ITER_MAX);

        int iterations = 0;

        QuantumNetParticle best = null;


        while (true) {
            if (endCondition(iterations,best,fitnessHistory)) {
                break;
            }

            QuantumNet sumNet = new QuantumNet(ALPHA,M,1,M*2+1,(M*2+1)*2+1);
            particles.stream().forEach(p -> sumNet.addToC(p.getNet()));
            sumNet.divCByParticlesSize(particles.size());

            particles.parallelStream().forEach(p -> {
                p.setC(sumNet);
                p.computeWeights();
                p.computeFitness(x,t,max);
            });

            Collections.sort(particles, new Comparator<QuantumNetParticle>() {
                @Override
                public int compare(QuantumNetParticle o1, QuantumNetParticle o2) {
                    return Double.compare(o1.getFitness(), o2.getFitness());
                }
            });

            fitnessHistory.add(particles.get(0).getFitness());

            if (best == null || best.getFitness() > particles.get(0).getFitness()) {
                best = particles.get(0);
                best.setGw(best.getGw());
                for(int i = 1; i < POP_SIZE;i++){
                    particles.get(i).setGw(best.getGw());
                }
            }
            System.out.println(String.format("Iteration = %d", iterations));
            System.out.println(String.format("Best fit weights = %s", best.getGw().toString()));
            System.out.println(String.format("Best fitness = %f", best.getFitness()));
            System.out.println(String.format("Actual fitness = %f", particles.get(0).getFitness()));
            System.out.println(String.format("Best RMS = %f", best.getRms()));

            iterations++;
        }

//        // create your PlotPanel (you can use it as a JPanel)
//        Plot2DPanel plot = new Plot2DPanel();
//        //Plot2DPanel plot1 = new Plot2DPanel();
//        Plot2DPanel plot2 = new Plot2DPanel();
//
//        // add a line plot to the PlotPanel
//        plot.addLinePlot("spy_plot", Color.blue, data);
//
//
//        double y[] = new double[data.length];
//        double xx[] = Arrays.copyOfRange(data, 0, data.length);
//        System.out.println("Training input length:" + x.length);
//        System.out.println("Testing input length:" + xx.length);
//        for (int i = 0; i < y.length; i++) {
//            if (i < data.length - FRAME)
//                y[i] = best.evaluate(xx[i]);
//            else
//                y[i] = best.evaluate(y[i - 1]);
//        }
//        plot.addLinePlot("curve_plot", Color.red, y);
//
        System.out.println("------------------RESULTS-----------------");
        double y[] = new double[data.length-M];
        double yy[] = new double[data.length-M];
        for (int i = data.length-FRAME; i < data.length-M; i++) {
            y[i] = best.evaluate(x[i])*max;
            yy[i] = t[i];
            double error = Math.abs( yy[i]-y[i]);
            System.out.println(String.format("error = %.3f-%.3f = %.3f", yy[i],y[i], error));
        }
        System.out.println(String.format("LSE=%f", Errors.leastSquareError(yy,y)));
//
//
//        plot2.addLinePlot("fitness",Arrays.copyOf(fitnessHistory.elements(),fitnessHistory.size()));
//
//        Dimension dim = new Dimension(800, 600);
//
//        // put the PlotPanel in a JFrame, as a JPanel
//        JFrame frame = new JFrame("SPY");
//        frame.setLayout(new GridLayout());
//        frame.setSize(dim);
//        frame.setMaximumSize(dim);
//        frame.setMinimumSize(dim);
//        frame.add(plot);
//        //frame.add(plot1);
//        frame.add(plot2);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static boolean endCondition(int iterations, QuantumNetParticle best, DoubleArrayList fitnessHistory) {
        int size = fitnessHistory.size();
        return (best!=null && best.getFitness() < ERROR_TOL) ||
                //iterations > ITER_MAX ||
                (size > 10 && Math.abs(fitnessHistory.get(size-1) - fitnessHistory.get(size-10)) < FIT_TOL);
    }

}
