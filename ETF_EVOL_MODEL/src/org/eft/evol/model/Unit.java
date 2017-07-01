package org.eft.evol.model;

import java.util.List;
import java.util.Map;

public interface Unit {

	public static final float BASE_CASH = 300.0f;

	public static int INDEX_BUY = 0;
	public static int INDEX_SELL = 1;
	public static int INDEX_HOLD = 2;

	public void performAction(float[][] navValues, int it, int cycle);

	public boolean buy(int iteration, int cycle, float[][] etfValueMap);

	public boolean sell(int iteration, int cycle, float[][] etfValueMap);

	public void hold(int iteration, int cycle, float[][] etfValueMap);

	public byte buyProb();

	public byte sellProb();

	public byte holdProb();

	public float netAssetValue(int cycle, float[][] navValues);

	public void resetAssets(int iteration);

	public int getID();

	public void logToFile(int iteration);

	public void resetLogs();

	public void appendHistory(int it, int cycle, float[][] navValues);

	public void addCash(float cash);

	public Unit crossOver(Unit other);

	public void mutate();

	byte[] getCharacter();

	byte[] getBuyPreferenceMap();

	float getInvesment();

	public void calculateDividends(float[][] dividends, int cycle);
}
