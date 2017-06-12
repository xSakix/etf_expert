package org.eft.evol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eft.evol.stats.UnitAction;

import cern.jet.math.Arithmetic;
import cern.jet.random.Exponential;
import cern.jet.random.Uniform;

public class UnitSigmoidImpl extends AbstractUnit implements Unit {

	private static final float TRANSACTION_COST = 4.0f;
	private static final float MODIFIER = 0.1f;
	private static final float ERROR_TOLERANCE = 0.001f;

	public UnitSigmoidImpl(int ETF_SIZE, int index) {
		super(ETF_SIZE, index);
	}

	public UnitSigmoidImpl(int index, float[] character, float[] preference) {
		super(index, character, preference);
	}

	private float sigmoid(float x) {
		return (float) (1.0f / (1.0f + Math.exp((double) -x)));
	}

	private int chooseIndex(Set<Integer> etfValueMapKeySet) {
		List<Integer> indexes = new ArrayList<Integer>();

		for (int i = 0; i < etfValueMapKeySet.size(); i++) {
			int indexChoice = Uniform.staticNextIntFromTo(0, etfValueMapKeySet.size() - 1);
			Integer chosenIndex = etfValueMapKeySet.toArray(new Integer[] {})[indexChoice];
			indexes.add(chosenIndex + 1);
		}

		for (Integer index : indexes) {
			float result = (float) index * sigmoid(100 * preference[index - 1]);
			if (Math.abs(((float) index) - result) < ERROR_TOLERANCE && etfValueMapKeySet.contains(index - 1)) {
				return index - 1;
			}
		}

		return -1;
	}

	@Override
	public boolean buy(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {

		if (etfValueMap.get(cycle).size() == 0) {
			return false;
		}

		Set<Integer> etfValueMapKeySet = etfValueMap.get(cycle).keySet();

		int index = chooseIndex(etfValueMapKeySet);
		if (index == -1)
			return false;
		float nav = etfValueMap.get(cycle).get(index);
		
		return doBuyAction(iteration, cycle, etfValueMap, index, nav);
	}

	private boolean doBuyAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index, float nav) {
		if (cash < nav) {
			return false;
		}

		long shares = Arithmetic.floor((cash - TRANSACTION_COST) / nav);
		shares = Uniform.staticNextLongFromTo(1l, shares);
		while(((float)shares)*nav+TRANSACTION_COST > cash){
			shares--;
		}
		if(shares <= 0){
			return false;
		}

		float bought = ((float) shares) * nav;

		
		cash = cash - bought - TRANSACTION_COST;

		if (etfs.containsKey(index)) {
			shares += etfs.get(index);
		}

		etfs.put(index, shares);

		if (preference[index] + MODIFIER <= 1.0d)
			preference[index] += MODIFIER;

		UnitAction buyAction = new UnitAction();

		buyAction.actionType = ActionType.BUY;
		buyAction.cycle = cycle;
		buyAction.iteration = iteration;
		buyAction.indexOfETF = index;
		buyAction.nav = nav;
		buyAction.shares = shares;
		addGradientToAction(cycle, etfValueMap, buyAction);

		actions.add(buyAction);
		
		if(shares > 0l)
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

		int index = chooseIndex(etfs.keySet());

		if (!etfValueMap.get(cycle).containsKey(index)) {
			return false;
		}

		float nav = etfValueMap.get(cycle).get(index);

		return doSellAction(iteration, cycle, etfValueMap, index, nav);
	}

	private boolean doSellAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index, float nav) {
		if (preference[index] - MODIFIER > 0.0d)
			preference[index] -= MODIFIER;

		long shares = etfs.get(index);
		shares = Uniform.staticNextLongFromTo(1l, shares);
		cash += (float) (nav * ((float) shares));
		cash -= TRANSACTION_COST;
		if (etfs.get(index) - shares == 0) {
			etfs.remove(index);
		} else {
			etfs.put(index, etfs.get(index) - shares);
		}

		UnitAction sellAction = new UnitAction();

		sellAction.actionType = ActionType.SELL;
		sellAction.cycle = cycle;
		sellAction.iteration = iteration;
		sellAction.indexOfETF = index;
		sellAction.nav = nav;
		sellAction.shares = shares;
		addGradientToAction(cycle, etfValueMap, sellAction);

		actions.add(sellAction);
		
		return true;
	}

	@Override
	public Unit crossOver(Unit other) {

		int crossIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length - 1);
		int crossIndexPreference = Uniform.staticNextIntFromTo(0, this.preference.length - 1);

		float[] nCharacter = new float[this.character.length];
		float[] nPreference = new float[this.preference.length];

		float[] otherCharacter = other.getCharacter();
		for (int i = 0; i < this.character.length; i++) {
			if (i <= crossIndexCharacter) {
				nCharacter[i] = this.character[i];
			} else {
				nCharacter[i] = otherCharacter[i];
			}
		}

		float[] otherPreference = other.getPreferences();
		for (int i = 0; i < this.preference.length; i++) {
			if (i <= crossIndexPreference) {
				nPreference[i] = this.preference[i];
			} else {
				nPreference[i] = otherPreference[i];
			}
		}

		return new UnitSigmoidImpl(UnitSequenceGenerator.getID(), nCharacter, nPreference);
	}

	@Override
	public String toString() {
		return "UnitSigmoidImpl " + super.toString();
	}

	@Override
	protected String currentStateString() {
		return "UnitSigmoidImpl " + super.currentStateString();
	}	

	@Override
	protected void addGradientToAction(int cycle, List<Map<Integer, Float>> etfValueMap, UnitAction sellAction) {
		super.addGradientToAction(cycle, etfValueMap, sellAction);
		if(this.actions.size() > 0)
			this.sumGradient += this.actions.get(this.actions.size()-1).gradient;
	}

	@Override
	public void performAction(List<Map<Integer, Float>> navValues, int it, int cycle) {
		boolean actionPerformed = false;			

		if(Math.abs(ActionType.HOLD - ActionType.HOLD*sigmoid(100.0f*character[INDEX_HOLD] + sumGradient)) < ERROR_TOLERANCE){
			hold(it, cycle, navValues);
			return;
		}
		
		if(Math.abs(ActionType.SELL - ActionType.SELL*sigmoid(100.0f*character[INDEX_BUY] + sumGradient)) < ERROR_TOLERANCE){
			actionPerformed = sell(it, cycle, navValues);
		}
		
		if(Math.abs(ActionType.BUY - ActionType.BUY*sigmoid(100.0f*character[INDEX_SELL] + sumGradient)) < ERROR_TOLERANCE){
			actionPerformed = buy(it, cycle, navValues);
		}
		
		if(!actionPerformed){
			hold(it, cycle, navValues);
		}
	}
	
	@Override
	public String getDir(int iteration) {
		return "UnitSigmoidImpl("+iteration+")";
	}

}
