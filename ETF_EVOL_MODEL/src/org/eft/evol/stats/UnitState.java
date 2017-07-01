package org.eft.evol.stats;

import org.eft.evol.model.ETFMap;

import cern.colt.Arrays;

public class UnitState {

	public int iteration;
	public int cycle;
	public byte[] character;
	public byte[] buyPreference;
	public float nav;
	public int[] etfs;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(iteration);
		builder.append(',');
		builder.append(cycle);
		builder.append(',');
		builder.append(Arrays.toString(character));
		builder.append(",{");
		for (int etf_index = 0; etf_index < buyPreference.length; etf_index++) {
			builder.append(ETFMap.getInstance().getEtfName(etf_index));
			builder.append('=');
			builder.append(buyPreference[etf_index]);
			builder.append(',');
		}
		builder.append("},{");
		for (int etf_index = 0; etf_index < etfs.length; etf_index++) {
			builder.append(ETFMap.getInstance().getEtfName(etf_index));
			builder.append('=');
			builder.append(etfs[etf_index]);
			builder.append(',');
		}
		builder.append("},");
		builder.append(nav);
		builder.append('\n');

		return builder.toString();
	}
}
