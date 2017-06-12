package com.etfdatadownload.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.etfdatadownload.main.resources.AppProperties;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class YahooFinanceTest {

	public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException {
		Path dir = Paths.get(AppProperties.getDirName());

		System.out.println(dir);

		if (!Files.exists(dir, LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectories(dir);
		}

		if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException(dir.toString() + " is not a directory!");
		}

		//TickerNasdaqNamesDownloader.downloadTickers();

		String tickers = "etf_tickers.csv";
		String tickerData = null;

		Path tickerFile = Paths.get(dir.toString(), tickers);
		tickerData = TickerYahooNamesDownloader.loadAllSymbolNamesFromYahoo();
		Files.write(tickerFile, tickerData.getBytes("UTF-8"));

		//String[] tickets = tickerData.split("\r\n");

//		List<String> etns = new ArrayList<>(); 
//		List<String> etfs = new ArrayList<>(); 
//		List<String> unknown = new ArrayList<>(); 
//		
//		for (String ticket : tickets) {
//			try {
//				Stock stock = YahooFinance.get(ticket);
//				if(stock.getName().contains("ETN")){
//					etns.add(stock.getName());
//				}else if(stock.getName().contains("ETF")){
//					etfs.add(stock.getName());
//				}else{
//					unknown.add(stock.getName());
//				}
//			} catch (Exception e) {
//				// nothing
//			}
//		}
//
//		System.out.println("ETNs:");
//		etns.stream().forEach(x -> System.out.println(x));
//		System.out.println("ETFs:");
//		etfs.stream().forEach(x -> System.out.println(x));
//		System.out.println("Uknowns:");
//		unknown.stream().forEach(x -> System.out.println(x));
		
	}

}
