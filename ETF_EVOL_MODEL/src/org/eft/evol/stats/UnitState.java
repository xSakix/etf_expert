package org.eft.evol.stats;

import java.util.Map;

import org.eft.evol.model.ETFMap;

import cern.colt.Arrays;

public class UnitState {

	public int iteration;
	public int cycle;
	public float[] character;
	public float[] preference;
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
		for(int i = 0; i < preference.length;i++){
			builder.append(ETFMap.getInstance().getEtfName(i)+"="+preference[i]+",");
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
