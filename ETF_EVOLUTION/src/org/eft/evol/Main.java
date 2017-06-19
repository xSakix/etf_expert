package org.eft.evol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eft.evol.model.ETFMap;
import org.eft.evol.model.Unit;
import org.eft.evol.model.UnitChoosyImpl;
import org.eft.evol.model.UnitImpl;
import org.eft.evol.model.UnitSequenceGenerator;
import org.eft.evol.model.UnitSigmoidImpl;
import org.etf.provider.ConfigProvider;
import org.joda.time.DateTime;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

import cern.jet.random.Uniform;

public class Main {

	private static final float INVESTMENT = 100.0f;
	private static final int POPULATION_SIZE = 5000;
	private static final float MUTATE = 0.1f;
	private static final int ITER_MAX = 100;
	private static final int INVESTMENT_PERIOD = 30;
	//private static final int REBALANCE_PERIOD = 90;

	private static final File LOG = new File(ConfigProvider.DIR + "simulation.log");

	public static void main(String[] args) throws IOException {

		List<ETF> loadedETFS = FileLoader.loadAllUSD(0);

		loadedETFS.stream().forEach(e -> System.out.println(e.getTicket()));

		for (int i = 0; i < loadedETFS.size(); i++) {
			ETF etf = loadedETFS.get(i);
			ETFMap.getInstance().putIndex(etf.getTicket(), i);
		}

		ETF maxETFByRange = loadedETFS.stream().max(new Comparator<ETF>() {

			@Override
			public int compare(ETF o1, ETF o2) {
				return Integer.compare(o1.getNavDataList().size(), o2.getNavDataList().size());
			}
		}).get();
		int maxSize = maxETFByRange.getNavDataList().size();
		Date start = maxETFByRange.getNavDataList().get(0).getDate();
		Date finish = maxETFByRange.getNavDataList().get(maxSize - 1).getDate();
		System.out.println("Starting at " + start + " and finishing at " + finish);

		if (!Files.exists(LOG.toPath())) {
			LOG.delete();
		}

		appendMessage("Loaded " + loadedETFS.size() + " etfs.");

		List<Map<Integer, Float>> navValues = new ArrayList<>();

		for (int index = 0; index < maxSize; index++) {
			navValues.add(index, getEtfValueMap(loadedETFS, index, start));
		}

		// System.out.println(Arrays.toString(loadedETFS.get(0).getNavDataList().toArray()));

		experiment(loadedETFS, maxSize, navValues, UnitChoosyImpl.class);
		// experiment(loadedETFS, maxSize, navValues, UnitImpl.class);
		// experiment(loadedETFS, maxSize, navValues, UnitSigmoidImpl.class);
		// experiment(loadedETFS, maxSize, navValues, null);
	}

	private static <T extends Unit> void experiment(List<ETF> loadedETFS, int maxSize,
			List<Map<Integer, Float>> navValues, Class<T> klass) {
		List<Unit> units = new ArrayList<>();
		if (klass != null) {
			for (int i = 0; i < POPULATION_SIZE; i++) {

				if (UnitChoosyImpl.class.equals(klass)) {
					units.add(new UnitChoosyImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
				} else if (UnitImpl.class.equals(klass)) {
					units.add(new UnitImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
				} else if (UnitSigmoidImpl.class.equals(klass)) {
					units.add(new UnitSigmoidImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
				}
			}
		} else {

			for (int i = 0; i < POPULATION_SIZE / 3; i++) {
				units.add(new UnitImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
			}
			for (int i = POPULATION_SIZE; i < 2 * (POPULATION_SIZE / 3); i++) {
				units.add(new UnitChoosyImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
			}

			for (int i = 2 * (POPULATION_SIZE / 3); i < POPULATION_SIZE; i++) {
				units.add(new UnitSigmoidImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
			}

			for (int i = units.size() - 1; i < POPULATION_SIZE; i++) {
				units.add(new UnitChoosyImpl(loadedETFS.size(), UnitSequenceGenerator.getID()));
			}
		}

		int iterationsMax = ITER_MAX;
		int it = 0;
		appendMessage("Evolution start: " + Calendar.getInstance().getTime());
		long time = System.currentTimeMillis();
		while (it < iterationsMax) {
			appendMessage("Starting iteration[" + it + "]");
			int cycle = 0;
			if (navValues.get(cycle).size() == 0) {
				appendMessage("[" + cycle + "] empty market!");
			}

			// long startCycle = System.currentTimeMillis();
			while (units.size() > 1 && cycle < maxSize) {

				List<Unit> epoch = new ArrayList<>(units);
				//AtomicInteger removed = new AtomicInteger(0);
				for (Unit unit : epoch) {
					//oneStep(navValues, units, it, cycle, unit, removed);
					oneStep(navValues, units, it, cycle, unit);
				}
				// if (removed.get() > 0) {
				// appendMessage("[" + cycle + "]Number of removed:" + removed +
				// " from " + POPULATION_SIZE);
				// }
				// int toBeAdded = POPULATION_SIZE - units.size();
				// crossoverPopulation(navValues, units, it, cycle);

				cycle++;
			}

			List<Unit> winners = getWinners(cycle - 1, navValues, units, (int) (0.1 * POPULATION_SIZE));
			for (Unit unit : winners) {
				unit.logToFile(it);
			}

			crossoverPopulation(navValues, units, winners, it, cycle);

			it++;
			for (Unit unit : units) {
				unit.resetAssets(it);
				unit.resetLogs();
			}
		}

		time = System.currentTimeMillis() - time;
		appendMessage("Simulation end: " + Calendar.getInstance().getTime());
		appendMessage("Simulation took: " + time + "[ms]");
	}

	private static void crossoverPopulation(List<Map<Integer, Float>> navValues, List<Unit> units, List<Unit> winners,
			int it, int cycle) {
		Integer mutated = 0;
		Integer crossover = 0;
		units.clear();
		while (units.size() != POPULATION_SIZE) {
			int firstChosen = 0;
			int secondChosen = 1;
			if (winners.size() > 2) {
				firstChosen = Uniform.staticNextIntFromTo(0, winners.size() - 1);
				secondChosen = Uniform.staticNextIntFromTo(0, winners.size() - 1);
				while (secondChosen == firstChosen) {
					secondChosen = Uniform.staticNextIntFromTo(0, winners.size() - 1);
				}
			}

			Unit first = winners.get(firstChosen);
			Unit second = winners.get(secondChosen);
			Unit newUnit = first.crossOver(second);
			crossover++;

			if (MUTATE >= Uniform.staticNextFloatFromTo(0.0f, 1.0f)) {
				newUnit.mutate();
				mutated++;
			}

			newUnit.addCash(first.getInvesment() - Unit.BASE_CASH);

			units.add(newUnit);
			newUnit.appendHistory(it, cycle, navValues);
		}

		appendMessage("[" + cycle + "]Number of crossover:" + crossover);
		appendMessage("[" + cycle + "]Number of mutated:" + mutated);
		appendMessage(
				"[" + cycle + "]Number of normal units:" + units.stream().filter(u -> u instanceof UnitImpl).count());
		appendMessage("[" + cycle + "]Number of choosy units:"
				+ units.stream().filter(u -> u instanceof UnitChoosyImpl).count());
		appendMessage("[" + cycle + "]Number of sigmoid units:"
				+ units.stream().filter(u -> u instanceof UnitSigmoidImpl).count());
	}

	private static Unit getUnitImplementation(Unit winner) {
		if (winner instanceof UnitChoosyImpl) {
			return new UnitChoosyImpl(
						UnitSequenceGenerator.getID(), 
						winner.getCharacter(), 
						winner.getBuyPreferenceMap(),
						winner.getSellPreferenceMap(),
						winner.getHoldPreferenceMap());
		} else if (winner instanceof UnitSigmoidImpl) {
			return new UnitSigmoidImpl(UnitSequenceGenerator.getID(), winner.getCharacter(), winner.getBuyPreferenceMap());
		}
		return new UnitImpl(UnitSequenceGenerator.getID(), winner.getCharacter(), winner.getBuyPreferenceMap());
	}

	private static void oneStep(List<Map<Integer, Float>> navValues, List<Unit> units, int it, int cycle, Unit unit) {
		if (cycle > 0 && cycle % INVESTMENT_PERIOD == 0) {
			unit.addCash(INVESTMENT);
		}

		performAction(navValues, it, cycle, unit);

		unit.appendHistory(it, cycle, navValues);

		// raz rocne budeme balancovat, ak su v strate idu prec
		// raz pol-rocne budeme balancovat, ak su v strate idu prec
		/*
		 * if (cycle % REBALANCE_PERIOD == 0) { float cashAdded = (float)
		 * (((int) (cycle / INVESTMENT_PERIOD)) * (INVESTMENT)); float coef =
		 * 250.0f + cashAdded; float netAssetValue = unit.netAssetValue(cycle,
		 * navValues); if (netAssetValue < coef) { units.remove(unit);
		 * removed.incrementAndGet(); } }
		 */
	}

	private static void performAction(List<Map<Integer, Float>> navValues, int it, int cycle, Unit unit) {
		unit.performAction(navValues, it, cycle);
	}

	private static Map<Integer, Float> getEtfValueMap(List<ETF> loadedETFS, int cycle, Date startDate) {
		Map<Integer, Float> etfValueMap = new HashMap<>();
		DateTime start = new DateTime(startDate.getTime());
		DateTime actual = start.plusDays(cycle);
		for (ETF etf : loadedETFS) {
			Integer key = ETFMap.getInstance().getIndex(etf.getTicket());

			Float nav = 0.0f;
			DateTime etfStart = new DateTime(etf.getNavDataList().get(0).getDate().getTime());
			if (!etfStart.isAfter(actual)) {
				for (NavData navData : etf.getNavDataList()) {
					DateTime etfTime = new DateTime(navData.getDate().getTime());
					if (etfTime.equals(actual)) {
						nav = Float.valueOf(navData.getNav());
						break;
					}
				}
			}
			if (nav == 0.0f) {
				continue;
			}

			etfValueMap.put(key, nav);
		}

		return etfValueMap;
	}

	private static List<Unit> getVictims(int cycle, List<Map<Integer, Float>> etfValueMap, List<Unit> epoch, int num) {
		return getChosen(cycle, etfValueMap, epoch, num, ChosenType.Victim);
	}

	private enum ChosenType {
		Winner, Victim
	}

	private static List<Unit> getWinners(int cycle, List<Map<Integer, Float>> etfValueMap, List<Unit> epoch, int num) {
		return getChosen(cycle, etfValueMap, epoch, num, ChosenType.Winner);
	}

	private static List<Unit> getChosen(int cycle, List<Map<Integer, Float>> etfValueMap, List<Unit> epoch, int num,
			ChosenType chosenType) {
		List<Unit> chosenList = new ArrayList<Unit>();
		Map<Float, List<Unit>> map = new HashMap<>();

		for (Unit unit : epoch) {
			Float key = Float.valueOf(unit.netAssetValue(cycle, etfValueMap));
			if (map.containsKey(key)) {
				map.get(key).add(unit);
			} else {
				List<Unit> u = new ArrayList<>();
				u.add(unit);
				map.put(key, u);
			}

		}

		List<Float> ordered = new ArrayList<>(map.keySet());
		if (chosenType == ChosenType.Winner) {
			Collections.sort(ordered, Collections.reverseOrder());
		} else if (chosenType == ChosenType.Victim) {
			Collections.sort(ordered);
		}

		for (Float key : ordered) {
			if (chosenList.size() <= num) {
				if (chosenList.size() + map.get(key).size() <= num) {
					chosenList.addAll(map.get(key));
				} else {
					// cs + ms <= num
					// ms <= num - cs
					chosenList.addAll(map.get(key).subList(0, num - chosenList.size()));
				}
			} else {
				break;
			}
		}

		return chosenList;
	}

	public static void appendMessage(String msg) {
		if (!msg.endsWith("\n")) {
			msg += "\n";
		}
		if (!Files.exists(LOG.toPath())) {
			try {
				LOG.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			Files.write(LOG.toPath(), msg.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
