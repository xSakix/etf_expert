package com.etfdatadatabase.domain;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ETF
{

    public static final int NUM_OF_DAYS = 8936;
    private String ticket;
    private List<NavData> navDataList;
    private List<NavData> dividendList;
    private float[] normalized = null;
    public Integer startIndex = null;

    // 1993-01-29
    // 2017-07-14

    public ETF(String ticket2)
    {
	this.ticket = ticket2;
    }

    public String getTicket()
    {
	return this.ticket;
    }

    public void addNavData(float nav, Date from)
    {
	if (this.navDataList == null)
	{
	    this.navDataList = new LinkedList<>();
	}

	this.navDataList.add(new NavData(nav, from));

	Collections.sort(this.navDataList, new Comparator<NavData>()
	{

	    @Override
	    public int compare(NavData o1, NavData o2)
	    {
		return o1.date.compareTo(o2.date);
	    }
	});
    }

    public List<NavData> getNavDataList()
    {
	if (this.navDataList == null)
	    return null;
	return this.navDataList;
    }

    public List<NavData> getDividendList()
    {
	if (this.dividendList == null)
	    return null;
	return this.dividendList;
    }

    public void addDividendData(float nav, Date from)
    {
	if (this.dividendList == null)
	{
	    this.dividendList = new LinkedList<>();
	}

	this.dividendList.add(new NavData(nav, from));

	Collections.sort(this.dividendList, new Comparator<NavData>()
	{

	    @Override
	    public int compare(NavData o1, NavData o2)
	    {
		return o1.date.compareTo(o2.date);
	    }
	});
    }

    public float[] normalised()
    {
	if (normalized == null)
	{
	    normalized = new float[NUM_OF_DAYS];
	    
	    Calendar start = Calendar.getInstance();
	    start.clear();
	    start.set(Calendar.YEAR, 1993);
	    start.set(Calendar.MONTH, 0);
	    start.set(Calendar.DAY_OF_MONTH, 29);
	    float last = 0.0f;
	    Map<Integer,Float> values = new Hashtable<>(NUM_OF_DAYS); 
	    for(NavData navData : navDataList){
		Calendar actual = Calendar.getInstance();
		actual.clear();
		actual.setTime(navData.getDate());
		actual.clear(Calendar.MILLISECOND);
		actual.clear(Calendar.SECOND);
		actual.clear(Calendar.MINUTE);
		actual.clear(Calendar.HOUR);
		values.put(actual.hashCode(), navData.getNav());
	    }
	    for(int i = 0; i < NUM_OF_DAYS;i++){
		if(values.containsKey(start.hashCode())){
		    if(startIndex == null){
			startIndex = i;
		    }
		    last = values.get(start.hashCode());
		    normalized[i] = last;		    
		}else{
		    normalized[i] = last;
		}
		start.add(Calendar.DAY_OF_MONTH, 1);
	    }
	}

	return this.normalized;
    }

    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((dividendList == null) ? 0 : dividendList.hashCode());
	result = prime * result
		+ ((navDataList == null) ? 0 : navDataList.hashCode());
	result = prime * result + ((ticket == null) ? 0 : ticket.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	ETF other = (ETF) obj;
	if (dividendList == null)
	{
	    if (other.dividendList != null)
		return false;
	} else if (!dividendList.equals(other.dividendList))
	    return false;
	if (navDataList == null)
	{
	    if (other.navDataList != null)
		return false;
	} else if (!navDataList.equals(other.navDataList))
	    return false;
	if (ticket == null)
	{
	    if (other.ticket != null)
		return false;
	} else if (!ticket.equals(other.ticket))
	    return false;
	return true;
    }

    @Override
    public String toString()
    {
	StringBuilder builder = new StringBuilder();

	builder.append("ETF [ticket=");
	builder.append(ticket);
	builder.append("]: \n");

	this.navDataList.stream().forEach(x -> {
	    builder.append(x.toString());
	    builder.append('\n');
	});

	builder.append("dividend:\n");

	this.dividendList.stream().forEach(x -> {
	    builder.append(x.toString());
	    builder.append('\n');
	});

	return builder.toString();
    }

}
