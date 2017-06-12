package com.etfdatadatabase.domain;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ETF {

	private String ticket;
	private List<NavData> navDataList;
	
	public ETF(String ticket2) {
		this.ticket = ticket2;
	}
	
	public String getTicket(){
		return this.ticket;
	}

	public void addNavData(float nav, Date from){
		if(this.navDataList == null){
			this.navDataList = new LinkedList<>();
		}
		
		this.navDataList.add(new NavData(nav, from));
		
		Collections.sort(this.navDataList, new Comparator<NavData>() {

			@Override
			public int compare(NavData o1, NavData o2) {
				return o1.date.compareTo(o2.date);
			}
		});
	}
	
	public List<NavData> getNavDataList(){
		if(this.navDataList == null)
			return null;
		return Collections.unmodifiableList(this.navDataList);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((navDataList == null) ? 0 : navDataList.hashCode());
		result = prime * result + ((ticket == null) ? 0 : ticket.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ETF other = (ETF) obj;
		if (navDataList == null) {
			if (other.navDataList != null)
				return false;
		} else if (!navDataList.equals(other.navDataList))
			return false;
		if (ticket == null) {
			if (other.ticket != null)
				return false;
		} else if (!ticket.equals(other.ticket))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.navDataList.stream().forEach(x-> builder.append(x.toString() +"\n"));
		return "ETF [ticket=" + ticket + "]: \n"+builder.toString();
	}
	
	
}
