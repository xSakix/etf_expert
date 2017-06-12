package org.eft.evol.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eft.evol.stats.UnitAction;
import org.eft.evol.stats.UnitState;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.learning.BackPropagation;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class UnitNeuralImpl implements Unit {

	private static final double TRANSACTION_COST = 4.0d;
	private static final double MODIFIER = 0.1d;
	
	public static final String DIR;
	static{
		Calendar cal = Calendar.getInstance();
		String sub = ""+
					cal.get(Calendar.YEAR)+
					cal.get(Calendar.MONTH)+
					cal.get(Calendar.DAY_OF_MONTH)+
					cal.get(Calendar.HOUR)+
					cal.get(Calendar.MINUTE)+
					cal.get(Calendar.SECOND);
		DIR="d:\\DATA\\etf_evolution\\"+sub+"\\";
		try {
			Files.createDirectories(new File(DIR).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	NeuralNetwork<BackPropagation> nn;
	
	double[] preference;
	int ID = 0;
	double cash = 300.0d;
	
	Map<Integer, Long> etfs = new HashMap<>();

	List<UnitAction> actions = new ArrayList<>();
	
	//List<UnitState> stateList = new ArrayList<>();
	UnitState currentState;
	
	public UnitNeuralImpl(int ETF_SIZE, int index) {
		this.preference = new double[ETF_SIZE];
		this.ID = index;
		init();
	}

	private void init() {
		for (int i = 0; i < preference.length; i++) {
			preference[i] = Uniform.staticNextDoubleFromTo(0.0d, 1.0d);
		}

	}
	
	public void appendHistory(int it, int cycle, Map<Integer, Float> etfValueMap){
		
		this.currentState = new UnitState();
		
		currentState.nav = this.netAssetValue(etfValueMap);
		currentState.iteration=it;
		currentState.cycle=cycle;
		currentState.preference = Arrays.copyOf(preference, preference.length);
		currentState.etfs=Collections.unmodifiableMap(this.etfs);
		
		//this.stateList.add(currentState);
	}
	
	
	
	@Override
	public void buy(int iteration, int cycle, Map<Integer, Float> etfValueMap) {

		int index = etfValueMap.keySet().toArray(new Integer[] {})[Uniform.staticNextIntFromTo(0, etfValueMap.keySet().size()-1)];
		double prob = preference[index];
		double nav = etfValueMap.get(index);
		double choice = Uniform.staticNextDoubleFromTo(0.0, 1.0);
		if (prob >= choice) {
			if (cash < nav) {
				return;
			}

			long shares = Arithmetic.floor((cash-TRANSACTION_COST) / nav);
			shares = Uniform.staticNextLongFromTo(1l, shares);
			double bought = ((double) shares) * nav;

			cash = cash - bought-TRANSACTION_COST;
		
			UnitAction buyAction = new UnitAction();
			
			buyAction.actionType = ActionType.BUY;
			buyAction.cycle = cycle;
			buyAction.iteration=iteration;
			buyAction.indexOfETF = index;
			buyAction.nav = nav;
			buyAction.shares = shares;

			actions.add(buyAction);
			
			if (etfs.containsKey(index)) {
				shares += etfs.get(index);
			}

			etfs.put(index, shares);
			
			if(preference[index] + MODIFIER <= 1.0d)
				preference[index] += MODIFIER;

		}
	}

	@Override
	public void sell(int iteration, int cycle, Map<Integer, Float> etfValueMap) {
		
		if (etfs.keySet().size() == 0) {
			return;
		}
		
		int index = etfs.keySet().toArray(new Integer[]{})[Uniform.staticNextIntFromTo(0, etfs.keySet().size()-1)];
		
		double prob = preference[index];
		double choice = Uniform.staticNextDoubleFromTo(0.0, 1.0);

		if (prob >= choice) {
			if(preference[index] - MODIFIER > 0.0d)
				preference[index] -= MODIFIER;
			
			double nav = etfValueMap.get(index);
			long shares = etfs.get(index);
			shares = Uniform.staticNextLongFromTo(1l, shares);
			cash += (double) (nav * ((double) shares));
			cash -= TRANSACTION_COST;
			if (etfs.get(index) - shares == 0) {
				etfs.remove(index);
			} else {
				etfs.put(index, etfs.get(index) - shares);
			}

			UnitAction sellAction = new UnitAction();
			
			sellAction.actionType = ActionType.SELL;
			sellAction.cycle = cycle;
			sellAction.iteration=iteration;
			sellAction.indexOfETF = index;
			sellAction.nav = nav;
			sellAction.shares = shares;

			actions.add(sellAction);
		}
	}

	@Override
	public void hold(int iteration,int cycle) {
		UnitAction holdAction = new UnitAction();
		
		holdAction.actionType = ActionType.HOLD;
		holdAction.cycle = cycle;
		holdAction.iteration=iteration;

		actions.add(holdAction);
	}

	@Override
	public double buyProb() {
		return -1;
	}

	@Override
	public double sellProb() {
		return -1;
	}

	@Override
	public double holdProb() {
		return -1;
	}

	@Override
	public float netAssetValue(Map<Integer, Float> etfValueMap) {
		double sum = cash;
		for (Integer indexEtf : etfs.keySet()) {
			sum += (double) (etfs.get(indexEtf) * etfValueMap.get(indexEtf));
		}
		return sum;
	}

	@Override
	public String toString() {
		return "[["+ID+"],null,"+ Arrays.toString(preference) +",[" + cash + "]," + etfs.toString()+"]";
	}

	@Override
	public void resetAssets(int iteration) {
		this.cash = 300.0d;
		this.etfs = new HashMap<>();
	}

	@Override
	public int getID() {
		return this.ID;
	}

	@Override
	public void logToFile(int iteration) {
		String directory = DIR+"/"+iteration+"/"+this.currentState.nav;
		File dir = new File(directory);
		File actionLogFile = new File(directory+"/action.log");
		File historyLogFile = new File(directory+"/history.log");
	
		if(!Files.exists(dir.toPath())){
			try {
				Files.createDirectories(dir.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		if(!Files.exists(actionLogFile.toPath())){
			try {
				Files.createFile(actionLogFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		if(!Files.exists(historyLogFile.toPath())){
			try {
				Files.createFile(historyLogFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		
		try {
			for(UnitAction action : actions){
				Files.write(actionLogFile.toPath(), action.toString().getBytes(), StandardOpenOption.APPEND);
			}
//			for(UnitState state:stateList){
//				Files.write(historyLogFile.toPath(), state.toString().getBytes(), StandardOpenOption.APPEND);
//			}
			Files.write(historyLogFile.toPath(), currentState.toString().getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void resetLogs() {
		this.currentState = null;
		this.actions.clear();
	}

	@Override
	public void addCash(double cash) {
		this.cash+=cash;
	}

}
