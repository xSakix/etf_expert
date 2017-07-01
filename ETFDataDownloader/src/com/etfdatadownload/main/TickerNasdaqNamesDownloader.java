package com.etfdatadownload.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

import com.etfdatadownload.main.resources.AppProperties;

public class TickerNasdaqNamesDownloader {

	static String URL = "http://www.nasdaq.com/investing/etfs/etf-finder-results.aspx?download=Yes";

	public static void downloadTickers() throws MalformedURLException, IOException {

		String data = IOUtils.toString(new java.net.URL(URL), "UTF-8");

		Path dir = Paths.get(AppProperties.getDirName());

		Path dataFile = Paths.get(dir.toString(), "symbols.csv");
		if (Files.exists(dataFile))
			return;

		Files.write(dataFile, data.getBytes("UTF-8"));
	}

}
