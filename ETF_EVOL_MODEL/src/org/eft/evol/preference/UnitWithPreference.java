package org.eft.evol.preference;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eft.evol.model.ActionType;
import org.eft.evol.model.UnitSequenceGenerator;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class UnitWithPreference extends AbstractPreferenceUnit implements PreferenceUnit {

	public UnitWithPreference(int ETF_SIZE, int index) {
		super(ETF_SIZE, index);
	}

	public UnitWithPreference(int index, float[] character, float[] preference) {
		super(index, character, preference);
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

		if (preference[index] + MODIFIER <= 1.0f)
			preference[index] += MODIFIER;

		UnitPreferenceAction buyAction = new UnitPreferenceAction();

		buyAction.actionType = ActionType.BUY;
		buyAction.cycle = cycle;
		buyAction.iteration = iteration;
		buyAction.indexOfETF = index;
		buyAction.nav = nav;
		buyAction.shares = shares;
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
		addGradientToAction(cycle, etfValueMap, sellAction);

		actions.add(sellAction);

		return true;
	}

	@Override
	public PreferenceUnit crossOver(PreferenceUnit other) {

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

		return new UnitWithPreference(UnitSequenceGenerator.getID(), nCharacter, nPreference);
	}

	@Override
	public String toString() {
		return "UnitWithPreference " + super.toString();
	}

	@Override
	protected String currentStateString() {
		return "UnitWithPreference " + super.currentStateString();
	}

	@Override
	public String getDir(int iteration) {
		return iteration+"_UnitWithPreference";
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
}
