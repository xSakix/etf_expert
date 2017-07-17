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
import org.eft.evol.preference.PreferenceUnit;
import org.eft.evol.preference.UnitWithPreference;
import org.etf.provider.ConfigProvider;
import org.joda.time.DateTime;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

import cern.jet.random.Uniform;

public class PreferenceMain
{

    private static final int INVESTMENT_PERIOD = 30;
    private static final float INVESTMENT = 300.0f;
    private static final int POPULATION_SIZE = 100;
    private static final float MUTATE = 0.05f;
    private static final int ITER_MAX = 200;

    private static final File LOG = new File(
	    ConfigProvider.DIR + "simulation.log");
    
    private static final File RESULTS_CSV = new File(
	    ConfigProvider.DIR + ConfigProvider.DATE_STR + "_results.csv");

    public static void main(String[] args) throws IOException
    {

	List<ETF> loadedETFS = FileLoader.loadAllUSD(0);

	for (int i = 0; i < loadedETFS.size(); i++)
	{
	    ETF etf = loadedETFS.get(i);
	    ETFMap.getInstance(loadedETFS.size()).putIndex(etf.getTicket(), i);
	}

	ETF maxETFByRange = loadedETFS.stream().max(new Comparator<ETF>()
	{

	    @Override
	    public int compare(ETF o1, ETF o2)
	    {
		return Integer.compare(o1.getNavDataList().size(),
			o2.getNavDataList().size());
	    }
	}).get();
	int maxSize = maxETFByRange.getNavDataList().size();
	Date start = maxETFByRange.getNavDataList().get(0).getDate();
	Date finish = maxETFByRange.getNavDataList().get(maxSize - 1).getDate();
	System.out.println(
		"Starting at " + start + " and finishing at " + finish);

	
	List<Map<Integer, Float>> navValues = new ArrayList<>();
	List<Map<Integer, Float>> divValues = new ArrayList<>();

	for (int index = 0; index < maxSize; index++)
	{
	    navValues.add(index, getEtfValueMap(loadedETFS, index, start,false));
	    divValues.add(index, getEtfValueMap(loadedETFS, index, start,true));
	}

	
	if (!Files.exists(LOG.toPath()))
	{
	    LOG.delete();
	}

	appendMessage("Loaded " + loadedETFS.size() + " etfs.");
	appendMessage("Rate of mutation: "+MUTATE);
	appendMessage("Iter max:"+ITER_MAX);
	appendMessage("Investment period:"+INVESTMENT_PERIOD);
	appendMessage("Investment money:"+INVESTMENT);
	appendMessage("Population size:"+POPULATION_SIZE);
	
	for(int i = 0; i < 10;i++){
	    experiment(loadedETFS, maxSize, navValues,divValues);
	}
    }

    private static <T extends PreferenceUnit> void experiment(
	    List<ETF> loadedETFS, int maxSize,
	    List<Map<Integer, Float>> navValues, List<Map<Integer, Float>> divValues)
    {
	List<PreferenceUnit> units = new ArrayList<>();
	List<Float> results = new ArrayList<Float>();
	
	for (int i = 0; i < POPULATION_SIZE; i++)
	{

	    units.add(new UnitWithPreference(loadedETFS.size(),
		    UnitSequenceGenerator.getID()));
	}

	int it = 0;
	appendMessage("Evolution start: " + Calendar.getInstance().getTime());
	long time = System.currentTimeMillis();
	while (true)
	{
	    String msg = "Starting iteration[" + it + "]";
	    System.out.println(msg);
	    appendMessage(msg);
	    int cycle = 0;
	    if (navValues.get(cycle).size() == 0)
	    {
		appendMessage("[" + cycle + "] empty market!");
	    }

	    while (units.size() > 1 && cycle < maxSize)
	    {

		List<PreferenceUnit> epoch = new ArrayList<>(units);
		AtomicInteger removed = new AtomicInteger(0);
		for (PreferenceUnit unit : epoch)
		{
		    oneStep(navValues,divValues, units, it, cycle, unit, removed);
		}
		if (removed.get() > 0)
		{
		    appendMessage("[" + cycle + "]Number of removed:" + removed
			    + " from " + POPULATION_SIZE);
		}
		int toBeAdded = POPULATION_SIZE - units.size();

		Integer mutated = 0;
		Integer crossover = 0;
		if (toBeAdded > 0)
		{
		    List<PreferenceUnit> winners = getWinners(cycle, navValues,
			    units, toBeAdded * 2);
		    if (winners.size() == 1)
		    {
			appendMessage("[" + cycle
				+ "] Only one winner found. Population stagnates....adding mutation of winner to population.");
			PreferenceUnit winner = winners.get(0);
			while (units.size() != POPULATION_SIZE)
			{
			    PreferenceUnit newUnit = getUnitImplementation(
				    winner);
			    newUnit.mutate();
			    mutated++;
			    newUnit.addCash(
				    winner.getInvesment() - Unit.BASE_CASH);
			    units.add(newUnit);
			    newUnit.appendHistory(it, cycle, navValues);
			}
		    } else
		    {
			while (units.size() != POPULATION_SIZE)
			{
			    int firstChosen = 0;
			    int secondChosen = 1;
			    if (winners.size() > 2)
			    {
				firstChosen = Uniform.staticNextIntFromTo(0,
					winners.size() - 1);
				secondChosen = Uniform.staticNextIntFromTo(0,
					winners.size() - 1);
				while (secondChosen == firstChosen)
				{
				    secondChosen = Uniform.staticNextIntFromTo(
					    0, winners.size() - 1);
				}
			    }

			    PreferenceUnit first = winners.get(firstChosen);
			    PreferenceUnit second = winners.get(secondChosen);
			    PreferenceUnit newUnit = first.crossOver(second);
			    crossover++;

			    if (MUTATE >= Uniform.staticNextFloatFromTo(0.0f,
				    1.0f))
			    {
				newUnit.mutate();
				mutated++;
			    }

			    newUnit.addCash(
				    first.getInvesment() - Unit.BASE_CASH);

			    units.add(newUnit);
			    newUnit.appendHistory(it, cycle, navValues);
			}
		    }
		    appendMessage(
			    "[" + cycle + "]Number of crossover:" + crossover);
		    appendMessage(
			    "[" + cycle + "]Number of mutated:" + mutated);

		}
		cycle++;
	    }

	    List<PreferenceUnit> winners = getWinners(cycle - 1, navValues, units, 10);
	    
	    results.add(it, winners.get(0).netAssetValue(cycle - 1, navValues));
	    
	    for (PreferenceUnit unit : winners)
	    {
		unit.logToFile(it);
	    }

	    if(was90PercentReached(winners)){
		break;
	    }

	    
	    it++;
	    for (PreferenceUnit unit : units)
	    {
		unit.resetAssets(it);
		unit.resetLogs();
	    }
	}

	time = System.currentTimeMillis() - time;
	appendMessage("Simulation end: " + Calendar.getInstance().getTime());
	appendMessage("Simulation took: " + time + "[ms]");
	
	logResultsToFile(results);
    }

    private static PreferenceUnit getUnitImplementation(PreferenceUnit winner)
    {
	return new UnitWithPreference(UnitSequenceGenerator.getID(),
		winner.getCharacter(), winner.getPreferences());
    }

    private static void oneStep(List<Map<Integer, Float>> navValues,
	    List<Map<Integer, Float>> divValues, List<PreferenceUnit> units, int it, int cycle, PreferenceUnit unit,
	    AtomicInteger removed)
    {
	if (cycle > 0 && cycle % INVESTMENT_PERIOD == 0)
	{
	    unit.addCash(INVESTMENT);
	}

	performAction(navValues, it, cycle, unit);

	calculateDividends(divValues, it, cycle, unit);
	
	unit.appendHistory(it, cycle, navValues);

	// raz rocne budeme balancovat, ak su v strate idu prec
	if (cycle % 365 == 0)
	{
	    float cashAdded = (float) (((int) (cycle / 120))
		    * (INVESTMENT / 2.0f));
	    float coef = 200.0f + cashAdded;
	    float netAssetValue = unit.netAssetValue(cycle, navValues);
	    if (netAssetValue < coef)
	    {
		units.remove(unit);
		removed.incrementAndGet();
	    }
	}
    }

    private static void performAction(List<Map<Integer, Float>> navValues,
	    int it, int cycle, PreferenceUnit unit)
    {
	unit.performAction(navValues, it, cycle);
    }
    private static void calculateDividends(List<Map<Integer, Float>> dividends, int it,
	    int cycle, PreferenceUnit unit)
    {
	unit.calculateDividends(dividends, cycle);
    }
    

    private static Map<Integer, Float> getEtfValueMap(List<ETF> loadedETFS,
	    int cycle, Date startDate, boolean dividends)
    {
	Map<Integer, Float> etfValueMap = new HashMap<>();
	DateTime start = new DateTime(startDate.getTime());
	DateTime actual = start.plusDays(cycle);
	for (ETF etf : loadedETFS)
	{
	    Integer key = ETFMap.getInstance().getIndex(etf.getTicket());

	    Float nav = 0.0f;
	    DateTime etfStart = new DateTime(
		    etf.getNavDataList().get(0).getDate().getTime());
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

    private static List<PreferenceUnit> getVictims(int cycle,
	    List<Map<Integer, Float>> etfValueMap, List<PreferenceUnit> epoch, int num)
    {
	return getChosen(cycle, etfValueMap, epoch, num, ChosenType.Victim);
    }

    private enum ChosenType
    {
	Winner, Victim
    }

    private static List<PreferenceUnit> getWinners(int cycle,
	    List<Map<Integer, Float>> etfValueMap, List<PreferenceUnit> epoch, int num)
    {
	return getChosen(cycle, etfValueMap, epoch, num, ChosenType.Winner);
    }

    private static List<PreferenceUnit> getChosen(int cycle,
	    List<Map<Integer, Float>> etfValueMap, List<PreferenceUnit> epoch, int num,
	    ChosenType chosenType)
    {
	List<PreferenceUnit> chosenList = new ArrayList<PreferenceUnit>();
	Map<Float, List<PreferenceUnit>> map = new HashMap<>();

	for (PreferenceUnit unit : epoch)
	{
	    Float key = Float.valueOf(unit.netAssetValue(cycle, etfValueMap));
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
	if (chosenType == ChosenType.Winner)
	{
	    Collections.sort(ordered, Collections.reverseOrder());
	} else if (chosenType == ChosenType.Victim)
	{
	    Collections.sort(ordered);
	}

	for (Float key : ordered)
	{
	    if (chosenList.size() <= num)
	    {
		if (chosenList.size() + map.get(key).size() <= num)
		{
		    chosenList.addAll(map.get(key));
		} else
		{
		    // cs + ms <= num
		    // ms <= num - cs
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

    public static void appendMessage(String msg)
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
    
    private static void logResultsToFile(List<Float> results)
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

    
    private static boolean was90PercentReached(List<PreferenceUnit> winners)
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
