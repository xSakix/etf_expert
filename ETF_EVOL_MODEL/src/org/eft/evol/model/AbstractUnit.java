package org.eft.evol.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eft.evol.stats.UnitAction;
import org.eft.evol.stats.UnitState;
import org.etf.provider.ConfigProvider;

import cern.jet.random.Uniform;

public abstract class AbstractUnit implements Unit {

	protected static final float TRANSACTION_COST = 4.0f;
	protected static final float MODIFIER = 0.01f;

	float[] character;
	Map<Integer,Float> buyPreference = new HashMap<>();
	Map<Integer,Float> sellPreference = new HashMap<>();
	Map<Integer,Float> holdPreference = new HashMap<>();
	int ID = 0;
	float cash = BASE_CASH;
	float investment = cash;
	protected float sumGradient = 0.0f;


	Map<Integer, Long> etfs = new HashMap<>();

	List<UnitAction> actions = new ArrayList<>();

	// List<UnitState> stateList = new ArrayList<>();
	UnitState currentState;

	public AbstractUnit(int ETF_SIZE, int index) {
		this.character = new float[3];
		this.ID = index;
		init();
		initState();
	}

	public AbstractUnit(int index, float[] character, Map<Integer,Float> preference) {
		this.character = Arrays.copyOf(character, character.length);
		this.buyPreference = new HashMap<>(preference);
		this.ID = index;
		initState();
	}

	private void init() {
		for (int i = 0; i < 3; i++) {
			character[i] = Uniform.staticNextFloatFromTo(0.0f, 1.0f);
		}

	}

	private void initState(){
		this.currentState = new UnitState();
		currentState.nav = this.cash;
		currentState.iteration = 0;
		currentState.cycle = 0;
		currentState.character = Arrays.copyOf(character, character.length);
		currentState.buyPreference = new HashMap<>(buyPreference);
		currentState.sellPreference = new HashMap<>(sellPreference);
		currentState.holdPreference = new HashMap<>(holdPreference);
		currentState.etfs = new HashMap(this.etfs);
	}
	
	public void appendHistory(int it, int cycle, List<Map<Integer, Float>> etfValueMap) {

		if (this.currentState == null) {
			this.currentState = new UnitState();
		}

		currentState.nav = this.netAssetValue(cycle, etfValueMap);
		currentState.iteration = it;
		currentState.cycle = cycle;
		currentState.character = Arrays.copyOf(character, character.length);
		currentState.buyPreference = new HashMap<>(buyPreference);
		currentState.sellPreference = new HashMap<>(sellPreference);
		currentState.holdPreference = new HashMap<>(holdPreference);
		currentState.etfs = new HashMap(this.etfs);

		// this.stateList.add(currentState);
	}

	protected void addGradientToAction(int cycle, List<Map<Integer, Float>> etfValueMap, UnitAction sellAction) {
		if (this.currentState == null) {
			sellAction.gradient = 0.0f;
		} else {
			sellAction.gradient = netAssetValue(cycle, etfValueMap) - this.currentState.nav;
		}
	}

	@Override
	public void hold(int iteration, int cycle, List<Map<Integer, Float>> etfValueMap) {
		UnitAction holdAction = new UnitAction();

		holdAction.actionType = ActionType.HOLD;
		holdAction.cycle = cycle;
		holdAction.iteration = iteration;
		addGradientToAction(cycle, etfValueMap, holdAction);

		actions.add(holdAction);
		
		for(Integer key : holdPreference.keySet()){
			if(etfs.containsKey(key) && holdPreference.get(key)+MODIFIER < 1.0f){
				holdPreference.put(key, holdPreference.get(key)+MODIFIER);
			}
		}
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
		
		StringBuilder builder = new StringBuilder();
		for(Integer key : buyPreference.keySet()){
			builder.append(ETFMap.getInstance().getEtfName(key)+"="+buyPreference.get(key)+",");
		}
		
		return "[[" + ID + "]," + Arrays.toString(character) + "," + builder.toString() + ",[" + cash + "],"
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
			for (UnitAction action : actions) {
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
	public Map<Integer,Float> getBuyPreferenceMap() {
		return this.buyPreference;
	}
	
	@Override
	public Map<Integer,Float> getSellPreferenceMap(){
		return this.sellPreference;
	}
	
	@Override
	public Map<Integer,Float> getHoldPreferenceMap(){
		return this.holdPreference;
	}

	@Override
	public void mutate() {

		int mutateIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length - 1);
		//int mutateIndexPreference = Uniform.staticNextIntFromTo(0, this.preference.length - 1);

		this.character[mutateIndexCharacter] = Uniform.staticNextFloatFromTo(0.0f, 1.0f);
		//this.preference[mutateIndexPreference] = Uniform.staticNextFloatFromTo(0.0f, 1.0f);

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

			UnitAction bankruptAction = new UnitAction();

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
	
	protected float getBuyPreference(int index) {
		float prob = 0.5f;
		if(buyPreference.containsKey(index)){
			prob = buyPreference.get(index);
		}else{
			buyPreference.put(index, prob);			
		}
		return prob;
	}

	protected float getSellPreference(int index) {
		float prob = 0.5f;
		if(sellPreference.containsKey(index)){
			prob = sellPreference.get(index);
		}else{
			sellPreference.put(index, prob);			
		}
		return prob;
	}
	
	protected float getHoldPreference(int index) {
		float prob = 0.1f;
		if(holdPreference.containsKey(index)){
			prob = holdPreference.get(index);
		}else{
			holdPreference.put(index, prob);			
		}
		return prob;
	}

	protected float[] crossOverCharacter(Unit other) {
		int crossIndexCharacter = Uniform.staticNextIntFromTo(0, this.character.length-1);

		float[] nCharacter = new float[this.character.length];

		float[] otherCharacter = other.getCharacter();
		for(int i = 0;i < this.character.length;i++){
			if(i <= crossIndexCharacter){
				nCharacter[i] = this.character[i]; 
			}else{
				nCharacter[i] = otherCharacter[i];
			}
		}
		return nCharacter;
	}
	
	protected Map<Integer, Float> crossoverPreference(Unit other, Map<Integer,Float> preferenceMap,int actionType) {
		Map<Integer,Float> nPreference = new HashMap<>(preferenceMap);
		Map<Integer,Float> otherMap = getOtherMap(other,actionType);
		for(Integer key : otherMap.keySet()){
			if(nPreference.containsKey(key)){
				nPreference.put(key, (nPreference.get(key)+otherMap.get(key))/2.0f);
			}else{
				nPreference.put(key, otherMap.get(key));
			}
			
		}
		return nPreference;
	}
	
	private Map<Integer, Float> getOtherMap(Unit other, int actionType) {
		if(actionType == ActionType.BUY){
			return buyPreference;
		}
		if(actionType == ActionType.SELL){
			return sellPreference;
		}
		if(actionType == ActionType.HOLD){
			return holdPreference;
		}
		
		return new HashMap<>();
	}

	protected void incrementPreference(int index, Map<Integer,Float> map) {
		if(map.get(index) + MODIFIER <= 1.0f){
			map.put(index, map.get(index) +MODIFIER);
		}
	}
	
}
