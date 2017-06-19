package org.eft.evol.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eft.evol.stats.UnitAction;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class UnitChoosyImpl extends AbstractUnit implements Unit {

	
	public UnitChoosyImpl(int ETF_SIZE, int index) {
		super(ETF_SIZE,index);
	}
	
	public UnitChoosyImpl(int index, float[] character, Map<Integer,Float> preference, Map<Integer,Float> sellPreference, Map<Integer,Float> holdPreference) {
		super(index,character,preference);
		this.sellPreference = sellPreference;
		this.holdPreference = holdPreference;
	}

	@Override
	public boolean buy(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {
		Map<Integer, Float> valueMap = etfValueMap.get(cycle);
		if(valueMap.size() == 0){
			return false;
		}
		Set<Integer> keySet = valueMap.keySet();
		int index = keySet.toArray(new Integer[] {})[Uniform.staticNextIntFromTo(0, keySet.size()-1)];
		float nav=valueMap.get(index);
		
		float prob = getBuyPreference(index);
		getHoldPreference(index);
		
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

		incrementPreference(index,buyPreference);

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
		
		int index = etfs.keySet().toArray(new Integer[]{})[Uniform.staticNextIntFromTo(0, etfs.keySet().size()-1)];
		if(!etfValueMap.get(cycle).containsKey(index))
			return false;
		
		float nav = etfValueMap.get(cycle).get(index);
		
		float prob = getSellPreference(index);
		
		float choice = Uniform.staticNextFloatFromTo(0.0f, 1.0f);

		if (prob >= choice) {
			return doSellAction(iteration, cycle, etfValueMap, index, nav);
		}
		
		return false;
	}

	private boolean doSellAction(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap, int index, float nav) {
		/*if(preference[index] - MODIFIER > 0.0f)
			preference[index] -= MODIFIER;*/
		
		
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
		
		incrementPreference(index, sellPreference);
		if(holdPreference.containsKey(index)){
			holdPreference.put(index, holdPreference.get(index)-MODIFIER);
		}
		
		return true;
	}
	
	@Override
	public Unit crossOver(Unit other){
		
		float[] nCharacter = crossOverCharacter(other);

		Map<Integer, Float> nPreference = crossoverPreference(other,buyPreference,ActionType.BUY);
		Map<Integer, Float> nSellPreference = crossoverPreference(other,sellPreference,ActionType.SELL);
		Map<Integer, Float> nHoldPreference = crossoverPreference(other,holdPreference,ActionType.HOLD);
		
		return new UnitChoosyImpl( UnitSequenceGenerator.getID(),nCharacter,nPreference,nSellPreference,nHoldPreference);
	}

	
	

	@Override
	public String toString() {
		return "UnitChoosyImpl " + super.toString();
	}

	@Override
	protected String currentStateString() {
		return "UnitChoosyImpl "+super.currentStateString();
	}

	@Override
	public String getDir(int iteration) {
		return "UnitChoosyImpl("+iteration+")";
	}
	
	
	
}
