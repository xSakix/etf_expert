package com.etfdatadatabase.helper;

import java.io.IOException;
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

	for(ETF etf : loadedETFS){
	    Calendar cal = Calendar.getInstance();
	    List<NavData> navDataList = etf.getNavDataList();
	    int size = navDataList.size();
	    NavData last = navDataList.get(size-1);
	    cal.setTime(last.getDate());
	    
	    //2017-06-29
	    int day = cal.get(Calendar.DAY_OF_MONTH);
	    int month = cal.get(Calendar.MONTH)+1;
	    int year = cal.get(Calendar.YEAR);
	    
	    if(day < 29 && month != 6 && year != 2017){
		System.out.println(etf.getTicket());
	    }
	}
	
    }

}
