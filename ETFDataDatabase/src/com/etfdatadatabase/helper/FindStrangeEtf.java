package com.etfdatadatabase.helper;

import java.io.IOException;
import java.util.List;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

public class FindStrangeEtf
{

    public static void main(String[] args) throws IOException
    {
	List<ETF> loadedETFS = FileLoader.loadAllUSD(0);
	
	for(ETF etf : loadedETFS){
	    checkETF(etf);
	}

    }

    private static void checkETF(ETF etf)
    {
	float tolerance = 15.0f;
	NavData last = null;
	for(NavData navData : etf.getNavDataList()){
	    if(last != null && Math.abs(last.getNav() - navData.getNav()) > tolerance){
		System.err.println(etf.getTicket());
		return;
	    }
	    
	    last = navData;

	    /*if(navData.getNav() < 5.0f){
		System.err.println(etf.getTicket());
		return;		
	    }*/
	}
    }

}
