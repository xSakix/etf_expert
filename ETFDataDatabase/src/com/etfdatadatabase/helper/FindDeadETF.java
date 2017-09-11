package com.etfdatadatabase.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.List;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

public class FindDeadETF
{

    public static void main(String[] args) throws IOException
    {
	List<ETF> loadedETFS = FileLoader.loadAllUSD(0);

	String dirName = "c:\\downloaded_data\\USD";

	String tickers = "etf_tickers.csv";
	String tickerData = null;

	Path tickerFile = Paths.get("c:\\downloaded_data\\", tickers);
	if (Files.exists(tickerFile))
	{
	    tickerData = new String(Files.readAllBytes(tickerFile), "UTF-8");
	}

	for (ETF etf : loadedETFS)
	{
	    Calendar cal = Calendar.getInstance();
	    List<NavData> navDataList = etf.getNavDataList();
	    int size = navDataList.size();
	    NavData last = navDataList.get(size - 1);
	    cal.setTime(last.getDate());

	    // 2017-06-29
	    int day = cal.get(Calendar.DAY_OF_MONTH);
	    int month = cal.get(Calendar.MONTH) + 1;
	    int year = cal.get(Calendar.YEAR);

	    if (day < 29 && month != 6 && year != 2017)
	    {
		tickerData = removeDeadEtf(dirName, tickerData, etf);
	    }

	}

	Files.write(tickerFile, tickerData.getBytes("UTF-8"),
		StandardOpenOption.WRITE);

    }

    private static String removeDeadEtf(String dirName, String tickerData,
	    ETF etf) throws IOException
    {
	System.out.println(etf.getTicket());
	if (tickerData.contains(etf.getTicket() + "\r\n"))
	{
	    System.out.println("Removing:" + etf.getTicket());
	    tickerData = tickerData.replace(etf.getTicket() + "\r\n", "");

	}

	Path dataFile = Paths.get(dirName, etf.getTicket() + ".csv");
	if (Files.exists(dataFile))
	{
	    System.out.println(
		    String.format("Deleting: %s\n", dataFile.toString()));
	    Files.delete(dataFile);
	}
	
	dataFile = Paths.get(dirName+"_div", etf.getTicket() + ".csv");
	if (Files.exists(dataFile))
	{
	    System.out.println(
		    String.format("Deleting: %s\n", dataFile.toString()));
	    Files.delete(dataFile);
	}

	return tickerData;
    }

}
