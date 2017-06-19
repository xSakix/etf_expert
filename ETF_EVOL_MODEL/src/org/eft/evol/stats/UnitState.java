package org.eft.evol.stats;

import java.util.HashMap;
import java.util.Map;

import org.eft.evol.model.ETFMap;

import cern.colt.Arrays;

public class UnitState {

	public int iteration;
	public int cycle;
	public float[] character;
	public Map<Integer,Float> buyPreference;
	public Map<Integer,Float> sellPreference;
	public Map<Integer,Float> holdPreference;
	public float nav;	
	public Map<Integer, Long> etfs;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(iteration);
		builder.append(",");
		builder.append(cycle);
		builder.append(",");
		builder.append(Arrays.toString(character));
		builder.append(",{");
		for(Integer key : buyPreference.keySet()){
			builder.append(ETFMap.getInstance().getEtfName(key)+"="+buyPreference.get(key)+",");
		}
		builder.append("},{");
		for(Integer key : sellPreference.keySet()){
			builder.append(ETFMap.getInstance().getEtfName(key)+"="+sellPreference.get(key)+",");
		}
		builder.append("},{");
		for(Integer key : holdPreference.keySet()){
			builder.append(ETFMap.getInstance().getEtfName(key)+"="+holdPreference.get(key)+",");
		}
		builder.append("},{");
		for(Integer key : etfs.keySet()){
			builder.append(ETFMap.getInstance().getEtfName(key)+"="+etfs.get(key)+",");
		}
		builder.append("},"+nav);
		builder.append("\n");
		
		return builder.toString();
	}
}
