package org.eft.evol.model;

import java.util.Arrays;

public class ETFMap {

	private String[] mapETFToIndex;

	private static ETFMap INSTANCE = null;

	private ETFMap(int eTF_SIZE) {
		mapETFToIndex = new String[eTF_SIZE];
	}

	public static ETFMap getInstance(int ETF_SIZE) {
		if (INSTANCE == null) {
			INSTANCE = new ETFMap(ETF_SIZE);
		}
		return INSTANCE;
	}

	public static ETFMap getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("Please call getInstance(int) to initialize etf name list.");
		}
		return INSTANCE;
	}

	public void putIndex(String etfName, int index) {
		if (index < mapETFToIndex.length)
			mapETFToIndex[index] = etfName;
	}

	public int getIndex(String etfName) {
		for (int i = 0; i < mapETFToIndex.length; i++) {
			if (mapETFToIndex[i].equals(etfName)) {
				return i;
			}
		}

		return -1;
	}

	public String getEtfName(int index) {
		if (index >= mapETFToIndex.length) {
			return null;
		}
		return mapETFToIndex[index];
	}

	public void removeIndex(int index) {
		String[] copy = Arrays.copyOf(mapETFToIndex, mapETFToIndex.length);
		mapETFToIndex = new String[mapETFToIndex.length-1];
		for(int i = 0; i < mapETFToIndex.length;i++){
			if(i < index){
				mapETFToIndex[i] = copy[i];
			}else{
				mapETFToIndex[i] = copy[i+1];
			}			
		}
	}
}
