package org.eft.evol.preference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eft.evol.model.ActionType;
import org.eft.evol.stats.UnitAction;
import org.eft.evol.stats.UnitState;
import org.etf.provider.ConfigProvider;

import cern.jet.random.Uniform;

public abstract class AbstractPreferenceUnit implements PreferenceUnit {

	protected static final float TRANSACTION_COST = 4.0f;
	protected static final float MODIFIER = 0.01f;

	float[] character;
	float[] preference;
	int ID = 0;
	float cash = BASE_CASH;
	float investment = cash;
	protected float sumGradient = 0.0f;

	Map<Integer, Long> etfs = new HashMap<>();

	List<UnitPreferenceAction> actions = new ArrayList<>();

	// List<UnitState> stateList = new ArrayList<>();
	UnitPreferenceState currentState;

	public AbstractPreferenceUnit(int ETF_SIZE, int index) {
		this.character = new float[3];
		this.preference = new float[ETF_SIZE];
		this.ID = index;
		init();
		initState();
	}

	public AbstractPreferenceUnit(int index, float[] character, float[] preference) {
		this.character = Arrays.copyOf(character, character.length);
		this.preference = Arrays.copyOf(preference, preference.length);
		this.ID = index;
		initState();
	}

	private void init() {
		for (int i = 0; i < 3; i++) {
			character[i] = 0.01f;
		}
		for (int i = 0; i < preference.length; i++) {
			// preference[i] = Uniform.staticNextFloatFromTo(0.0f, 0.5f);
			preference[i] = 0.01f;
			// character[i] = 0.5d;
		}

	}

	private void initState() {
		this.currentState = new UnitPreferenceState();
		currentState.nav = this.cash;
		currentState.iteration = 0;
		currentState.cycle = 0;
		currentState.character = Arrays.copyOf(character, character.length);
		currentState.preference = Arrays.copyOf(preference, preference.length);
		currentState.etfs = new HashMap(this.etfs);
	}

	public void appendHistory(int it, int cycle, List<Map<Integer, Float>> etfValueMap) {

		if (this.currentState == null) {
			this.currentState = new UnitPreferenceState();
		}

		currentState.nav = this.netAssetValue(cycle, etfValueMap);
		currentState.iteration = it;
		currentState.cycle = cycle;
		currentState.character = Arrays.copyOf(character, character.length);
		currentState.preference = Arrays.copyOf(preference, preference.length);
		currentState.etfs = new HashMap(this.etfs);

		// this.stateList.add(currentState);
	}

	protected void addGradientToAction(int cycle, List<Map<Integer, Float>> etfValueMap,
			UnitPreferenceAction sellAction) {
		if (this.currentState == null) {
			sellAction.gradient = 0.0f;
		} else {
			sellAction.gradient = netAssetValue(cycle, etfValueMap) - this.currentState.nav;
		}
	}

	@Override
	public void hold(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {
		UnitPreferenceAction holdAction = new UnitPreferenceAction();

		holdAction.actionType = ActionType.HOLD;
		holdAction.cycle = cycle;
		holdAction.iteration = iteration;
		addGradientToAction(cycle, etfValueMap, holdAction);

		actions.add(holdAction);
	}

	@Override
	public float buyProb() {
		return character[INDEX_BUY];
	}

	@Override
	public float sellProb() {
		return character[INDEX_SELL];
	}

	@Override
	public float holdProb() {
		return character[INDEX_HOLD];
	}

	@Override
	public float netAssetValue(int cycle, List<Map<Integer, Float>> etfValueMap) {
		float sum = cash;
		for (Integer indexEtf : etfs.keySet()) {
			// ak je v etfs, tak musela byt jednoducho kupena, t.j. musela
			// existovat v niektorom cykle
			Float nav = getNavValueByIndex(cycle, etfValueMap, indexEtf);

			sum += (float) (etfs.get(indexEtf) * nav);
		}
		return sum;
	}

	private Float getNavValueByIndex(int cycle, List<Map<Integer, Float>> etfValueMap, Integer indexEtf) {
		if (etfValueMap.get(cycle).containsKey(indexEtf)) {
			return etfValueMap.get(cycle).get(indexEtf);
		}

		while (cycle > 0) {
			if (etfValueMap.get(cycle).containsKey(indexEtf)) {
				return etfValueMap.get(cycle).get(indexEtf);
			}
			cycle--;
		}

		return 0.0f;
	}

	@Override
	public String toString() {
		return "[[" + ID + "]," + Arrays.toString(character) + "," + Arrays.toString(preference) + ",[" + cash + "],"
				+ etfs.toString() + "]";
	}

	@Override
	public void resetAssets(int iteration) {
		this.cash = BASE_CASH;
		this.investment = this.cash;
		this.etfs = new HashMap<>();
		this.sumGradient = 0.0f;
		initState();
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public abstract String getDir(int iteration);

	@Override
	public synchronized void logToFile(int iteration) {
		String directory = ConfigProvider.DIR + "/" + getDir(iteration) + "/" + this.currentState.nav;
		// String directory =
		// ConfigProvider.DIR+"/"+iteration+"/"+this.stateList.get(this.stateList.size()-1).nav;
		File dir = new File(directory);
		File actionLogFile = new File(directory + "/" + this.ID + "_action.log");
		File historyLogFile = new File(directory + "/" + this.ID + "_history.log");
		File gradientLogFile = new File(directory + "/" + this.ID + "_gradient.log");

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

		// if(!Files.exists(gradientLogFile.toPath())){
		// try {
		// Files.createFile(gradientLogFile.toPath());
		// } catch (IOException e) {
		// e.printStackTrace();
		// return;
		// }
		// }

		try {
			for (UnitPreferenceAction action : actions) {
				Files.write(actionLogFile.toPath(), action.toString().getBytes(), StandardOpenOption.APPEND);
			}
			/*
			 * for(UnitState state:stateList){
			 * Files.write(historyLogFile.toPath(), state.toString().getBytes(),
			 * StandardOpenOption.APPEND); } float lastNav = 0.0f; for(UnitState
			 * state:stateList){ float gradient = state.nav - lastNav; lastNav =
			 * state.nav;
			 * 
			 * StringBuilder logLine = new StringBuilder();
			 * 
			 * logLine.append(state.iteration); logLine.append(",");
			 * logLine.append(state.cycle); logLine.append(",");
			 * logLine.append(gradient); logLine.append(",["); for(UnitAction
			 * action : actions){ if(action.iteration == state.iteration &&
			 * action.cycle == state.cycle){ logLine.append(action.actionType);
			 * logLine.append(","); } if(action.iteration > state.iteration ||
			 * action.cycle > state.cycle){ break; } } logLine.append("]\n");
			 * 
			 * Files.write(gradientLogFile.toPath(),
			 * logLine.toString().getBytes(), StandardOpenOption.APPEND); }
			 */

			Files.write(historyLogFile.toPath(), currentStateString().getBytes(), StandardOpenOption.APPEND);
			Files.write(historyLogFile.toPath(), ("Total investment:" + investment + "\n").getBytes(),
					StandardOpenOption.APPEND);
			Files.write(historyLogFile.toPath(), ("Total gradient:" + sumGradient + "\n").getBytes(),
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected String currentStateString() {
		return currentState.toString();
	}

	@Override
	public void resetLogs() {
		// this.currentState = null;
		// for(UnitState state : stateList){
		// state.character = null;
		// state.preference = null;
		// }
		// this.stateList.clear();
		this.actions.clear();
	}

	@Override
	public void addCash(float cash) {
		this.cash += cash;
		this.investment += cash;
	}

	@Override
	public float[] getCharacter() {
		return Arrays.copyOf(this.character, this.character.length);
	}

	@Override
	public float[] getPreferences() {
		return Arrays.copyOf(this.preference, this.preference.length);
	}

	@Override
	public void mutate() {

		int mutateIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length - 1);
		// int mutateIndexPreference = Uniform.staticNextIntFromTo(0,
		// this.preference.length - 1);

		this.character[mutateIndexCharacter] = Uniform.staticNextFloatFromTo(0.0f, 1.0f);
		// this.preference[mutateIndexPreference] =
		// Uniform.staticNextFloatFromTo(0.0f, 1.0f);

	}

	@Override
	public float getInvesment() {
		return this.investment;
	}

	@Override
	public void checkBankrupt(int iteration, int cycle, Map<Integer, Float> actual, Map<Integer, Float> previous) {
		if (etfs.keySet().size() == 0) {
			return;
		}

		for (Integer etfKey : etfs.keySet()) {
			float nav = actual.get(etfKey);
			if (nav != 0.0f) {
				continue;
			}
			long shares = etfs.get(etfKey);
			cash += (float) (previous.get(etfKey) * ((float) shares));
			etfs.remove(etfKey);

			UnitPreferenceAction bankruptAction = new UnitPreferenceAction();

			bankruptAction.actionType = ActionType.BANKRUPT;
			bankruptAction.cycle = cycle;
			bankruptAction.iteration = iteration;
			bankruptAction.indexOfETF = etfKey;
			bankruptAction.nav = nav;
			bankruptAction.shares = shares;

			actions.add(bankruptAction);
		}

	}

	@Override
	public void performAction(List<Map<Integer, Float>> navValues, int it, int cycle) {
		boolean actionPerformed = false;

		while (!actionPerformed) {
			if (holdProb() >= Uniform.staticNextFloatFromTo(0.0f, 1.0f)) {
				hold(it, cycle, navValues);
				actionPerformed = true;
				continue;
			}
			if (sellProb() >= Uniform.staticNextFloatFromTo(0.0f, 1.0f)) {
				actionPerformed = true;
				sell(it, cycle, navValues);
			}

			if (buyProb() >= Uniform.staticNextFloatFromTo(0.0f, 1.0f)) {
				actionPerformed = true;
				buy(it, cycle, navValues);
			}
		}

	}

}
