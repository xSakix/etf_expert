package com.etf.nav.approx;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.neuroph.core.data.DataSet;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;

public class TrainingSetCreatorRELU {

	public static DataSet getTrainingSet(ETF etf,int numOfInputs){
		DataSet trainingSet = new DataSet(numOfInputs, 1);
		int index=0;
		double[] history = new double[numOfInputs];
		
		for(NavData navData : etf.getNavDataList()){
			
			if(index < numOfInputs){
				history[index] = navData.getNav();
			}
			if(index >= numOfInputs){
				trainingSet.addRow(history, new double[]{navData.getNav()});
				double[] historyTmp = Arrays.copyOf(history, numOfInputs);
				history = new double[numOfInputs];
				
				for (int i = 0; i < numOfInputs;i++) {
					if(i < numOfInputs-1){
						history[i] = historyTmp[i+1];
					}else{
						history[i] = navData.getNav();
					}
				}
				
			}
			
			index++;
		}
		
		return trainingSet;
	}
		
}
