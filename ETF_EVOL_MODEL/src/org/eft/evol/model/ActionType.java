package org.eft.evol.model;

public class ActionType {

	public static int HOLD = 1;
	public static int BUY = 2;
	public static int SELL = 3;
	public static int BANKRUPT = 4;

	// t = bankrupt
	public static final char[] labels = new char[] { 'U', 'H', 'B', 'S', 't' };
}
