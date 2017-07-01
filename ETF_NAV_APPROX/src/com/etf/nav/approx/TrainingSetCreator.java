package com.etf.nav.approx;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.neuroph.core.data.DataSet;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;

public class TrainingSetCreator {

	public static final double NORM = 1000.0d;

	public static DataSet getTrainingSet(ETF etf, int numOfInputs) {
		DataSet trainingSet = new DataSet(numOfInputs, 1);
		int index = 0;
		double[] history = new double[numOfInputs];

		for (NavData navData : etf.getNavDataList()) {

			if (index < numOfInputs) {
				history[index] = getRounded(navData);
			}
			if (index >= numOfInputs) {
				trainingSet.addRow(history, new double[] { getRounded(navData) });
				double[] historyTmp = Arrays.copyOf(history, numOfInputs);
				history = new double[numOfInputs];

				for (int i = 0; i < numOfInputs; i++) {
					if (i < numOfInputs - 1) {
						history[i] = historyTmp[i + 1];
					} else {
						history[i] = getRounded(navData);
					}
				}

			}

			index++;
		}

		return trainingSet;
	}

	public static DataSet getTrainingSet(ETF etf, int numOfInputs, int numOfOutputs) {

		DataSet trainingSet = new DataSet(numOfInputs, numOfOutputs);
		int indexInput = 0;
		int indexOutput = 0;
		double[] inputHistory = new double[numOfInputs];
		Double[] outputHistory = new Double[numOfOutputs];
		LinkedList<Double[]> outputList = new LinkedList<>();

		for (int i = 0; i < etf.getNavDataList().size() - numOfOutputs - 1; i++) {

			NavData navDataIn = etf.getNavDataList().get(i);

			NavData navDataOut = null;

			if (indexOutput == 0) {
				for (int initIndex = 1; initIndex <= numOfOutputs; initIndex++) {
					navDataOut = etf.getNavDataList().get(initIndex);
					outputHistory[indexOutput] = getRounded(navDataOut);
					indexOutput++;
				}
				navDataOut = etf.getNavDataList().get(numOfOutputs + 1);
			} else {
				navDataOut = etf.getNavDataList().get(i + numOfOutputs);

			}

			if (indexInput < numOfInputs) {
				inputHistory[indexInput] = getRounded(navDataIn);
			}

			if (indexOutput >= numOfOutputs) {
				outputList.addLast(outputHistory);
				Double[] historyTmp = Arrays.copyOf(outputHistory, numOfOutputs);
				outputHistory = new Double[numOfOutputs];
				for (int j = 0; j < numOfOutputs; j++) {
					if (j < numOfOutputs - 1) {
						outputHistory[j] = historyTmp[j + 1];
					} else {
						outputHistory[j] = getRounded(navDataOut);
					}
				}

			}

			indexOutput++;

			if (indexInput >= numOfInputs) {
				double[] out = new double[numOfOutputs];
				Double[] outTmp = outputList.removeFirst();
				for (int k = 0; k < numOfOutputs; k++) {
					out[k] = outTmp[k].doubleValue();
				}
				trainingSet.addRow(inputHistory, out);
				double[] historyTmp = Arrays.copyOf(inputHistory, numOfInputs);
				inputHistory = new double[numOfInputs];

				for (int j = 0; j < numOfInputs; j++) {
					if (j < numOfInputs - 1) {
						inputHistory[j] = historyTmp[j + 1];
					} else {
						inputHistory[j] = getRounded(navDataIn);
					}
				}

			}

			indexInput++;
		}

		return trainingSet;
	}

	public static double getRounded(NavData navData) {
		double val = (double) (navData.getNav() / NORM);

		return getRounded(val);

	}

	public static double getRounded(double val) {

		DecimalFormat decimalFormat = new DecimalFormat("###.#####");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		decimalFormat.setDecimalFormatSymbols(dfs);
		// System.out.println(val);
		return Double.valueOf(decimalFormat.format(val));

	}

}
