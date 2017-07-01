package com.etfdatadownload.main;

public class ETFDataDownloaderFactory {

	public static IETFDataDownloader getYahooDownloader() {

		return new YahooETFDataDownload();
	}

}
