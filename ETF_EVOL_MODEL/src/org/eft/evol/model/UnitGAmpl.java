package org.eft.evol.model;

import java.util.Arrays;
import java.util.BitSet;

import org.eft.evol.stats.UnitAction;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class UnitGAmpl extends AbstractUnit implements Unit {

	public UnitGAmpl(int ETF_SIZE, int index) {
		super(ETF_SIZE, index);
	}

	public UnitGAmpl(int index, byte[] character, byte[] preference) {
		super(index, character, preference);
	}

	@Override
	public boolean buy(int iteration, int cycle, float[][] etfValueMap) {

		int index = 0;
		
		if(cache.size()  < 10){		
			index = Uniform.staticNextIntFromTo(0, etfValueMap[cycle].length - 1);			
		}else{
			Integer[] indexes  = cache.keySet().toArray(new Integer[]{});
			int chosenIndex = Uniform.staticNextIntFromTo(0, indexes.length-1);
			try{
				index = indexes[ chosenIndex ];
			}catch(ArrayIndexOutOfBoundsException exc){
				index = Uniform.staticNextIntFromTo(0, etfValueMap[cycle].length - 1);
				System.err.println(String.format("index[%d] of %d length",chosenIndex,cache.size()));
			}
		}
		
		float nav = etfValueMap[cycle][index];
		if (nav == 0.0f) {
			return false;
		}

		byte choice = (byte) Uniform.staticNextIntFromTo(0, 100);
		if (preference[index] >= choice) {
			//incrementPreference(index, preference);
			return doBuyAction(iteration, cycle, etfValueMap, index, nav);
		}

		return false;
	}

	private boolean doBuyAction(int iteration, int cycle, float[][] etfValueMap, int index, float nav) {
		if (cash < nav) {
			return false;
		}

		int shares = (int) Arithmetic.floor((cash - TRANSACTION_COST) / nav);
		if (shares < 10) {
			return false;
		}
		shares = Uniform.staticNextIntFromTo(10, shares);

		while (((float) shares) * nav + TRANSACTION_COST > cash) {
			shares--;
		}
		if (shares <= 0) {
			return false;
		}

		float bought = ((float) shares) * nav;

		cash = cash - bought - TRANSACTION_COST;

		etfs[index] += shares;

		UnitAction buyAction = new UnitAction();

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
	public boolean sell(int iteration, int cycle, float[][] etfValueMap) {

		int index = Uniform.staticNextIntFromTo(0, etfValueMap[cycle].length - 1);
		if (etfs[index] == 0) {
			return false;
		}

		float nav = etfValueMap[cycle][index];
		if (nav == 0.0f) {
			return false;
		}

		return doSellAction(iteration, cycle, etfValueMap, index, nav);

	}

	private boolean doSellAction(int iteration, int cycle, float[][] etfValueMap, int index, float nav) {

		if (preference[index] - MODIFIER > 0.0f)
			preference[index] -= MODIFIER;

		int shares = Uniform.staticNextIntFromTo(1, etfs[index]);
		cash += (float) (nav * ((float) shares));
		cash -= TRANSACTION_COST;

		etfs[index] -= shares;

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

		byte[] nCharacter = crossOverCharacter(other);

		byte[] nPreference = crossoverPreference(other, preference, ActionType.BUY);

		return new UnitGAmpl(UnitSequenceGenerator.getID(), nCharacter, nPreference);
	}

	@Override
	public String toString() {
		return "UnitGAImpl " + super.toString();
	}

	@Override
	protected String currentStateString() {
		return "UnitGAImpl " + super.currentStateString();
	}

	@Override
	public String getDir(int iteration) {
		StringBuilder builder = new StringBuilder();
		builder.append(iteration);
		builder.append("_UnitGAImpl");
		return builder.toString();
	}

	@Override
	public void calculateDividends(float[][] dividends, int cycle) {
		for (int i = 0; i < etfs.length; i++) {
			if (etfs[i] > 0 && dividends[cycle][i] > 0.0f) {
				float dividends_payed = (float) etfs[i] * dividends[cycle][i];
				this.cash += dividends_payed;
			}
		}
	}

	@Override
	public void mutate() {
		for (int i = 0; i < this.character.length; i++) {
			this.character[i] = mutateByte(this.character[i]);
		}
		for (int i = 0; i < this.preference.length; i++) {
			this.preference[i] = mutateByte(this.preference[i]);
		}
	}

	private byte mutateByte(byte b) {
		BitSet bitSet1 = BitSet.valueOf(new byte[] { b });
		if (bitSet1.isEmpty()) {
			bitSet1.set(Uniform.staticNextIntFromTo(0, 7));
		} else {
			bitSet1.flip(Uniform.staticNextIntFromTo(0, 7));
		}

		byte result = 0;
		if (!bitSet1.isEmpty()) {
			result = bitSet1.toByteArray()[0];
		}
		return result > 99 ? 99 : result < 0 ? 0 : result;
	}

	@Override
	protected byte[] crossOverCharacter(Unit other) {
		byte[] nCharacter = new byte[this.character.length];

		for (int i = 0; i < this.character.length; i++) {
			nCharacter[i] = crossover(this.character[i], other.getCharacter()[i]);
		}

		return nCharacter;
	}

	@Override
	protected byte[] crossoverPreference(Unit other, byte[] preferenceMap, int actionType) {
		byte[] nPreference = new byte[this.preference.length];

		for (int i = 0; i < this.preference.length; i++) {
			nPreference[i] = crossover(this.preference[i], other.getBuyPreferenceMap()[i]);
		}

		return nPreference;
	}

	private byte crossover(byte b1, byte b2) {
		BitSet bitSet1 = BitSet.valueOf(new byte[] { b1 });
		BitSet bitSet2 = BitSet.valueOf(new byte[] { b2 });

		BitSet mask1 = new BitSet(Byte.SIZE);
		for (int i = 0; i < Byte.SIZE; i++) {
			mask1.set(i, Uniform.staticNextBoolean());
		}

		BitSet mask2 = (BitSet) mask1.clone();
		if (mask2.isEmpty()) {
			for (int i = 0; i < 7; i++) {
				mask2.set(i);
			}
		} else {
			mask2.flip(0, mask2.length());
		}

		BitSet child1A = (BitSet) bitSet1.clone();
		child1A.and(mask1);

		BitSet child1B = (BitSet) bitSet2.clone();
		child1B.and(mask2);

		child1A.or(child1B);

		byte result = 0;
		if (!child1A.isEmpty()) {
			result = child1A.toByteArray()[0];
		}
		return result > 99 ? 99 : result < 0 ? 0 : result;
	}

}
