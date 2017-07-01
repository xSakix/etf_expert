package org.eft.evol.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eft.evol.stats.UnitAction;
import org.eft.evol.stats.UnitState;
import org.etf.provider.ConfigProvider;

import cern.jet.random.Uniform;

public abstract class AbstractUnit implements Unit {

	protected static final float TRANSACTION_COST = 4.0f;
	protected static final byte MODIFIER = 1;

	byte[] character;
	byte[] preference;
	int ID = 0;
	float cash = BASE_CASH;
	float investment = cash;
	// protected float sumGradient = 0.0f;
	int[] etfs;

	List<UnitAction> actions = new LinkedList<>();

	// List<UnitState> stateList = new ArrayList<>();
	UnitState currentState;

	public AbstractUnit(int ETF_SIZE, int index) {
		this.character = new byte[3];
		this.preference = new byte[ETF_SIZE];
		this.etfs = new int[ETF_SIZE];
		this.ID = index;
		init();
		initState();
	}

	public AbstractUnit(int index, byte[] character, byte[] buyPreference) {
		this.character = Arrays.copyOf(character, character.length);
		this.preference = Arrays.copyOf(buyPreference, buyPreference.length);
		this.ID = index;
		this.etfs = new int[buyPreference.length];
		Arrays.fill(etfs, 0);
		initState();
	}

	private void init() {
		for (int i = 0; i < 3; i++) {
			character[i] = (byte) Uniform.staticNextIntFromTo(0, 100);
		}

		for (int i = 0; i < preference.length; i++) {
			preference[i] = 50;
		}

		Arrays.fill(etfs, 0);
	}

	private void initState() {
		this.currentState = new UnitState();
		currentState.nav = this.cash;
		currentState.iteration = 0;
		currentState.cycle = 0;
		setStateFromArrays();
	}

	public void appendHistory(int it, int cycle, float[][] etfValueMap) {

		if (this.currentState == null) {
			this.currentState = new UnitState();
		}

		currentState.nav = this.netAssetValue(cycle, etfValueMap);
		currentState.iteration = it;
		currentState.cycle = cycle;
		setStateFromArrays();

		// this.stateList.add(currentState);
	}

	private void setStateFromArrays() {
		currentState.character = Arrays.copyOf(character, character.length);
		currentState.buyPreference = Arrays.copyOf(preference, preference.length);
		if (etfs != null) {
			currentState.etfs = Arrays.copyOf(this.etfs, this.etfs.length);
		}
	}

	protected void addGradientToAction(int cycle, float[][] etfValueMap, UnitAction sellAction) {
		if (this.currentState == null) {
			sellAction.gradient = 0.0f;
		} else {
			sellAction.gradient = netAssetValue(cycle, etfValueMap) - this.currentState.nav;
		}
	}

	@Override
	public void hold(int iteration, int cycle, float[][] etfValueMap) {

		UnitAction holdAction = new UnitAction();

		holdAction.actionType = ActionType.HOLD;
		holdAction.cycle = cycle;
		holdAction.iteration = iteration;
		addGradientToAction(cycle, etfValueMap, holdAction);

		actions.add(holdAction);

	}

	@Override
	public byte buyProb() {
		return character[INDEX_BUY];
	}

	@Override
	public byte sellProb() {
		return character[INDEX_SELL];
	}

	@Override
	public byte holdProb() {
		return character[INDEX_HOLD];
	}

	@Override
	public float netAssetValue(int day, float[][] etfValueMap) {
		float sum = cash;
		if (this.etfs != null) {
			for (int etf_index = 0; etf_index < etfs.length; etf_index++) {
				if (etfs[etf_index] == 0) {
					continue;
				}
				float nav = getNavValueByIndex(day, etfValueMap, etf_index);
				if (nav == 0.0f) {
					continue;
				}
				sum += ((float) etfs[etf_index]) * nav;
			}
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

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("[[");
		builder.append(ID);
		builder.append("],");
		builder.append(Arrays.toString(character));
		builder.append(',');
		for (int etf_index = 0; etf_index < preference.length; etf_index++) {
			builder.append(ETFMap.getInstance(preference.length).getEtfName(etf_index));
			builder.append('=');
			builder.append(preference[etf_index]);
			builder.append(',');
		}
		builder.append(",[");
		builder.append(cash);
		builder.append("],[");
		for (int etf_index = 0; etf_index < preference.length; etf_index++) {
			if (etfs[etf_index] == 0) {
				continue;
			}
			builder.append(ETFMap.getInstance().getEtfName(etf_index));
			builder.append('=');
			builder.append(etfs[etf_index]);
			builder.append(',');
		}
		builder.append(']');

		return builder.toString();
	}

	@Override
	public void resetAssets(int iteration) {
		this.cash = BASE_CASH;
		this.investment = this.cash;
		int size = this.etfs.length;
		this.etfs = new int[size];
		Arrays.fill(etfs, 0);
		// this.sumGradient = 0.0f;
		initState();
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public abstract String getDir(int iteration);

	@Override
	public synchronized void logToFile(int iteration) {
		String directory = getDirectoryName(iteration);
		File dir = new File(directory);
		File actionLogFile = new File(getActionLogFileName(directory));
		File historyLogFile = new File(getHistoryLogFileName(directory));
		File gradientLogFile = new File(getGradientLogFIleName(directory));

		if (actionLogFile.exists() || historyLogFile.exists() || gradientLogFile.exists()) {
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

		if (!Files.exists(actionLogFile.toPath())) {
			try {
				Files.createFile(actionLogFile.toPath());
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

			for (UnitAction action : actions) {
				Files.write(actionLogFile.toPath(), action.toString().getBytes(), StandardOpenOption.APPEND);
			}

			Files.write(historyLogFile.toPath(), currentStateString().getBytes(), StandardOpenOption.APPEND);

			StringBuilder builder = new StringBuilder();
			builder.append("Total investment:");
			builder.append(investment);
			builder.append('\n');

			Files.write(historyLogFile.toPath(), builder.toString().getBytes(), StandardOpenOption.APPEND);

			// builder = new StringBuilder();
			// builder.append("Total gradient:");
			// builder.append(sumGradient);
			// builder.append('\n');

			// Files.write(historyLogFile.toPath(),
			// builder.toString().getBytes(),StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getGradientLogFIleName(String directory) {
		StringBuilder builder = new StringBuilder();

		builder.append(directory);
		builder.append('/');
		builder.append(this.ID);
		builder.append("_gradient.log");

		return builder.toString();
	}

	private String getHistoryLogFileName(String directory) {
		StringBuilder builder = new StringBuilder();

		builder.append(directory);
		builder.append('/');
		builder.append(this.ID);
		builder.append("_history.log");

		return builder.toString();
	}

	private String getActionLogFileName(String directory) {
		StringBuilder builder = new StringBuilder();

		builder.append(directory);
		builder.append('/');
		builder.append(this.ID);
		builder.append("_action.log");

		return builder.toString();
	}

	private String getDirectoryName(int iteration) {
		StringBuilder builder = new StringBuilder();

		builder.append(ConfigProvider.DIR);
		builder.append('/');
		builder.append(getDir(iteration));
		builder.append('/');
		builder.append(this.currentState.nav);

		return builder.toString();
	}

	protected String currentStateString() {
		return currentState.toString();
	}

	@Override
	public void resetLogs() {
		// this.actions.clear();
	}

	@Override
	public void addCash(float cash) {
		this.cash += cash;
		this.investment += cash;
	}

	@Override
	public byte[] getCharacter() {
		return this.character;
	}

	@Override
	public byte[] getBuyPreferenceMap() {
		return this.preference;
	}

	@Override
	public void mutate() {

		int mutateIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length - 1);
		int mutateIndexPreference = Uniform.staticNextIntFromTo(0, this.preference.length - 1);

		this.character[mutateIndexCharacter] = (byte) Uniform.staticNextIntFromTo(0, 99);
		this.preference[mutateIndexPreference] = (byte) Uniform.staticNextIntFromTo(0, 99);

	}

	@Override
	public float getInvesment() {
		return this.investment;
	}

	@Override
	public void performAction(float[][] navValues, int it, int cycle) {
		boolean actionPerformed = false;

		while (!actionPerformed) {
			if (holdProb() >= Uniform.staticNextIntFromTo(0, 100)) {
				hold(it, cycle, navValues);
				actionPerformed = true;
				continue;
			}
//			if (sellProb() >= Uniform.staticNextIntFromTo(0, 100)) {
//				actionPerformed = true;
//				sell(it, cycle, navValues);
//			}

			if (buyProb() >= Uniform.staticNextIntFromTo(0, 100)) {
				actionPerformed = true;
				buy(it, cycle, navValues);
			}
		}

	}

	protected byte[] crossOverCharacter(Unit other) {
		int crossIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length - 1);

		byte[] nCharacter = new byte[this.character.length];

		byte[] otherCharacter = other.getCharacter();
		for (int i = 0; i < this.character.length; i++) {
			if (i <= crossIndexCharacter) {
				nCharacter[i] = this.character[i];
			} else {
				nCharacter[i] = otherCharacter[i];
			}
		}
		return nCharacter;
	}

	protected byte[] crossoverPreference(Unit other, byte[] preferenceMap, int actionType) {

		int crossIndex = Uniform.staticNextIntFromTo(0, preferenceMap.length - 1);

		byte[] nPreference = Arrays.copyOf(preferenceMap, preferenceMap.length);
		byte[] otherMap = other.getBuyPreferenceMap();
		for (int etf_index = crossIndex; etf_index < otherMap.length; etf_index++) {
			nPreference[etf_index] = otherMap[etf_index];
		}
		return nPreference;
	}

	protected void incrementPreference(int index, byte[] preference2) {
		if (preference2[index] + MODIFIER < 100) {
			preference2[index] += MODIFIER;
		}
	}

}
