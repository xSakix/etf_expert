package org.eft.evol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eft.evol.model.ETFMap;
import org.eft.evol.model.SimpleUnit;
import org.eft.evol.model.Unit;
import org.eft.evol.model.UnitGAmpl;
import org.eft.evol.model.UnitSequenceGenerator;
import org.etf.provider.ConfigProvider;
import org.joda.time.DateTime;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

import cern.jet.random.Uniform;

public class SimpleMain
{
    private static final float INVESTMENT = 300.0f;
    private static final int POPULATION_SIZE = 20000;
    private static final float MUTATE = 0.05f;
    private static final int ITER_MAX = 400;
    private static final int INVESTMENT_PERIOD = 30;
    // private static final int REBALANCE_PERIOD = 90;

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

	if (!Files.exists(LOG.toPath()))
	{
	    LOG.delete();
	}

	appendMessage("Loaded " + loadedETFS.size() + " etfs.");

	float[][] navValues = new float[maxSize][loadedETFS.size()];
	float[][] dividends = new float[maxSize][loadedETFS.size()];

	for (int index = 0; index < maxSize; index++)
	{
	    navValues[index] = getEtfValueMap(loadedETFS, index, start, false);
	    dividends[index] = getEtfValueMap(loadedETFS, index, start, true);
	}

	int size = loadedETFS.size();
	loadedETFS.clear();
	loadedETFS = null;

	experiment(size, maxSize, navValues, dividends);
    }

    private static <T extends Unit> void experiment(int eftSize, int maxSize,
	    float[][] navValues, float[][] dividends)
    {
	List<SimpleUnit> population = new ArrayList<>(POPULATION_SIZE);
	List<Float> results = new ArrayList<Float>();
	initPopulation(eftSize, population);
	int it = 0;
	appendMessage("Evolution start: " + Calendar.getInstance().getTime());

	long time = System.currentTimeMillis();
	int cycle = 0;
	while (true)
	{
	    if (it >= ITER_MAX)
	    {
		break;
	    }

	    appendMessage("Starting iteration[" + it + "]");
	    cycle = 0;

	    final int iteration = it;
	    while (population.size() > 1 && cycle < maxSize)
	    {
		final int finalCycle = cycle;
		population.parallelStream().forEach(unit -> oneStep(navValues,
			dividends, population, iteration, finalCycle, unit));
		cycle++;

	    }

	    int winner_size = (int) (0.02 * POPULATION_SIZE);
	    List<SimpleUnit> winners = getWinners(cycle - 1, navValues,
		    population, winner_size > 10 ? 10 : winner_size);

	    results.add(it, winners.get(0).netAssetValue(cycle - 1, navValues));

	    final int day = cycle - 1;
	    winners.parallelStream()
		    .forEach(unit -> unit.logToFile(iteration, day, navValues));

	    // TODO: uz mam winner-a, mozno pouzit jeho??
	    crossoverPopulation(navValues, population, winners, it, cycle);
	    population.addAll(winners);

	    it++;

	    population.parallelStream().forEach(unit -> {
		unit.cash = 300.0f;
		unit.etfShares.clear();
		unit.initShares();
	    });

	    printMemoryUsage(it);
	}

	time = System.currentTimeMillis() - time;
	appendMessage("Simulation end: " + Calendar.getInstance().getTime());
	appendMessage("Simulation took: " + time + "[ms]");

	printMemoryUsage(it);

	logResultsToFile(results);

    }

    private static void crossoverPopulation(float[][] navValues,
	    List<SimpleUnit> population, List<SimpleUnit> winners, int it,
	    int cycle)
    {
	Integer mutated = 0;
	Integer crossover = 0;
	population.clear();
	while (population.size() != POPULATION_SIZE - winners.size())
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
		    secondChosen = Uniform.staticNextIntFromTo(0,
			    winners.size() - 1);
		}
	    }

	    SimpleUnit first = winners.get(firstChosen);
	    SimpleUnit second = winners.get(secondChosen);
	    SimpleUnit newUnit = first.crossOver(second);
	    crossover++;

	    if (MUTATE >= Uniform.staticNextFloatFromTo(0.0f, 1.0f))
	    {
		newUnit.mutate();
		mutated++;
	    }

	    population.add(newUnit);
	}

	StringBuilder builder = new StringBuilder();
	builder.append('[');
	builder.append(cycle);
	builder.append("]Number of crossover:");
	builder.append(crossover);
	appendMessage(builder.toString());

	builder = new StringBuilder();
	builder.append('[');
	builder.append(cycle);
	builder.append("]Number of mutated:");
	builder.append(mutated);
	appendMessage(builder.toString());

    }

    private static void initPopulation(int eftSize, List<SimpleUnit> population)
    {

	for (int i = 0; i < POPULATION_SIZE; i++)
	{
	    population.add(new SimpleUnit(eftSize));
	}
    }

    private static void oneStep(float[][] navValues, float[][] dividends,
	    List<SimpleUnit> units, int it, int cycle, SimpleUnit unit)
    {
	if (cycle > 0 && cycle % INVESTMENT_PERIOD == 0)
	{
	    unit.cash += INVESTMENT;
	}

	unit.doAction(cycle, navValues);
	unit.calculateDividends(dividends, cycle);

    }

    private static List<SimpleUnit> getWinners(int cycle, float[][] etfValueMap,
	    List<SimpleUnit> epoch, int num)
    {

	List<SimpleUnit> chosenList = new ArrayList<SimpleUnit>(num);
	Map<Float, List<SimpleUnit>> map = new HashMap<>(num);

	for (SimpleUnit unit : epoch)
	{
	    Float key = Float.valueOf(unit.netAssetValue(cycle, etfValueMap));
	    if (map.containsKey(key))
	    {
		map.get(key).add(unit);
	    } else
	    {
		List<SimpleUnit> u = new LinkedList<>();
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

    private static float[] getEtfValueMap(List<ETF> loadedETFS, int cycle,
	    Date startDate, boolean dividends)
    {
	float[] etfValueMap = new float[loadedETFS.size()];
	Arrays.fill(etfValueMap, 0.0f);

	DateTime start = new DateTime(startDate.getTime());
	DateTime actual = start.plusDays(cycle);
	for (ETF etf : loadedETFS)
	{
	    int key = ETFMap.getInstance(loadedETFS.size())
		    .getIndex(etf.getTicket());

	    float nav = 0.0f;

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
			nav = navData.getNav();
			break;
		    }
		}
	    }
	    if (nav == 0.0f)
	    {
		continue;
	    }

	    etfValueMap[key] = nav;
	}

	return etfValueMap;
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

    private static void printMemoryUsage(int iteration)
    {
	System.out.println("Total used[" + iteration + "]:"
		+ Runtime.getRuntime().totalMemory());
	System.out.println(
		"Free[" + iteration + "]:" + Runtime.getRuntime().freeMemory());
	System.out.println("Max available[" + iteration + "]:"
		+ Runtime.getRuntime().maxMemory());
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
}
