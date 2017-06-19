package org.eft.evol.model;

import java.util.List;
import java.util.Map;

public interface Unit {

	public static final float BASE_CASH = 300.0f;

	
	public static int INDEX_BUY=0;
	public static int INDEX_SELL=1;
	public static int INDEX_HOLD=2;
	
	public void performAction(List<Map<Integer, Float>> navValues, int it, int cycle);
	
	public boolean buy(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap);
	public boolean sell(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap);
	public void hold(int iteration,int cycle,List<Map<Integer, Float>> etfValueMap);
	public void checkBankrupt(int iteration, int cycle, Map<Integer, Float> actual,Map<Integer, Float> previous);
	
	public float buyProb();
	public float sellProb();
	public float holdProb();
	
	public float netAssetValue(int cycle,List<Map<Integer, Float>> navValues);
	
	public void resetAssets(int iteration);
			
	public int getID();
	
	public void logToFile(int iteration);
	
	public void resetLogs();
	
	public void appendHistory(int it, int cycle, List<Map<Integer, Float>> navValues);
	
	public void addCash(float cash);
	
	public Unit crossOver(Unit other);
	public void mutate();
	
	float[] getCharacter();
	Map<Integer,Float> getBuyPreferenceMap();
	Map<Integer,Float> getSellPreferenceMap();
	Map<Integer,Float> getHoldPreferenceMap();
	
	float getInvesment();
}
