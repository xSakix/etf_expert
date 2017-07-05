package org.etf.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigProvider {
	public static final String MAIN_DIR = "C:\\DATA\\etf_evolution\\";
	public static final String DIR;
	public static final String DATE_STR;

	static {
		// Calendar cal = Calendar.getInstance();

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		DATE_STR = df.format(new Date());
		DIR = MAIN_DIR + DATE_STR + "\\";
		try {
			Files.createDirectories(new File(DIR).toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
