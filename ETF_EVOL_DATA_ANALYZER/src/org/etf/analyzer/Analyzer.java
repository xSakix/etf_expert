package org.etf.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.etf.provider.ConfigProvider;

public class Analyzer {

	private static Pattern pattern = Pattern.compile("\\{(.*?)\\}");

	
	public static void main(String[] args) throws IOException {
		String dir = ConfigProvider.MAIN_DIR;
		List<Path> listing = Files.list(new File(dir).toPath()).filter(Files::isDirectory).collect(Collectors.toList());

		if (listing.isEmpty()) {
			return;
		}

		listing.forEach(experimentDir -> {
			try {
				analyzeOne(experimentDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});		
	}

	private static void analyzeOne(Path experimentDir) throws IOException {
		List<Path> listing;
		File analyzedData = new File(experimentDir.toAbsolutePath() + "\\analyzed.csv");
		if (analyzedData.exists()) {
			analyzedData.delete();
		}
		analyzedData.createNewFile();


		listing = Files.list(experimentDir).filter(Files::isDirectory).collect(Collectors.toList());

		if (listing.isEmpty()) {
			return;
		}

		List<Path> sols = new ArrayList<>();

		listing.forEach(p -> {

			try {
				sols.addAll(Files.list(p).collect(Collectors.toList()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		Collections.sort(sols, new Comparator<Path>() {

			@Override
			public int compare(Path o1, Path o2) {
				String p1 = o1.getFileName().toString();
				String p2 = o2.getFileName().toString();
				
				return Float.valueOf(p2).compareTo(Float.valueOf(p1));
			}
		});
		
		List<Path> expData = new ArrayList<>();

		sols.forEach(p -> {
			try {
				expData.addAll(Files.list(p).filter(f -> f.toAbsolutePath().toString().endsWith("history.log"))
						.collect(Collectors.toList()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		analyzeListOfLogFiles(analyzedData, expData);
		
		Path winner_log = expData.get(0);
		System.out.println("global winner:"+winner_log.toString());
		
		analyzedData = new File(experimentDir.toAbsolutePath() + "\\analyzed_winner.csv");
		if (analyzedData.exists()) {
			//return;
			analyzedData.delete();
		}
		analyzedData.createNewFile();
		expData.clear();
		expData.add(winner_log);
		analyzeListOfLogFiles(analyzedData, expData);
	}

	private static void analyzeListOfLogFiles(File analyzedData, List<Path> expData) {
		Map<String, Integer> holdMap = new HashMap<>();
		Map<Integer,Map<String, Float>> holder = new HashMap<>();
		Map<Integer,Map<String, AtomicInteger>> counterHolder = new HashMap<>();
		
		holder.put(0, new HashMap<String, Float>());
		holder.put(1, new HashMap<String, Float>());
		holder.put(2, new HashMap<String, Float>());

		counterHolder.put(0, new HashMap<String,AtomicInteger>());
		counterHolder.put(1, new HashMap<String,AtomicInteger>());
		counterHolder.put(2, new HashMap<String,AtomicInteger>());
		
		expData.forEach(p -> {
			try {
				String content = new String(Files.readAllBytes(p), "UTF-8");
				
				Matcher m = pattern.matcher(content);
				
				int i = 0;
				while(m.find()){
					String preferences = content.substring(m.start(),m.end()).replace("{", "").replace("}", "");
					Map<String,Float> preferenceMap = holder.get(i);
					Map<String,AtomicInteger> counterMap = counterHolder.get(i);
					if(i > 2){
						break;
					}
					
					i++;
					
					String[] splitPreferenceString = preferences.split(",");
					if(StringUtils.isEmpty(preferences)) continue;
					
					Arrays.asList(splitPreferenceString).forEach(preferecne -> {
						
						String[] parts = preferecne.split("=");
						
						Float percentage = Float.valueOf(parts[1]);
						String key = parts[0];
						if (preferenceMap.containsKey(key)) {
							percentage = preferenceMap.get(key)+ percentage;
						}
						preferenceMap.put(key, percentage);
						
						if(counterMap.containsKey(key)){
							counterMap.get(key).incrementAndGet();
						}else{
							counterMap.put(key, new AtomicInteger(1));
						}
						
					});
					
						
				}
				
				
				String hold = content.substring(content.lastIndexOf("{")+1, content.lastIndexOf("}"));
				if(StringUtils.isNotEmpty(hold)){
					String[] splitHold = hold.split(",");
					Arrays.asList(splitHold).forEach(held -> {
						String[] parts = held.split("=");
						String value = parts[1];
						Integer holdVal = Integer.valueOf(value);
						String key = parts[0];
						if(holdMap.containsKey(key)){
							holdVal= holdMap.get(key)+holdVal;
						}
						holdMap.put(key, holdVal);
					});
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		Set<String> keySet = new HashSet<String>();
		for(int i = 0;i < 3;i++){
			final Integer index = i;
			holder.get(index).keySet().forEach(key->{
				Float percentrage = holder.get(index).get(key);
				holder.get(index).put(key, (float)(percentrage/(float)counterHolder.get(index).get(key).get()));
			});
			keySet.addAll(holder.get(index).keySet());		

		}

		keySet.forEach(key -> {
			String row = key + 
						 ";" + 
						 holder.get(0).get(key)+
						 ";"+
						 holder.get(1).get(key)+
						 ";"+
						 holder.get(2).get(key)+
						 ";"+
						 (holdMap.get(key) == null ? 0 : holdMap.get(key))+
						 ";"+
						 "http://www.etf.com/"+key+"?nopaging=1"+
						 "\n";
			try {
				Files.write(analyzedData.toPath(), row.getBytes("UTF-8"),StandardOpenOption.APPEND);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
