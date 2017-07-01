package com.etfdatadownload.main;

import java.util.Calendar;
import java.util.Date;

import yahoofinance.Stock;

public class ETFDownLoadBuilder {

	public static final Date dateFromDefault;
	public static final Date dateToDefault;
	public static final String freqDefault = "m";

	static {
		Calendar from = Calendar.getInstance();
		from.set(2008, 1, 1);
		dateFromDefault = from.getTime();

		dateToDefault = new Date();
	}

	String ticket;
	Date from;
	Date to;
	String freq;
	Stock stock;

	private ETFDownLoadBuilder() {
		this.ticket = "";
		this.from = dateFromDefault;
		this.to = dateToDefault;
		this.freq = freqDefault;
	}

	public ETFDownLoadBuilder ETFTicket(String ticket) {
		this.ticket = ticket;
		return this;
	}

	public static ETFDownLoadBuilder start() {
		return new ETFDownLoadBuilder();
	}

	public ETFDownLoadBuilder dateFrom(Date from2) {
		this.from = from2;
		return this;
	}

	public ETFDownLoadBuilder dateTo(Date to2) {
		this.to = to2;
		return this;
	}

	public ETFDownLoadBuilder frequency(String freq2) {
		this.freq = freq2;
		return this;
	}

	public ETFDownLoadBuilder stock(Stock stock) {
		this.stock = stock;
		return this;
	}

	public ETFDownloadParams build() {

		ETFDownloadParams params = new ETFDownloadParams();
		params.ticket = this.ticket;
		params.from = this.from;
		params.to = this.to;
		params.freq = this.freq;
		params.stock = stock;

		return params;
	}

}
