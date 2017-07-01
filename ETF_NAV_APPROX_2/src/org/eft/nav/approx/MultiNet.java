package org.eft.nav.approx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;
import com.google.common.io.Files;

public class MultiNet {

	private static final String ETF_NAME = "TLH";
	public static final double NORM = 1000.0d;

	public static void main(String[] args) throws IOException, InterruptedException {

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(Calendar.getInstance().getTimeInMillis()).iterations(10000).activation(Activation.RELU)
				.weightInit(WeightInit.XAVIER).learningRate(0.1).regularization(true).l2(1e-4).list()
				.layer(0, new DenseLayer.Builder().nIn(2).nOut(3).activation(Activation.RELU).build())
				.layer(1, new DenseLayer.Builder().nIn(3).nOut(3).activation(Activation.RELU).build())
				.layer(2,
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
								.activation(Activation.SIGMOID).nIn(3).nOut(1).build())
				.backprop(true).pretrain(false).build();

		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(10));

		ETF etf = FileLoader.loadETFData(ETF_NAME);
		String filename = prepareTraningSet(etf, 2, 1);

		LineIterator it = IOUtils.lineIterator(new InputStreamReader(new FileInputStream(filename)));
		while (it.hasNext()) {
			System.out.println(it.next());
		}

		// CollectionRecordReader
		RecordReader rr = new CSVSequenceRecordReader(0, ";");
		rr.initialize(new FileSplit(new File(filename)));
		DataSetIterator trainIter = new RecordReaderDataSetIterator(rr, 2285);

		while (rr.hasNext()) {
			List<Writable> line = rr.next();
			System.out.println(Arrays.toString(line.toArray()));
		}

		while (trainIter.hasNext()) {
			DataSet data = trainIter.next();
			System.out.println(data.toString());
		}

		for (int i = 0; i < 100000; i++) {
			model.fit(trainIter);
		}
		// Evaluation eval = new Evaluation(1);

		while (trainIter.hasNext()) {
			DataSet data = trainIter.next();
			INDArray outputs = model.output(trainIter);
			System.out.println(outputs);
		}
	}

	public static String prepareTraningSet(ETF etf, int numOfInputs, int numOfOutputs) throws IOException {
		int index = 0;
		int rowSize = numOfInputs + numOfOutputs;
		String filename = etf.getTicket() + rowSize + ".csv";
		File file = new File(filename);
		if (file.exists()) {
			return filename;
		} else {
			file.createNewFile();
		}

		double[] history = new double[rowSize];

		for (NavData navData : etf.getNavDataList()) {

			if (index < rowSize) {
				history[index] = getRounded(navData);
			}
			if (index >= rowSize) {

				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < rowSize; i++) {
					builder.append(history[i]);
					if (i < rowSize - 1) {
						builder.append(";");
					} else {
						builder.append("\n");
					}
				}
				Files.append(builder.toString(), file, Charset.defaultCharset());

				double[] historyTmp = Arrays.copyOf(history, rowSize);
				history = new double[rowSize];

				for (int i = 0; i < rowSize; i++) {
					if (i < rowSize - 1) {
						history[i] = historyTmp[i + 1];
					} else {
						history[i] = getRounded(navData);
					}
				}

			}

			index++;
		}

		return filename;
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
