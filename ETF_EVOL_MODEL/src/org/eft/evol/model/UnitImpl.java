package org.eft.evol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eft.evol.stats.UnitAction;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class UnitImpl extends AbstractUnit implements Unit {

	private static final float TRANSACTION_COST = 4.0f;
	private static final float MODIFIER = 0.1f;
		
	public UnitImpl(int ETF_SIZE, int index) {
		super(ETF_SIZE,index);
	}
	
	public UnitImpl(int index, float[] character, float[] preference) {
		super(index,character,preference);
	}
		
	@Override
	public boolean buy(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {

		if(etfValueMap.get(cycle).size() == 0){
			return false;
		}
		
		boolean actionPerformed = false;
		Set<Integer> etfValueMapKeySet = etfValueMap.get(cycle).keySet();
		int max = etfValueMapKeySet.size();
		
		while(max > 0 ){
			int index = etfValueMapKeySet.toArray(new Integer[] {})[Uniform.staticNextIntFromTo(0, etfValueMapKeySet.size()-1)];
			float nav=etfValueMap.get(cycle).get(index);
			
			float prob = preference[index];
			float choice = Uniform.staticNextFloatFromTo(0.0f, 1.0f);
			if (prob >= choice) {
				return doBuyAction(iteration, cycle, etfValueMap, index, nav);
			}
			max--;
		}
		
		return false;
	}

	private boolean doBuyAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index, float nav) {
		if (cash < nav) {
			return false;
		}

		long shares = Arithmetic.floor((cash-TRANSACTION_COST) / nav);
		shares = Uniform.staticNextLongFromTo(1l, shares);
		while(((float)shares)*nav+TRANSACTION_COST > cash){
			shares--;
		}
		if(shares <= 0){
			return false;
		}
		
		
		float bought = ((float) shares) * nav;

		cash = cash - bought-TRANSACTION_COST;
		
		if (etfs.containsKey(index)) {
			shares += etfs.get(index);
		}

		etfs.put(index, shares);
		
		if(preference[index] + MODIFIER <= 1.0d)
			preference[index] += MODIFIER;

		UnitAction buyAction = new UnitAction();
		
		buyAction.actionType = ActionType.BUY;
		buyAction.cycle = cycle;
		buyAction.iteration=iteration;
		buyAction.indexOfETF = index;
		buyAction.nav = nav;
		buyAction.shares = shares;
		addGradientToAction(cycle,etfValueMap, buyAction);

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
		
		if(etfValueMap.get(cycle).size() == 0){
			return false;
		}
				
		while(true){
			
			int index = etfs.keySet().toArray(new Integer[]{})[Uniform.staticNextIntFromTo(0, etfs.keySet().size()-1)];
			
			if(!etfValueMap.get(cycle).containsKey(index)){
				return false;
			}
			
			float nav = etfValueMap.get(cycle).get(index);

			float prob = preference[index];
			float choice = Uniform.staticNextFloatFromTo(0.0f, 1.0f);
	
			if (prob >= choice) {
				return doSellAction(iteration, cycle, etfValueMap, index, nav);
			}
		}		
	}

	private boolean doSellAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index, float nav) {
		if(preference[index] - MODIFIER > 0.0d)
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
		sellAction.iteration=iteration;
		sellAction.indexOfETF = index;
		sellAction.nav = nav;
		sellAction.shares = shares;
		addGradientToAction(cycle,etfValueMap, sellAction);
				
		actions.add(sellAction);
		
		return true;
	}
	
	@Override
	public Unit crossOver(Unit other){
		
		int crossIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length-1);
		int crossIndexPreference = Uniform.staticNextIntFromTo(0, this.preference.length-1);

		float[] nCharacter = new float[this.character.length];
		float[] nPreference = new float[this.preference.length];

		float[] otherCharacter = other.getCharacter();
		for(int i = 0;i < this.character.length;i++){
			if(i <= crossIndexCharacter){
				nCharacter[i] = this.character[i]; 
			}else{
				nCharacter[i] = otherCharacter[i];
			}
		}

		float[] otherPreference = other.getPreferences();
		for(int i = 0;i < this.preference.length;i++){
			if(i <= crossIndexPreference){
				nPreference[i] = this.preference[i]; 
			}else{
				nPreference[i] = otherPreference[i];
			}
		}

		
		return new UnitImpl( UnitSequenceGenerator.getID(),nCharacter,nPreference);
	}

	@Override
	public String toString() {
		return "UnitImpl " + super.toString();
	}

	@Override
	protected String currentStateString() {
		return "UnitImpl " +super.currentStateString();
	}
	
	@Override
	public String getDir(int iteration) {
		return "UnitImpl("+iteration+")";
	}
}
