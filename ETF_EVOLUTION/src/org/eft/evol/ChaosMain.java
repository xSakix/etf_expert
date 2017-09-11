package org.eft.evol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
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
import org.eft.evol.model.UnitSequenceGenerator;
import org.eft.evol.preference.ChaosPreferenceUnit;
import org.eft.evol.preference.PreferenceUnit;
import org.etf.provider.ConfigProvider;
import org.joda.time.DateTime;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

import cern.jet.random.Uniform;

public class ChaosMain
{

    private static final int NUMBER_OF_EXPERIMENTS = 1;
    private static final int INVESTMENT_PERIOD = 30;
    private static final float INVESTMENT = 300.0f;
    private static final int POPULATION_SIZE = 500;
    private static final float MUTATE = 0.05f;
    private static final int ITER_MAX = 200;

    private static final File LOG = new File(getSimulationFileName());
    private static final File RESULTS_CSV = new File(getResultCSVFileName());
    private List<ETF> loadedETFS;
    private List<Map<Integer, Float>> navValues = new ArrayList<>();
    private List<Map<Integer, Float>> divValues = new ArrayList<>();
    private List<PreferenceUnit> units = new ArrayList<>();
    private List<Float> results = new ArrayList<Float>();
    private int simulationDays;

    private static String getSimulationFileName()
    {
	StringBuilder builder = new StringBuilder();

	builder.append(ConfigProvider.DIR);
	builder.append("simulation.log");

	return builder.toString();
    }

    private static String getResultCSVFileName()
    {
	StringBuilder builder = new StringBuilder();

	builder.append(ConfigProvider.DIR);
	builder.append(ConfigProvider.DATE_STR);
	builder.append("_results.csv");

	return builder.toString();
    }

    public static void main(String[] args) throws IOException
    {
	ChaosMain preferenceMain = new ChaosMain();
	preferenceMain.run();
    }

    public void run() throws IOException
    {

	loadedETFS = FileLoader.loadAllUSD(0);

	initTicketToIndexOfETFMap();

	ETF maxETFByRange = loadETFByLongestNavList();

	this.simulationDays = loadSimulationDays(maxETFByRange);
	Date start = loadSimulationStartDate(maxETFByRange);
	Date finish = loadSimulationFinishDate(maxETFByRange);

	System.out.println(getStartMessage(start, finish));

	initializeNAVandDIV(start);

	deleteLogFileIfExists();

	appendInitMessagesToLog();

	runExperiments();
    }

    private String getStartMessage(Date start, Date finish)
    {
	StringBuilder builder = new StringBuilder();

	builder.append("Starting at ");
	builder.append(start);
	builder.append(" and finishing at ");
	builder.append(finish);

	return builder.toString();
    }

    private void runExperiments()
    {
	for (int i = 0; i < NUMBER_OF_EXPERIMENTS; i++)
	{
	    experiment();
	}
    }

    private void appendInitMessagesToLog()
    {
	StringBuilder builder = new StringBuilder();
	builder.append("Loaded ");
	builder.append(loadedETFS.size());
	builder.append(" etfs.\n");

	builder.append("Rate of mutation: ");
	builder.append(MUTATE);
	builder.append('\n');

	builder.append("Iter max:");
	builder.append(ITER_MAX);
	builder.append('\n');

	builder.append("Investment period:");
	builder.append(INVESTMENT_PERIOD);
	builder.append('\n');

	builder.append("Investment money:");
	builder.append(INVESTMENT);
	builder.append('\n');

	builder.append("Population size:");
	builder.append(POPULATION_SIZE);
	builder.append('\n');

	appendMessage(builder.toString());
    }

    private void deleteLogFileIfExists()
    {
	if (!Files.exists(LOG.toPath()))
	{
	    LOG.delete();
	}
    }

    private void initializeNAVandDIV(Date start)
    {
	for (int day = 0; day < simulationDays; day++)
	{
	    navValues.add(day, getEtfValueMap(day, start, false));
	    divValues.add(day, getEtfValueMap(day, start, true));
	}
    }

    private int loadSimulationDays(ETF maxETFByRange)
    {
	return maxETFByRange.getNavDataList().size();
    }

    private Date loadSimulationFinishDate(ETF maxETFByRange)
    {
	return maxETFByRange.getNavDataList().get(simulationDays - 1).getDate();
    }

    private Date loadSimulationStartDate(ETF maxETFByRange)
    {
	return maxETFByRange.getNavDataList().get(0).getDate();
    }

    private ETF loadETFByLongestNavList()
    {
	return loadedETFS.stream().max(new ETFComparator()).get();
    }

    private void initTicketToIndexOfETFMap()
    {
	ETFMap etfMap = ETFMap.getInstance(loadedETFS.size());

	for (int i = 0; i < loadedETFS.size(); i++)
	{
	    addOneETF(etfMap, i);
	}
    }

    private void addOneETF(ETFMap etfMap, int i)
    {
	ETF etf = loadedETFS.get(i);
	String ticket = etf.getTicket();
	etfMap.putIndex(ticket, i);
    }

    private <T extends PreferenceUnit> void experiment()
    {
	initializePopulation();

	int it = 0;
	logEvolutionStartMsg();

	long time = System.currentTimeMillis();

	while (true)
	{
	    List<PreferenceUnit> winners = runOneIteration(it);

	    if (endConditionReached(winners,it))
	    {
		break;
	    }

	    it++;
	    resetPopulation(it);
	}

	appendSimulationEndMessages(time);

	logResultsToFile();
    }

    private void logEvolutionStartMsg()
    {
	StringBuilder builder = new StringBuilder();

	builder.append("Evolution start: ");
	builder.append(Calendar.getInstance().getTime());

	appendMessage(builder.toString());
    }

    private boolean endConditionReached(List<PreferenceUnit> winners, int it)
    {
//	return was90PercentReached(winners);
	return it >= ITER_MAX;
    }

    private List<PreferenceUnit> runOneIteration(int it)
    {
	startIterationMessage(it);
	int day = 0;

	checkEmptyMarketSituation(day);

	runOneFullSimulation(it, day);

	return processWinners(it);
    }

    private List<PreferenceUnit> processWinners(int it)
    {
	int lastDayOfSimulation = simulationDays - 1;

	List<PreferenceUnit> winners = getWinners(lastDayOfSimulation, 10);

	float winnerNAV = winners.get(0).netAssetValue(lastDayOfSimulation,
		navValues);

	results.add(it, winnerNAV);

	logWinnersToFiles(it, winners);

	return winners;
    }

    private void logWinnersToFiles(int it, List<PreferenceUnit> winners)
    {
	for (PreferenceUnit unit : winners)
	{
	    unit.logToFile(it);
	}
    }

    private void runOneFullSimulation(int it, int day)
    {
	while (units.size() > 1 && day < simulationDays)
	{

	    runOneDay(it, day++);
	}
    }

    private void runOneDay(int it, int day)
    {
	List<PreferenceUnit> epoch = new ArrayList<>(units);

	AtomicInteger removed = new AtomicInteger(0);

	for (PreferenceUnit unit : epoch)
	{
	    oneStep(it, day, unit, removed);
	}

	adjustPopulation(it, day, removed);
    }

    private void startIterationMessage(int it)
    {
	String msg = String.format("Starting iteration[%d]\n", it);
	System.out.println(msg);
	appendMessage(msg);
    }

    private void checkEmptyMarketSituation(int day)
    {
	if (navValues.get(day).size() == 0)
	{
	    appendMessage(String.format("[%d] empty market!\n", day));
	}
    }

    private void appendSimulationEndMessages(long time)
    {
	StringBuilder builder = new StringBuilder();

	builder.append("Simulation end: ");
	builder.append(Calendar.getInstance().getTime());
	builder.append('\n');
	builder.append("Simulation took: ");
	builder.append((System.currentTimeMillis() - time));
	builder.append("[ms]");
	builder.append('\n');

	appendMessage(builder.toString());
    }

    private void resetPopulation(int it)
    {
	for (PreferenceUnit unit : units)
	{
	    unit.resetAssets(it);
	    unit.resetLogs();
	}
    }

    private final class ETFComparator implements Comparator<ETF>
    {
	@Override
	public int compare(ETF o1, ETF o2)
	{
	    return Integer.compare(loadSimulationDays(o1),
		    loadSimulationDays(o2));
	}
    }

    private class CrossoverMutatePop
    {
	Integer mutated = 0;
	Integer crossover = 0;

	public void crossOrMutate(int it, int cycle, AtomicInteger removed,
		int toBeAdded)
	{
	    List<PreferenceUnit> winners = getWinners(cycle, toBeAdded * 2);

	    if (isOnlyOneWinner(winners))
	    {
		mutateWinnerUntilPopulationFilled(it, cycle, winners);
	    } else
	    {
		crossOrMutatePopulation(it, cycle, winners);
	    }
	}

	private void crossOrMutatePopulation(int it, int cycle,
		List<PreferenceUnit> winners)
	{
	    while (units.size() != POPULATION_SIZE)
	    {
		addOneUnitViaCrossAndMutate(it, cycle, winners);
	    }
	}

	private void addOneUnitViaCrossAndMutate(int it, int cycle,
		List<PreferenceUnit> winners)
	{
	    int firstChosen = Uniform.staticNextIntFromTo(0,
		    winners.size() - 1);
	    int secondChosen = Uniform.staticNextIntFromTo(0,
		    winners.size() - 1);

	    PreferenceUnit first = winners.get(firstChosen);
	    PreferenceUnit second = winners.get(secondChosen);
	    PreferenceUnit newUnit = first.crossOver(second);
	    crossover++;

	    if (MUTATE >= Uniform.staticNextFloatFromTo(0.0f, 1.0f))
	    {
		newUnit.mutate();
		mutated++;
	    }

	    newUnit.addCash(first.getInvesment() - Unit.BASE_CASH);

	    units.add(newUnit);
	    newUnit.appendHistory(it, cycle, navValues);
	}

	private void mutateWinnerUntilPopulationFilled(int it, int cycle,
		List<PreferenceUnit> winners)
	{
	    appendMessage(String.format(
		    "[%d] Only one winner found. Population stagnates....adding mutation of winner to population.",
		    cycle));

	    PreferenceUnit winner = winners.get(0);

	    while (units.size() != POPULATION_SIZE)
	    {
		mutateTheChosenWinner(units, it, cycle, winner);
	    }

	}

	private void mutateTheChosenWinner(List<PreferenceUnit> units, int it,
		int cycle, PreferenceUnit winner)
	{
	    PreferenceUnit newUnit = getUnitImplementation(winner);
	    newUnit.mutate();
	    mutated++;
	    newUnit.addCash(winner.getInvesment() - Unit.BASE_CASH);
	    units.add(newUnit);
	    newUnit.appendHistory(it, cycle, navValues);

	}

    }

    private void adjustPopulation(int it, int cycle, AtomicInteger removed)
    {
	logRemovedUnits(cycle, removed);

	int toBeAdded = POPULATION_SIZE - units.size();

	if (!needsToAddNewUnits(toBeAdded))
	{
	    return;
	}

	CrossoverMutatePop crossoverMutatePop = new CrossoverMutatePop();

	crossoverMutatePop.crossOrMutate(it, cycle, removed, toBeAdded);

	appendCrossAndMutMsg(cycle, crossoverMutatePop);

    }

    private void appendCrossAndMutMsg(int cycle,
	    CrossoverMutatePop crossoverMutatePop)
    {
	StringBuilder builder = new StringBuilder();

	builder.append('[');
	builder.append(cycle);
	builder.append("]Number of crossover:");
	builder.append(crossoverMutatePop.crossover);
	builder.append('\n');

	builder.append('[');
	builder.append(cycle);
	builder.append("]Number of mutated:");
	builder.append(crossoverMutatePop.mutated);
	builder.append('\n');

	appendMessage(builder.toString());
    }

    private boolean isOnlyOneWinner(List<PreferenceUnit> winners)
    {
	return winners.size() == 1;
    }

    private boolean needsToAddNewUnits(int toBeAdded)
    {
	return toBeAdded > 0;
    }

    private void logRemovedUnits(int cycle, AtomicInteger removed)
    {
	if (unitsWhereRemoved(removed))
	{
	    StringBuilder builder = new StringBuilder();

	    builder.append('[');
	    builder.append(cycle);
	    builder.append("]Number of removed:");
	    builder.append(removed);
	    builder.append(" from ");
	    builder.append(POPULATION_SIZE);

	    appendMessage(builder.toString());
	}
    }

    private boolean unitsWhereRemoved(AtomicInteger removed)
    {
	return removed.get() > 0;
    }

    private void initializePopulation()
    {
	units.clear();
	
	for (int i = 0; i < POPULATION_SIZE; i++)
	{

	    units.add(new ChaosPreferenceUnit(loadedETFS.size(),
		    UnitSequenceGenerator.getID()));
	}
    }

    private PreferenceUnit getUnitImplementation(PreferenceUnit w)
    {
	ChaosPreferenceUnit winner = (ChaosPreferenceUnit) w;
	
	return new ChaosPreferenceUnit(UnitSequenceGenerator.getID(),winner.getPreferences().length,
		winner.getR_char(), winner.getR_pref());
    }

    private void oneStep(int it, int cycle, PreferenceUnit unit,
	    AtomicInteger removed)
    {
	if (shouldAddInvestmentMoney(cycle))
	{
	    unit.addCash(INVESTMENT);
	}

	performAction(it, cycle, unit);

	calculateDividends(it, cycle, unit);

	unit.appendHistory(it, cycle, navValues);

	balancePopulation(cycle, unit, removed);
	((ChaosPreferenceUnit)unit).growPreferences();
    }

    private boolean shouldAddInvestmentMoney(int cycle)
    {
	return cycle > 0 && cycle % INVESTMENT_PERIOD == 0;
    }

    private void balancePopulation(int cycle, PreferenceUnit unit,
	    AtomicInteger removed)
    {
	if (cycle % 365 != 0)
	{
	    return;
	}

	float cashAdded = computeCashToBeAdded(cycle);
	float coef = 290.0f + cashAdded;
	float netAssetValue = unit.netAssetValue(cycle, navValues);

	if (unitShouldBeRemoved(coef, netAssetValue))
	{
	    removeUnit(unit, removed);
	}
    }

    private void removeUnit(PreferenceUnit unit, AtomicInteger removed)
    {
	units.remove(unit);
	removed.incrementAndGet();
    }

    private boolean unitShouldBeRemoved(float coef, float netAssetValue)
    {
	return netAssetValue < coef;
    }

    private float computeCashToBeAdded(int cycle)
    {
	float investmentCoef = INVESTMENT / 2.0f;
	int cyclePeriod = cycle / INVESTMENT_PERIOD;
	return (float) (cyclePeriod * investmentCoef);
    }

    private void performAction(int it, int cycle, PreferenceUnit unit)
    {
	unit.performAction(navValues, it, cycle);
    }

    private void calculateDividends(int it, int cycle, PreferenceUnit unit)
    {
	unit.calculateDividends(divValues, cycle);
    }

    private Map<Integer, Float> getEtfValueMap(int cycle, Date startDate,
	    boolean dividends)
    {
	Map<Integer, Float> etfValueMap = new HashMap<>();
	DateTime start = new DateTime(startDate.getTime());
	DateTime actual = start.plusDays(cycle);
	for (ETF etf : loadedETFS)
	{
	    Integer key = ETFMap.getInstance().getIndex(etf.getTicket());

	    Float nav = 0.0f;
	    DateTime etfStart = new DateTime(
		    loadSimulationStartDate(etf).getTime());
	    if (!etfStart.isAfter(actual))
	    {
		List<NavData> navDataList = dividends ? etf.getDividendList()
			: etf.getNavDataList();
		if (navDataList == null)
		{
		    break;
		}

		for (NavData navData : navDataList)
		{
		    DateTime etfTime = new DateTime(
			    navData.getDate().getTime());
		    if (etfTime.equals(actual))
		    {
			nav = Float.valueOf(navData.getNav());
			break;
		    }
		}
	    }
	    if (nav == 0.0f)
	    {
		continue;
	    }

	    etfValueMap.put(key, nav);
	}

	return etfValueMap;
    }

    private List<PreferenceUnit> getWinners(int cycle, int num)
    {

	List<PreferenceUnit> chosenList = new ArrayList<PreferenceUnit>();
	Map<Float, List<PreferenceUnit>> map = new HashMap<>();

	for (PreferenceUnit unit : units)
	{
	    Float key = Float.valueOf(unit.netAssetValue(cycle, navValues));
	    if (map.containsKey(key))
	    {
		map.get(key).add(unit);
	    } else
	    {
		List<PreferenceUnit> u = new ArrayList<>();
		u.add(unit);
		map.put(key, u);
	    }

	}

	List<Float> ordered = new ArrayList<>(map.keySet());

	Collections.sort(ordered, Collections.reverseOrder());

	for (Float key : ordered)
	{
	    if (chosenList.size() <= num)
	    {
		if (chosenList.size() + map.get(key).size() <= num)
		{
		    chosenList.addAll(map.get(key));
		} else
		{
		    chosenList.addAll(
			    map.get(key).subList(0, num - chosenList.size()));
		}
	    } else
	    {
		break;
	    }
	}

	return chosenList;
    }

    public void appendMessage(String msg)
    {
	if (!msg.endsWith("\n"))
	{
	    msg += "\n";
	}

	if (!Files.exists(LOG.toPath()))
	{
	    try
	    {
		LOG.createNewFile();
	    } catch (IOException e)
	    {
		e.printStackTrace();
		return;
	    }
	}

	try
	{
	    Files.write(LOG.toPath(), msg.getBytes(),
		    StandardOpenOption.APPEND);
	} catch (IOException e)
	{
	    e.printStackTrace();
	}

    }

    private void logResultsToFile()
    {
	if (!Files.exists(RESULTS_CSV.toPath()))
	{
	    try
	    {
		RESULTS_CSV.createNewFile();
	    } catch (IOException e)
	    {
		e.printStackTrace();
		return;
	    }
	}

	DecimalFormat formatter = new DecimalFormat("#0.00");

	try
	{
	    final StringBuilder builder = new StringBuilder();
	    for (int i = 0; i < results.size(); i++)
	    {
		builder.append(i);
		builder.append(';');
		builder.append(formatter.format(results.get(i)));
		builder.append('\n');
	    }
	    Files.write(RESULTS_CSV.toPath(), builder.toString().getBytes(),
		    StandardOpenOption.WRITE);

	} catch (IOException e)
	{
	    e.printStackTrace();
	}

    }

    private boolean was90PercentReached(List<PreferenceUnit> winners)
    {

	if (winners == null || winners.size() == 0)
	{
	    appendMessage("Winner: not found!");
	    return false;
	}

	PreferenceUnit winner = winners.get(0);
	appendMessage("Winner: " + winner.getID());

	int counter = 0;

	for (int etf_index = 0; etf_index < winner
		.getPreferences().length; etf_index++)
	{
	    if (winner.getPreferences()[etf_index] > 0.98f)
	    {
		counter++;
	    }
	    if (counter > 9)
	    {
		return true;
	    }
	}

	return false;
    }

}
