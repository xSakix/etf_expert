package org.eft.evol.stats;

import org.eft.evol.model.ActionType;
import org.eft.evol.model.ETFMap;

public class UnitAction {

	public int actionType;
	public int cycle;
	public int iteration;
	public int shares;
	public float nav;
	public int indexOfETF;
	public float gradient;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(iteration);
		builder.append(",");
		builder.append(cycle);
		builder.append(",");
		builder.append(ActionType.labels[actionType]);
		builder.append(",");
		builder.append(shares);
		builder.append(",");
		builder.append((actionType == ActionType.HOLD) ? "" : ETFMap.getInstance().getEtfName(indexOfETF));
		builder.append(",");
		builder.append(nav);
		builder.append(",");
		builder.append(gradient);
		builder.append("\n");

		return builder.toString();
	}

}
