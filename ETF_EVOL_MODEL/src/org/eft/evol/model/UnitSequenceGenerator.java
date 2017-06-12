package org.eft.evol.model;

import java.util.concurrent.atomic.AtomicInteger;

public class UnitSequenceGenerator {
	
	private static final UnitSequenceGenerator INSTANCE = new UnitSequenceGenerator();
	
	private static final AtomicInteger ID = new AtomicInteger(0);
	
	public static int getID(){
		return INSTANCE.ID.getAndIncrement();
	}
	
}
