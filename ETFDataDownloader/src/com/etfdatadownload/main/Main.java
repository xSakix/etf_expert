package com.etfdatadownload.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.etfdatadownload.main.resources.AppProperties;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

public class Main {
	
	public static final String EMPTY_DATA = "not found";

	public static void main(String[] args) throws IOException, InterruptedException {		
		Path dir = Paths.get(AppProperties.getDirName());
		
		System.out.println(dir);
		
		if(!Files.exists(dir, LinkOption.NOFOLLOW_LINKS)){
			Files.createDirectories(dir);
		}
		
		if(!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)){
			throw new IOException(dir.toString()+" is not a directory!");
		}

		//TickerNasdaqNamesDownloader.downloadTickers();

		String tickers = "etf_tickers.csv"; 
		String tickerData = null;

		Path tickerFile = Paths.get(dir.toString(), tickers);
		if(Files.exists(tickerFile)){
			tickerData = new String(Files.readAllBytes(tickerFile),"UTF-8");
		}else{		
			tickerData = TickerYahooNamesDownloader.loadAllSymbolNamesFromYahoo();			
			Files.write(tickerFile, tickerData.getBytes("UTF-8"));
		}
		
		String[] tickets = tickerData.split("\r\n");
		
		for (String ticket : tickets) {			
			String currencyDirectory = "unknown";
			Stock stock = null;
			try{
				stock = YahooFinance.get(ticket);
				currencyDirectory = stock.getCurrency();
			}catch(Exception e){
				//nothing
			}
			if(currencyDirectory == null){
				currencyDirectory = "unknown";
			}
			

			
			Path dataFile = Paths.get(dir.toString()+"\\"+currencyDirectory, ticket+".csv");
			
			Files.createDirectories(Paths.get(dir.toString()+"\\"+currencyDirectory));
			
			if(Files.exists(dataFile)){
				//Files.delete(dataFile);
				continue;
			}
			
			
			
			IETFDataDownloader downloader = ETFDataDownloaderFactory.getYahooDownloader();
			ETFDownloadParams params = ETFDownLoadBuilder.start().stock(stock).ETFTicket(ticket).frequency("d").build();
			String data = EMPTY_DATA;
			try {
				data = downloader.download(params);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(data == EMPTY_DATA){
				System.out.println("Not writing file:"+dataFile.toString());
				continue;
			}
			
			try{
				Files.write(dataFile, data.getBytes("UTF-8"),StandardOpenOption.CREATE_NEW);
				System.out.println("Written file:"+dataFile.toString());
			}catch(Exception e){
				System.out.println("Writing file failed: "+dataFile.toString());
				e.printStackTrace();			
			}
			Thread.sleep(5000);
		}
		
	}
	
	

}
