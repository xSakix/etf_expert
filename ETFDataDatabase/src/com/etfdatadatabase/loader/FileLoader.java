package com.etfdatadatabase.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.resources.AppProperties;

public class FileLoader {
	
	public static List<ETF> loadAllUSD(int num) throws IOException{
		List<ETF> etfs = new ArrayList<>();
		Path directory = Paths.get(AppProperties.getDirName()+"");
		if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)
				&& !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException(directory.toString() + " not found");
		}
		
		List<File> files = Files.list(directory).filter(p -> p.toString().endsWith(".csv")).map(p -> p.toFile()).collect(Collectors.toList());
		
		
		Collections.sort(files, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return Long.compare(o2.length(), o1.length());
			}
		});

		if(num > 0){
			files = files.subList(0, num);
		}
		
		files.forEach(p->{
			try {
				String ticket = p.getName().toString().replace(".csv", "");
				//System.out.println("loading ticket:"+ticket);
				ETF etfData = loadETFData(ticket);
				if(etfData != null && etfData.getNavDataList() !=null && !etfData.getNavDataList().isEmpty())
					etfs.add(etfData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		return etfs;
	}
	

	public static ETF loadETFData(String ticket) throws IOException {
		ETF etf = new ETF(ticket);

		Path directory = Paths.get(AppProperties.getDirName());
		if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)
				&& !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException(directory.toString() + " not found");
		}

		Path file = Paths.get(directory.toString(), ticket + ".csv");

		File inputF = file.toFile();
		InputStream inputFS = new FileInputStream(inputF);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
		
		System.out.println("Loading ticket:"+ticket);
		
		
		//zoberiem natvrdo len rok dozadu
		Calendar calFrom = Calendar.getInstance();
		calFrom.set(Calendar.YEAR, 2000);
		calFrom.set(Calendar.MONTH, 0);
		calFrom.set(Calendar.DAY_OF_MONTH,1);

		/*Calendar calTo = Calendar.getInstance();
		calTo.set(Calendar.YEAR, 2016);
		calTo.set(Calendar.MONTH, 11);
		calTo.set(Calendar.DAY_OF_MONTH,31);*/
		
		br.lines().forEach(line -> {
			
			String[] parts = line.split(",");
			if(parts[1] != null && !"null".equals(parts[1])){
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate dateTime = LocalDate.parse(parts[0], formatter);
				Date datePart = Date.from(dateTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
				if(datePart.after(calFrom.getTime()) /*&& datePart.before(calTo.getTime())*/){
					etf.addNavData(Float.valueOf(parts[1]), datePart);
				}
			}
		});
		
		return etf;
	}
	
	public static void main(String[] args) throws IOException {
		
		List<ETF> etfs = loadAllUSD(0);
		System.out.println(etfs.size());
	}
}
