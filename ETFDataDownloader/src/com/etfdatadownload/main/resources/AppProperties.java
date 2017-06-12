package com.etfdatadownload.main.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
	
	private static final Properties props;
	
	static{
		
		props = new Properties();
		try(InputStream is = AppProperties.class.getResourceAsStream("application.properties")){
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static String getDirName(){
		return AppProperties.props.getProperty("dir_name");
	}
}
