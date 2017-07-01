package com.etfdatadownload.main;

import java.io.IOException;
import java.net.MalformedURLException;

public interface IETFDataDownloader {

	public String download(ETFDownloadParams params) throws MalformedURLException, IOException, Exception;

	String downloadDiv(ETFDownloadParams params) throws Exception;
}
