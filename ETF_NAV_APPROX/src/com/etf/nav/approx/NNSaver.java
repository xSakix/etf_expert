package com.etf.nav.approx;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MLPReluNet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

public class NNSaver {

	public static void saveNet(NeuralNetwork<?> nn, DataSet orig, DataSet calc, long time) throws IOException{
				
		String fileName = getFileName(nn);
		nn.save("nets/"+fileName+".net");
		
		File stats = new File("nets/stats/"+fileName+".csv");
		if(!stats.exists()){
			stats.createNewFile();
		}else{
			stats.delete();
			stats.createNewFile();
		}
		
		// Create Chart
	    XYChart chart = new XYChartBuilder().width(800).height(600).title("error").build();

	    // Customize Chart
	    chart.getStyler().setLegendVisible(true);
		
		double sumError = 0.0d;
		
		List<Integer> xData = new ArrayList<Integer>();
 	    List<Double> yData = new ArrayList<Double>();

 	    int index = 0;
		
		for(int i = 0;i < calc.size();i++){
			StringBuilder builder = new StringBuilder();
			double origOut = orig.get(i).getDesiredOutput()[0];
			double calcOut = calc.get(i).getDesiredOutput()[0];
			double error = 0.5d*Math.pow(Math.abs(origOut - calcOut),2);
			sumError+=error;
			
			builder.append(origOut+";");
			builder.append(calcOut+";");
			builder.append(String.format("%.7f",error)+"\n");
			Files.write(stats.toPath(), builder.toString().getBytes(),StandardOpenOption.APPEND);
			
			xData.add(index++);
			yData.add(error);			
		}

		XYSeries series = chart.addSeries("error", xData, yData);
     	series.setLineColor(Color.RED);
		
		new SwingWrapper<XYChart>(chart).displayChart();
		
		double avgError = sumError/((double)calc.size());
		
		File statsFile = new File("nets/stats/"+fileName+".stats");
		if(!statsFile.exists()){			
			statsFile.createNewFile();
		}
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("avgError:%.7f",avgError)+"\n");
		builder.append("Time[s]:"+time/1000l);
		Files.write(statsFile.toPath(), builder.toString().getBytes(), StandardOpenOption.WRITE);
	}

	public static String getFileName(NeuralNetwork<?> nn) {
		StringBuilder builder = new StringBuilder();
		
		if(nn instanceof MultiLayerPerceptron){
			builder.append("MLP_SIGMOID_");
		}else if(nn instanceof MLPReluNet){
			builder.append("MLP_RELU_");
		}else{
			builder.append("NN_");
		}
		
		
		for(Layer l : nn.getLayers()){
			builder.append(l.getNeuronsCount()+"_");
		}
		String maxError = String.valueOf(((BackPropagation)nn.getLearningRule()).getMaxError()).replace(".", "");
		builder.append(maxError +"_");
		
		String maxIt = String.valueOf(((BackPropagation)nn.getLearningRule()).getMaxIterations()).replace(".", "");
		builder.append(maxIt+"_");
		
		String learningRate = String.valueOf(((BackPropagation)nn.getLearningRule()).getLearningRate()).replace(".", "");
		builder.append(learningRate +"_");
		
		
		String fileName = builder.toString().substring(0, builder.toString().length());
		return fileName;
	}
}
