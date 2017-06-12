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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.etf.provider.ConfigProvider;

public class Analyzer {

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
		Map<String, Float> preferenceMap = new HashMap<>();
		Map<String, Integer> holdMap = new HashMap<>();

		expData.forEach(p -> {
			try {
				String content = new String(Files.readAllBytes(p), "UTF-8");
				String preferences = content.substring(content.indexOf("{") + 1, content.indexOf("}"));
				String[] splitPreferenceString = preferences.split(",");
				Arrays.asList(splitPreferenceString).forEach(preferecne -> {
					String[] parts = preferecne.split("=");
					Float percentage = Float.valueOf(parts[1]);
					if (preferenceMap.containsKey(parts[0])) {
						percentage = preferenceMap.get(parts[0])+ percentage;
					}
					preferenceMap.put(parts[0], percentage);
				});
				
				String hold = content.substring(content.lastIndexOf("{")+1, content.lastIndexOf("}"));
				if(StringUtils.isNotEmpty(hold)){
					String[] splitHold = hold.split(",");
					Arrays.asList(splitHold).forEach(held -> {
						String[] parts = held.split("=");
						Integer holdVal = Integer.valueOf(parts[1]);
						if(holdMap.containsKey(parts[0])){
							holdVal= holdMap.get(parts[0])+holdVal;
						}
						holdMap.put(parts[0], holdVal);
					});
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		preferenceMap.keySet().forEach(key->{
			Float percentrage = preferenceMap.get(key);
			preferenceMap.put(key, (float)(percentrage/expData.size()));
		});
		
		Map<String,Float> sorted = preferenceMap.entrySet().stream()
				.sorted(Map.Entry
						.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		sorted.keySet().forEach(key -> {
			String row = key + ";" + preferenceMap.get(key)+";"+(holdMap.get(key) == null ? 0 : holdMap.get(key))+"\n";
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
