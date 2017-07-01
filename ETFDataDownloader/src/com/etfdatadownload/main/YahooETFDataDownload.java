package com.etfdatadownload.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import yahoofinance.Stock;
import yahoofinance.Utils;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class YahooETFDataDownload implements IETFDataDownloader {

	// public static final String URL =
	// "http://chart.finance.yahoo.com/table.csv?s=%s&amp;a=%d&amp;b=%d&amp;c=%d&amp;d=%d&amp;e=%d&amp;f=%d&amp;g=%s&amp;ignore=.csv";
	// public static final String URL =
	// "http://ichart.yahoo.com/table.csv?s=%s&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d&g=%s&ignore=.csv";

	public static final String URL1 = "https://uk.finance.yahoo.com/quote/%s/history";
	public static final String URL = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=728262000&period2=%d&interval=1d&events=history&crumb=%s";
	public static final String URL_DIV = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=728262000&period2=%d&interval=1d&events=div&crumb=%s";

	private static String cookie = null;
	private static String crumb = null;

	@Override
	public String download(ETFDownloadParams params) throws Exception {

		StringBuilder builder = new StringBuilder();

		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(params.from);

		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(params.to);

		if (cookie == null)
			loadCookie(params);
		if (cookie == null || crumb == null) {
			throw new Exception("Failed to retrieve cookie!");
		}

		String url = String.format(URL, params.ticket, params.to.getTime(), crumb);
		String url_div = String.format(URL_DIV, params.ticket, params.to.getTime(), crumb);

		try {
			System.out.println(url);
			java.net.URL theUrl = new java.net.URL(url);
			URLConnection con = theUrl.openConnection();
			con.setRequestProperty("Cookie", cookie);
			String redirect = con.getHeaderField("Location");
			if (redirect != null) {
				con = new java.net.URL(redirect).openConnection();
			}

			InputStream iss = con.getInputStream();
			if (con instanceof HttpURLConnection) {
				HttpURLConnection httpCon = (HttpURLConnection) con;
				if (httpCon.getResponseCode() != 200) {
					throw new Exception(httpCon.getResponseMessage());
				}
			}

			InputStreamReader is = new InputStreamReader(iss);
			BufferedReader br = new BufferedReader(is);
			br.readLine(); // skip the first line
			// Parse CSV
			float last = 0.0f;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] data = line.split(YahooFinance.QUOTES_CSV_DELIMITER);
				String nav = data[1];
				if (nav != null && !"null".equals(nav)) {
					if (last == 0.0f) {
						last = Float.valueOf(nav);
					}

					float tolerance = 5.0f;

					if (params.ticket.equals("SPY")) {
						tolerance = 7.0f;
					}

					if (Math.abs(last - Float.valueOf(nav).floatValue()) < tolerance) {
						last = Float.valueOf(nav);
						builder.append(data[0] + "," + nav + "\r\n");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Main.EMPTY_DATA;
		}

		return builder.toString();
	}

	@Override
	public String downloadDiv(ETFDownloadParams params) throws Exception {

		StringBuilder builder = new StringBuilder();

		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(params.from);

		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(params.to);

		if (cookie == null)
			loadCookie(params);
		if (cookie == null || crumb == null) {
			throw new Exception("Failed to retrieve cookie!");
		}

		String url = String.format(URL_DIV, params.ticket, params.to.getTime(), crumb);

		try {
			System.out.println(url);
			java.net.URL theUrl = new java.net.URL(url);
			URLConnection con = theUrl.openConnection();
			con.setRequestProperty("Cookie", cookie);
			String redirect = con.getHeaderField("Location");
			if (redirect != null) {
				con = new java.net.URL(redirect).openConnection();
			}

			InputStream iss = con.getInputStream();
			if (con instanceof HttpURLConnection) {
				HttpURLConnection httpCon = (HttpURLConnection) con;
				if (httpCon.getResponseCode() != 200) {
					throw new Exception(httpCon.getResponseMessage());
				}
			}

			InputStreamReader is = new InputStreamReader(iss);
			BufferedReader br = new BufferedReader(is);
			br.readLine(); // skip the first line
			// Parse CSV
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] data = line.split(YahooFinance.QUOTES_CSV_DELIMITER);
				String nav = data[1];
				if (nav != null && !"null".equals(nav)) {
					builder.append(data[0] + "," + nav + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Main.EMPTY_DATA;
		}

		return builder.toString();
	}

	private void loadCookie(ETFDownloadParams params) throws MalformedURLException, IOException, Exception {
		String url1 = String.format(URL1, params.ticket);
		System.out.println(url1);
		java.net.URL theUrl = new java.net.URL(url1);
		URLConnection con = theUrl.openConnection();
		String redirect = con.getHeaderField("Location");
		if (redirect != null) {
			con = new java.net.URL(redirect).openConnection();
		}
		InputStream iss = con.getInputStream();
		if (con instanceof HttpURLConnection) {
			HttpURLConnection httpCon = (HttpURLConnection) con;
			if (httpCon.getResponseCode() != 200) {
				throw new Exception(httpCon.getResponseMessage());
			}
		}

		InputStreamReader is = new InputStreamReader(iss);
		String theString = IOUtils.toString(is);

		List<String> cookies = con.getHeaderFields().get("Set-Cookie");
		for (String string : cookies) {
			if (string.startsWith("B")) {
				cookie = string;
				System.out.println("FOUND: " + cookie);
				break;
			}
		}

		Pattern p = Pattern.compile("\"CrumbStore\":\\{\"crumb\":\"(?<crumb>\\w+)\"\\}");
		Matcher m = p.matcher(theString);
		if (m.find()) {
			crumb = theString.substring(m.start(), m.end());
			String[] crumbSplit = crumb.split(":");
			crumb = crumbSplit[2].replaceAll("\\}", "").replaceAll("\"", "");
			System.out.println("FOUND: " + crumb);
		}

	}

	private Interval ziskajInterval(String freq) {
		if ("m" == freq) {
			return Interval.MONTHLY;
		}

		if ("w" == freq) {
			return Interval.DAILY;
		}

		return Interval.DAILY;
	}

	public static int JGREG = 15 + 31 * (10 + 12 * 1582);
	public static double HALFSECOND = 0.5;

	public static double toJulian(int[] ymd) {
		int year = ymd[0];
		int month = ymd[1]; // jan=1, feb=2,...
		int day = ymd[2];
		int julianYear = year;
		if (year < 0)
			julianYear++;
		int julianMonth = month;
		if (month > 2) {
			julianMonth++;
		} else {
			julianYear--;
			julianMonth += 13;
		}

		double julian = (java.lang.Math.floor(365.25 * julianYear) + java.lang.Math.floor(30.6001 * julianMonth) + day
				+ 1720995.0);
		if (day + 31 * (month + 12 * year) >= JGREG) {
			// change over to Gregorian calendar
			int ja = (int) (0.01 * julianYear);
			julian += 2 - ja + (0.25 * ja);
		}
		return java.lang.Math.floor(julian);
	}

}
