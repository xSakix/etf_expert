package org.eft.evol.preference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eft.evol.model.ActionType;
import org.eft.evol.model.ETFMap;
import org.eft.evol.model.UnitSequenceGenerator;
import org.etf.provider.ConfigProvider;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class ChaosPreferenceUnit extends AbstractPreferenceUnit implements PreferenceUnit {

	
	private float[] r_pref; 
	private float[] r_char; 
	
	public ChaosPreferenceUnit(int ETF_SIZE, int index) {
		super(ETF_SIZE, index);
		initR();
	}

	public ChaosPreferenceUnit(int index, int size,float[] r_char, float[] r_pref) {
		super(size,index);
		this.r_pref = Arrays.copyOf(r_pref, r_pref.length);
		this.r_char = Arrays.copyOf(r_char, r_char.length);
	}
	
	public void initR(){
		r_pref = new float[preference.length];
		for(int i = 0; i < r_pref.length;i++){
			r_pref[i] = Uniform.staticNextFloatFromTo(1.1f, 4.0f);
		}
		
		r_char = new float[character.length];
		for(int i = 0; i < r_char.length;i++){
			r_char[i] = Uniform.staticNextFloatFromTo(1.1f, 4.0f);
		}

	}

	@Override
	public boolean buy(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {
		Map<Integer, Float> valueMap = etfValueMap.get(cycle);
		if (valueMap.size() == 0) {
			return false;
		}
		Set<Integer> keySet = valueMap.keySet();
		int index = keySet.toArray(new Integer[] {})[Uniform.staticNextIntFromTo(0, keySet.size() - 1)];
		float nav = valueMap.get(index);

		float prob = preference[index];
		float choice = Uniform.staticNextFloatFromTo(0.0f, 1.0f);
		if (prob >= choice) {
			//preference[index] = prob*r*(1-prob);
			return doBuyAction(iteration, cycle, etfValueMap, index, nav);
		}

		return false;
	}

	private boolean doBuyAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index, float nav) {
		if (cash < nav) {
			return false;
		}

		long shares = Arithmetic.floor((cash - TRANSACTION_COST) / nav);
		shares = Uniform.staticNextLongFromTo(1l, shares);
		while (((float) shares) * nav + TRANSACTION_COST > cash) {
			shares--;
		}
		if (shares <= 0) {
			return false;
		}

		float bought = ((float) shares) * nav;

		cash = cash - bought - TRANSACTION_COST;

		if (etfs.containsKey(index)) {
			shares += etfs.get(index);
		}

		etfs.put(index, shares);

		UnitPreferenceAction buyAction = new UnitPreferenceAction();

		buyAction.actionType = ActionType.BUY;
		buyAction.cycle = cycle;
		buyAction.iteration = iteration;
		buyAction.indexOfETF = index;
		buyAction.nav = nav;
		buyAction.shares = shares;
		buyAction.r_action = r_char[INDEX_BUY];
		addGradientToAction(cycle, etfValueMap, buyAction);

		actions.add(buyAction);

		if (shares > 0l)
			return true;

		return false;
	}

	@Override
	public boolean sell(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {

		if (etfs.keySet().size() == 0) {
			return false;
		}

		if (etfValueMap.get(cycle).size() == 0) {
			return false;
		}

		int index = etfs.keySet().toArray(new Integer[] {})[Uniform.staticNextIntFromTo(0, etfs.keySet().size() - 1)];
		if (!etfValueMap.get(cycle).containsKey(index))
			return false;

		float nav = etfValueMap.get(cycle).get(index);

		float prob = preference[index];
		float choice = Uniform.staticNextFloatFromTo(0.0f, 1.0f);

		if (prob >= choice) {
			return doSellAction(iteration, cycle, etfValueMap, index, nav);
		}

		return false;
	}

	private boolean doSellAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index,
			float nav) {
		/*
		 * if(preference[index] - MODIFIER > 0.0f) preference[index] -=
		 * MODIFIER;
		 */

		long shares = etfs.get(index);
		shares = Uniform.staticNextLongFromTo(1l, shares);
		cash += (float) (nav * ((float) shares));
		cash -= TRANSACTION_COST;
		if (etfs.get(index) - shares == 0) {
			etfs.remove(index);
		} else {
			etfs.put(index, etfs.get(index) - shares);
		}

		UnitPreferenceAction sellAction = new UnitPreferenceAction();

		sellAction.actionType = ActionType.SELL;
		sellAction.cycle = cycle;
		sellAction.iteration = iteration;
		sellAction.indexOfETF = index;
		sellAction.nav = nav;
		sellAction.shares = shares;
		sellAction.r_action = r_char[INDEX_SELL];

		addGradientToAction(cycle, etfValueMap, sellAction);

		actions.add(sellAction);

		return true;
	}

	@Override
	public PreferenceUnit crossOver(PreferenceUnit o) {

		ChaosPreferenceUnit other = (ChaosPreferenceUnit) o;
		
		int crossIndexCharacter = Uniform.staticNextIntFromTo(0, this.r_char.length - 1);
		int crossIndexPreference = Uniform.staticNextIntFromTo(0, this.r_pref.length - 1);

		float[] nCharacter = new float[this.r_char.length];
		float[] nPreference = new float[this.r_pref.length];

		float[] otherCharacter = other.getR_char();
		for (int i = 0; i < this.r_char.length; i++) {
			if (i <= crossIndexCharacter) {
				nCharacter[i] = this.r_char[i];
			} else {
				nCharacter[i] = otherCharacter[i];
			}
		}

		float[] otherPreference = other.getR_pref();
		for (int i = 0; i < this.r_pref.length; i++) {
			if (i <= crossIndexPreference) {
				nPreference[i] = this.r_pref[i];
			} else {
				nPreference[i] = otherPreference[i];
			}
		}

		return new ChaosPreferenceUnit(UnitSequenceGenerator.getID(),this.preference.length, nCharacter, nPreference);
	}

	@Override
	public String toString() {
		return "ChaosUnit " + super.toString();
	}

	@Override
	protected String currentStateString() {
		return "ChaosUnit " + super.currentStateString();
	}

	@Override
	public String getDir(int iteration) {
		return iteration+"_ChaosUnit";
	}

	@Override
	public void calculateDividends(List<Map<Integer, Float>> dividends, int cycle) {
		for (Integer key : etfs.keySet()) {
			if (etfs.get(key) > 0 && dividends.get(cycle).containsKey(key) && dividends.get(cycle).get(key) > 0.0f) {
				float dividends_payed = (float) etfs.get(key) * dividends.get(cycle).get(key);
				this.cash += dividends_payed;
			}
		}
	}
	
	public void growPreferences(){
		for(int i = 0; i< preference.length;i++){
			preference[i] = preference[i] * r_pref[i]*(1-preference[i]); 
		}
		for(int i = 0; i < character.length;i++){
			character[i] = character[i]*r_char[i]*(1-character[i]);
		}
	}

	@Override
	public synchronized void logToFile(int iteration) {
		super.logToFile(iteration);
		String directory = ConfigProvider.DIR + "/" + getDir(iteration) + "/" + this.currentState.nav;
		File historyLogFile = new File(directory + "/" + this.ID + "_history.log");
		
		try {
			StringBuilder builder = new StringBuilder();
			
			builder.append(",{");
			for(int i = 0; i < r_pref.length;i++){
				builder.append(ETFMap.getInstance().getEtfName(i)+"="+r_pref[i]+",");
			}
			builder.append('}');			
					
			
			Files.write(historyLogFile.toPath(), (builder.toString() + "\n").getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public float[] getR_pref() {
		return r_pref;
	}

	public float[] getR_char() {
		return r_char;
	}
	
	@Override
	public void hold(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {
		UnitPreferenceAction holdAction = new UnitPreferenceAction();

		holdAction.actionType = ActionType.HOLD;
		holdAction.cycle = cycle;
		holdAction.iteration = iteration;
		holdAction.r_action = r_char[INDEX_HOLD];
		addGradientToAction(cycle, etfValueMap, holdAction);

		actions.add(holdAction);
	}

	
}
