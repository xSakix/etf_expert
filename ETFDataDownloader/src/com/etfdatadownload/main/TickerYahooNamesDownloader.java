package com.etfdatadownload.main;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TickerYahooNamesDownloader {

	// private static final String URL =
	// "http://finance.yahoo.com/lookup/etfs?s=1&t=E&m=ALL&b=%d";

	private static final String URL = "https://finance.yahoo.com/lookup/etfs?s=etf&t=E&m=ALL&b=%d";

	public static String loadAllSymbolNamesFromYahoo() throws MalformedURLException, IOException, InterruptedException {

		StringBuilder builder = new StringBuilder();

		// System.out.println(IOUtils.toString(new
		// java.net.URL(String.format(URL, 0)),"UTF-8"));

		// pojdem stale o 20
		int index = 0;
		int inc = 20;
		while (true) {
			Document page = null;
			try {
				page = Jsoup.connect(String.format(URL, index)).get();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			// System.out.println(page.html());
			// a href="/quote/
			Elements elements = page.select("span[class=W(16%) D(b) Fl(start)] a[href*=/quote/");

			if (elements.size() == 0) {
				break;
			}

			for (Element element : elements) {
				System.out.println(element.html());
				builder.append(element.html() + "\r\n");
			}
			index += inc;
			Thread.sleep(10000);
		}

		return builder.toString();
	}

}
