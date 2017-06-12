package org.eft.evol.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ETFMap {

	private Map<String,Integer> mapETFToIndex = new HashMap<>();
	
	private static ETFMap INSTANCE = new ETFMap(); 
	
	public static ETFMap getInstance(){
		return INSTANCE;
	}
	
	public void putIndex(String etfName, Integer index){
		mapETFToIndex.put(etfName, index);
	}
	
	public Integer getIndex(String etfName){
		return mapETFToIndex.get(etfName);
	}
	
	public String getEtfName(Integer index){
		for(Map.Entry<String, Integer> entry : mapETFToIndex.entrySet()){
			if(Objects.equals(index, entry.getValue())){
				return entry.getKey();
			}
		}
		return null;
	}
}
