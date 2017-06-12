package org.etf.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;

public class ConfigProvider {
	public static final String MAIN_DIR = "C:\\DATA\\etf_evolution\\";
	public static final String DIR;
	
	static{
		Calendar cal = Calendar.getInstance();
		String sub = ""+
					cal.get(Calendar.YEAR)+
					(cal.get(Calendar.MONTH)+1)+
					cal.get(Calendar.DAY_OF_MONTH)+
					cal.get(Calendar.HOUR)+
					cal.get(Calendar.MINUTE)+
					cal.get(Calendar.SECOND);
		DIR=MAIN_DIR+sub+"\\";
		try {
			Files.createDirectories(new File(DIR).toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
