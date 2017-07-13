package org.eft.evol.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.etf.provider.ConfigProvider;

import cern.jet.math.Arithmetic;
import cern.jet.random.Uniform;

public class SimpleUnit {

	private static final int MAX_SIZE = 10;
	private static final float TRANSACTION_COST = 2.0f;

	public byte buyProb = (byte) Uniform.staticNextIntFromTo(0, 100);
	public List<Integer> etfs = new ArrayList<Integer>(MAX_SIZE);
	public Map<Integer, Integer> etfShares = new Hashtable<Integer, Integer>(MAX_SIZE);

	public float cash = 300.0f;
	private int etfSize;

	public SimpleUnit(int etfSize) {
		this.etfSize = etfSize;
		initEtfsProb(etfSize);
		initShares();
	}

	private void initEtfsProb(int etfSize) {
		for (int i = 0; i < 10; i++) {
			this.etfs.add(Uniform.staticNextIntFromTo(0, etfSize-1));
		}
	}

	public void initShares() {
		for (Integer key : etfs) {
			etfShares.put(key, 0);
		}
	}

	public SimpleUnit(int etfSize, byte buyProb, List<Integer> etfs) {
		this.etfSize = etfSize;
		this.buyProb = buyProb;
		this.etfs = new ArrayList<Integer>(etfs);
	}

	public void doAction(int cycle, float[][] etfValueMap) {
		if (Uniform.staticNextIntFromTo(0, 100) >= buyProb) {
			buy(cycle, etfValueMap);
		}
	}

	public boolean buy(int cycle, float[][] etfValueMap) {

		int index = 0;

		int chosenIndex = Uniform.staticNextIntFromTo(0, etfs.size() - 1);
		index = etfs.get(chosenIndex);

		float nav = etfValueMap[cycle][index];
		if (nav == 0.0f) {
			return false;
		}

		return doBuyAction(cycle, etfValueMap, index, nav);
	}

	private boolean doBuyAction(int cycle, float[][] etfValueMap, int index, float nav) {
		if (cash < nav) {
			return false;
		}

		int shares = (int) Arithmetic.floor((cash - TRANSACTION_COST) / nav);

		while (((float) shares) * nav + TRANSACTION_COST > cash) {
			shares--;
		}
		if (shares <= 0) {
			return false;
		}

		float bought = ((float) shares) * nav;

		cash = cash - bought - TRANSACTION_COST;

		etfShares.put(index, etfShares.get(index) + shares);

		if (shares > 0l)
			return true;

		return false;
	}

	public SimpleUnit crossOver(SimpleUnit other) {

		byte nBuyProb = crossover(this.buyProb, other.buyProb);

		List<Integer> nPrefs = crossoverPreference(other);

		return new SimpleUnit(etfSize,nBuyProb, nPrefs);
	}

	public void mutate() {
		
		this.buyProb = mutateByte(buyProb);
		
		for (int i = 0; i < MAX_SIZE; i++) {
			this.etfs.add(i,mutateInteger(etfs.get(i)));
		}
	}

	private Integer mutateInteger(Integer b) {
		String num1 = Integer.toBinaryString(b);

		char[] c1 = num1.toCharArray();

		if(c1.length == 1){
			if(c1[0] == '0')
				c1[0] = '1';
			else
				c1[0] = '0';
		}else{
			int randomIndex=  Uniform.staticNextIntFromTo(0, c1.length-1);
			if(c1[randomIndex] == '0')
				c1[randomIndex] = '1';
			else
				c1[randomIndex] = '0';
			
		}

		int result = Math.abs(Integer.parseInt(String.valueOf(c1), 2));
		if(result > etfSize-1){
			result = etfSize - 1;
		}
		
		return result;
	}

	
	private byte mutateByte(byte b) {
		BitSet bitSet1 = BitSet.valueOf(new byte[] { b });
		if (bitSet1.isEmpty()) {
			bitSet1.set(Uniform.staticNextIntFromTo(0, 7));
		} else {
			bitSet1.flip(Uniform.staticNextIntFromTo(0, 7));
		}

		byte result = 0;
		if (!bitSet1.isEmpty()) {
			result = bitSet1.toByteArray()[0];
		}
		return result > 99 ? 99 : result < 0 ? 0 : result;
	}

	private List<Integer> crossoverPreference(SimpleUnit other) {
		List<Integer> nPref = new ArrayList<Integer>(MAX_SIZE);

		for(int i = 0;i < MAX_SIZE;i++){
			nPref.add(crossover(this.etfs.get(i), other.etfs.get(i)));
		}
		
		return nPref;
	}

	private Integer crossover(Integer i1, Integer i2) {

		String num1 = Integer.toBinaryString(i1);
		String num2 = Integer.toBinaryString(i2);

		char[] c1 = num1.toCharArray();
		char[] c2 = num2.toCharArray();

		if (c2.length > c1.length) {
			c1 = adjust(c1, c2.length);
		} else if (c1.length > c2.length) {
			c2 = adjust(c2, c1.length);
		}

		char[] cc = new char[c2.length];

		int half = c2.length / 2;
		for (int i = 0; i < c2.length; i++) {
			if (i < half) {
				cc[i] = c1[i];
			} else {
				cc[i] = c2[i];
			}
		}

		int result = Math.abs(Integer.parseInt(String.valueOf(cc), 2));
		if(result > etfSize-1){
			result = etfSize - 1;
		}
		
		return result;
	}

	private char[] adjust(char[] c1, int length) {
		char[] cc = new char[length];

		for (int i = cc.length - 1, j = 0; i >= 0; i--, j++) {
			if (j < c1.length) {
				cc[i] = c1[j];
			} else {
				cc[i] = '0';
			}
		}

		return cc;
	}

	private byte crossover(byte b1, byte b2) {
		BitSet bitSet1 = BitSet.valueOf(new byte[] { b1 });
		BitSet bitSet2 = BitSet.valueOf(new byte[] { b2 });

		BitSet mask1 = new BitSet(Byte.SIZE);
		for (int i = 0; i < Byte.SIZE; i++) {
			mask1.set(i, Uniform.staticNextBoolean());
		}

		BitSet mask2 = (BitSet) mask1.clone();
		if (mask2.isEmpty()) {
			for (int i = 0; i < 7; i++) {
				mask2.set(i);
			}
		} else {
			mask2.flip(0, mask2.length());
		}

		BitSet child1A = (BitSet) bitSet1.clone();
		child1A.and(mask1);

		BitSet child1B = (BitSet) bitSet2.clone();
		child1B.and(mask2);

		child1A.or(child1B);

		byte result = 0;
		if (!child1A.isEmpty()) {
			result = child1A.toByteArray()[0];
		}
		return result > 99 ? 99 : result < 0 ? 0 : result;
	}

	
	public void calculateDividends(float[][] dividends, int cycle) {
		for(Integer etf_index : etfShares.keySet()){
			if (etfShares.get(etf_index) > 0 && dividends[cycle][etf_index] > 0.0f) {
				float dividends_payed = (float) etfShares.get(etf_index) * dividends[cycle][etf_index];
				this.cash += dividends_payed;
			}
			
		}
	}
	
	public float netAssetValue(int day, float[][] etfValueMap) {
		float sum = cash;
		for (Integer etf_index : etfShares.keySet()) {
			if (etfShares.get(etf_index) == 0) {
				continue;
			}
			float nav = getNavValueByIndex(day, etfValueMap, etf_index);
			if (nav == 0.0f) {
				continue;
			}
			sum += ((float) etfShares.get(etf_index)) * nav;
		}

		return sum;
	}
	
	private Float getNavValueByIndex(int day, float[][] etf_values, int etf_index) {
		if (etf_values[day][etf_index] > 0.0f) {
			return etf_values[day][etf_index];
		}

		while (day > 0) {
			if (etf_values[day][etf_index] > 0.0f) {
				return etf_values[day][etf_index];
			}
			day--;
		}

		return 0.0f;
	}
	
	private String getHistoryLogFileName(String directory) {
		StringBuilder builder = new StringBuilder();

		builder.append(directory);
		builder.append('/');
		builder.append(UnitSequenceGenerator.getID());
		builder.append("_history.log");

		return builder.toString();
	}

	public String getDir(int iteration) {
		StringBuilder builder = new StringBuilder();
		builder.append(iteration);
		builder.append("_SimpleUnit");
		return builder.toString();
	}
	
	private String getDirectoryName(int iteration,int cycle, float nav) {
		StringBuilder builder = new StringBuilder();

		builder.append(ConfigProvider.DIR);
		builder.append('/');
		builder.append(getDir(iteration));
		builder.append('/');
		builder.append(nav);

		return builder.toString();
	}
	
	
	public synchronized void logToFile(int iteration, int cycle, float[][] navValues) {
		
		float nav = netAssetValue(cycle, navValues);
		
		String directory = getDirectoryName(iteration,cycle,nav);
		File dir = new File(directory);
		File historyLogFile = new File(getHistoryLogFileName(directory));

		if (historyLogFile.exists()) {
			return;
		}

		if (!Files.exists(dir.toPath())) {
			try {
				Files.createDirectories(dir.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		if (!Files.exists(historyLogFile.toPath())) {
			try {
				Files.createFile(historyLogFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		try {
			StringBuilder builder = new StringBuilder();

			builder.append(iteration);
			builder.append(',');
			builder.append(cycle);
			builder.append(',');
			builder.append(buyProb);
			builder.append(",{");
			for (Integer etf_index : etfs) {
				builder.append(ETFMap.getInstance().getEtfName(etf_index));
				builder.append('=');
				builder.append("99");
				builder.append(',');
			}
			builder.append("},{");
			for (Integer etf_index : etfs) {
				builder.append(ETFMap.getInstance().getEtfName(etf_index));
				builder.append('=');
				builder.append(etfShares.get(etf_index));
				builder.append(',');
			}
			builder.append("},");
			builder.append(nav);
			builder.append('\n');

			Files.write(historyLogFile.toPath(), builder.toString().getBytes(), StandardOpenOption.APPEND);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
