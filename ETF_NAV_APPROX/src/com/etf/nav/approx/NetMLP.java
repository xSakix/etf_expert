package com.etf.nav.approx;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MLPReluNet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.TransferFunctionType;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.loader.FileLoader;

public class NetMLP {

	private static final String ETF_NAME = "VBK";
	private static final int SIZE = 4;
	private static final int TEST_SIZE = 365;

	public static void main(String[] args) throws IOException {
		long timeStart = System.currentTimeMillis();
		ETF etf = FileLoader.loadETFData(ETF_NAME);
		System.out.println(etf.toString());

		// Create Chart
		XYChart chart = new XYChartBuilder().width(800).height(600).title(etf.getTicket()).build();

		// Customize Chart
		chart.getStyler().setLegendVisible(true);
		System.out.println("SIZE: " + SIZE);
		DataSet trainingSet = TrainingSetCreator.getTrainingSet(etf, SIZE);

		for (DataSetRow row : trainingSet.getRows()) {
			String in = Arrays.toString(row.getInput());
			String out = Arrays.toString(row.getDesiredOutput());
			System.out.println(in + "->" + out);
		}

		addXYSeries(chart, "training", trainingSet, Color.BLUE);

		int maxIterations = 100000;

		@SuppressWarnings("rawtypes")
		NeuralNetwork neuralNet = new MultiLayerPerceptron(SIZE, 4, 1);

		BackPropagation bpg = (BackPropagation) neuralNet.getLearningRule();
		bpg.setMaxError(0.000001d);
		bpg.setLearningRate(0.3d);
		bpg.setMaxIterations(maxIterations);

		DataSet subTrainingSet = new DataSet(SIZE);
		for (int i = 0; i < trainingSet.getRows().size() - TEST_SIZE; i++) {
			subTrainingSet.addRow(trainingSet.getRowAt(i));
		}

		String fileName = NNSaver.getFileName(neuralNet);
		fileName = "nets/" + fileName + ".net";
		if (new File(fileName).exists()) {
			neuralNet = NeuralNetwork.createFromFile(fileName);
		} else {
			neuralNet.learn(subTrainingSet);
		}

		double sumError = 0.0d;
		double maxError = -Double.MAX_VALUE;

		DataSet calculated = new DataSet(trainingSet.getInputSize(), trainingSet.getOutputSize());
		for (DataSetRow row : subTrainingSet.getRows()) {
			neuralNet.setInput(row.getInput());
			neuralNet.calculate();
			double[] out = Arrays.copyOf(neuralNet.getOutput(), neuralNet.getOutput().length);
			calculated.addRow(row.getInput(), out);
			double error = 0.5d
					* Math.pow(Math.abs(row.getDesiredOutput()[0] - TrainingSetCreator.getRounded(out[0])), 2);
			if (error > maxError) {
				maxError = error;
			}
			sumError += error;
			System.out.println("calc" + Arrays.toString(row.getInput()) + "->" + Arrays.toString(out)
					+ Arrays.toString(row.getDesiredOutput()) + "| error=" + error);
		}

		System.out.println(String.format("Average error:%.7f | Max error:%.7f",
				sumError / ((double) calculated.getRows().size()), maxError));

		DataSetRow rowLast = calculated.getRowAt(calculated.getRows().size() - 1);
		double[] newRow = new double[SIZE];
		for (int i = 0; i < SIZE - 1; i++) {
			newRow[i] = rowLast.getInput()[i + 1];
		}
		newRow[SIZE - 1] = rowLast.getDesiredOutput()[0];
		DataSetRow rowNew = new DataSetRow(newRow);
		for (int i = 0; i < TEST_SIZE; i++) {
			neuralNet.setInput(rowNew.getInput());
			neuralNet.calculate();
			double[] out = Arrays.copyOf(neuralNet.getOutput(), neuralNet.getOutput().length);
			calculated.addRow(rowNew.getInput(), out);

			newRow = new double[SIZE];
			for (int j = 0; j < SIZE - 1; j++) {
				newRow[j] = rowNew.getInput()[j + 1];
			}
			newRow[SIZE - 1] = out[0];
			rowNew = new DataSetRow(newRow);
		}

		addXYSeries(chart, "calculated", calculated, Color.RED);

		new SwingWrapper<XYChart>(chart).displayChart();

		XYChart chart2 = new XYChartBuilder().width(800).height(600).title(etf.getTicket()).build();

		DataSet setOriginal = new DataSet(trainingSet.getInputSize(), trainingSet.getOutputSize());
		for (int i = trainingSet.getRows().size() - TEST_SIZE; i < trainingSet.getRows().size(); i++) {
			setOriginal.addRow(trainingSet.getRowAt(i));
		}

		DataSet setCalc = new DataSet(calculated.getInputSize(), calculated.getOutputSize());
		for (int i = calculated.getRows().size() - TEST_SIZE; i < calculated.getRows().size(); i++) {
			setCalc.addRow(calculated.getRowAt(i));
		}

		addXYSeries(chart2, "original", setOriginal, Color.BLUE);
		addXYSeries(chart2, "calculated", setCalc, Color.RED);
		new SwingWrapper<XYChart>(chart2).displayChart();

		long timeTaken = System.currentTimeMillis() - timeStart;
		System.out.println("Experiment took time[s]=" + (timeTaken / 1000l));

		NNSaver.saveNet(neuralNet, trainingSet, calculated, timeTaken);

	}

	private static void addXYSeries(XYChart chart, String name, DataSet dataSet, Color color) {

		List<Integer> xData = new ArrayList<Integer>();
		List<Double> yData = new ArrayList<Double>();

		int index = 0;
		for (DataSetRow row : dataSet.getRows()) {
			xData.add(index++);
			double output = row.getDesiredOutput()[0] * TrainingSetCreator.NORM;
			yData.add(output);

		}

		XYSeries series = chart.addSeries(name, xData, yData);
		series.setLineColor(color);
	}

}
