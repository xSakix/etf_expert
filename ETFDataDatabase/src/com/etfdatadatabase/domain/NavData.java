package com.etfdatadatabase.domain;

import java.util.Date;

public class NavData {

	Float nav;
	Date date;
	
	public NavData(Float nav, Date date) {
		super();
		this.nav = nav;
		this.date = date;
	}
	
	public Float getNav() {
		return nav;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return "NavData [nav=" + nav + ", date=" + date + "]";
	}	
	
}
