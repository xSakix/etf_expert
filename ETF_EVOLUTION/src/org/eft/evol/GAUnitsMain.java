
package org.eft.evol;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eft.evol.model.ETFMap;
import org.eft.evol.model.Unit;
import org.eft.evol.model.UnitGAmpl;
import org.eft.evol.model.UnitSequenceGenerator;
import org.etf.provider.ConfigProvider;
import org.joda.time.DateTime;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;

import cern.jet.random.Uniform;

public class GAUnitsMain
{

    private static final float INVESTMENT = 300.0f;
    private static final int POPULATION_SIZE = 100;
    private static final float MUTATE = 0.05f;
    private static final int ITER_MAX = 100;
    private static final int INVESTMENT_PERIOD = 30;
    // private static final int REBALANCE_PERIOD = 90;
    private static final int NUM_CHOSEN = 20;

    private static final File LOG = new File(getSimulationFileName());
    private File RESULTS_CSV = new File(getResultCSVFileName(0));
    private List<ETF> loadedETFS;
    private float[][] navValues;
    private float[][] dividends;
    private List<Unit> population = new ArrayList<>(POPULATION_SIZE);
    private List<Float> results = new ArrayList<Float>();
    private List<String> chosenETFs = new ArrayList<>(NUM_CHOSEN);
    private int simulationDays;

    private static String getSimulationFileName()
    {
	StringBuilder builder = new StringBuilder();

	builder.append(ConfigProvider.DIR);
	builder.append("simulation.log");

	return builder.toString();
    }

    private String getResultCSVFileName(int num)
    {
	StringBuilder builder = new StringBuilder();

	builder.append(ConfigProvider.DIR);
	builder.append(ConfigProvider.DATE_STR);
	if (num == 0)
	{
	    builder.append("_results.csv");
	} else
	{
	    builder.append(String.format("_results_%d.csv", num));

	}

	return builder.toString();
    }

    public static void main(String[] args) throws IOException
    {
	GAUnitsMain gaUnitsMain = new GAUnitsMain();
	gaUnitsMain.run(args);
    }

    public void run(String[] args) throws IOException
    {

	this.loadedETFS = FileLoader.loadAllUSD(0);

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

	this.simulationDays = maxETFByRange.getNavDataList().size();
	Date start = maxETFByRange.getNavDataList().get(0).getDate();
	Date finish = maxETFByRange.getNavDataList()
		.get(this.simulationDays - 1).getDate();
	System.out.println(
		"Starting at " + start + " and finishing at " + finish);

	if (!Files.exists(LOG.toPath()))
	{
	    LOG.delete();
	}

	appendMessage("Loaded " + loadedETFS.size() + " etfs.");
	appendMessage("Rate of mutation: " + MUTATE);
	appendMessage("Iter max:" + ITER_MAX);
	appendMessage("Investment period:" + INVESTMENT_PERIOD);
	appendMessage("Investment money:" + INVESTMENT);
	appendMessage("Population size:" + POPULATION_SIZE);

	navValues = new float[this.simulationDays][loadedETFS.size()];
	dividends = new float[this.simulationDays][loadedETFS.size()];

	for (int index = 0; index < this.simulationDays; index++)
	{
	    navValues[index] = getEtfValueMap(index, start, false);
	    dividends[index] = getEtfValueMap(index, start, true);
	}

	int size = loadedETFS.size();
	loadedETFS.clear();
	loadedETFS = null;
	// lamerske uvolnenie pameti:D
	Runtime.getRuntime().gc();

	experiment(size);
    }

    @SuppressWarnings("unused")
    private void printMemoryUsage(int iteration)
    {
	System.out.println("Total used[" + iteration + "]:"
		+ Runtime.getRuntime().totalMemory());
	System.out.println(
		"Free[" + iteration + "]:" + Runtime.getRuntime().freeMemory());
	System.out.println("Max available[" + iteration + "]:"
		+ Runtime.getRuntime().maxMemory());
    }

    private void experiment(int etfSize)
    {
	Unit lastWinner = null;
	int[] etfs = null;
	while (chosenETFs.size() != NUM_CHOSEN)
	{

	    initPopulation(etfSize);
	    int it = 0;
	    appendMessage(
		    "Evolution start: " + Calendar.getInstance().getTime());
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
		while (population.size() > 1 && cycle < this.simulationDays)
		{
		    final int finalCycle = cycle;
		    population.parallelStream().forEach(
			    unit -> oneStep(iteration, finalCycle, unit));
		    cycle++;

		}

		int winner_size = (int) (0.1 * POPULATION_SIZE);
		List<Unit> winners = getWinners(cycle - 1,
			winner_size > 10 ? 10 : winner_size);

		lastWinner = winners.get(0);
		etfs = lastWinner.getEtfs();
		results.add(it,
			lastWinner.netAssetValue(cycle - 1, navValues));

		winners.parallelStream()
			.forEach(unit -> unit.logToFile(iteration));

		// TODO: uz mam winner-a, mozno pouzit jeho??
		crossoverPopulation(winners, it, cycle);
		population.addAll(winners);

		it++;

		population.parallelStream().forEach(unit -> {
		    unit.resetAssets(0);
		    unit.resetLogs();
		});

	    }
	    if(lastWinner == null || etfs == null){
		throw new RuntimeException("Winner missing!");
	    }
	    
	    
	    // hm, asi skusit, ze vyber 10 naj na zaklade pref
	    // pre ne pozri etf-shares a vyber take co ma naj shares?
	    // zjednodusene, vyber naj pocet share(pametaj index)
	    // a pre ne to bacni, ze to je top a basta
	    int max = 0;
	    int indexMax = 0;
	    for (int i = 0; i < etfs.length; i++)
	    {
		if (etfs[i] > max)
		{
		    indexMax = i;
		    max = etfs[i];
		}
	    }

	    chosenETFs.add(ETFMap.getInstance().getEtfName(indexMax));
	    ETFMap.getInstance().removeIndex(indexMax);
	    etfSize--;

	    float[][] copyNav = new float[navValues.length][navValues[0].length];
	    float[][] copyDiv = new float[navValues.length][navValues[0].length];
	    for (int i = 0; i < navValues.length; i++)
	    {
		copyNav[i] = Arrays.copyOf(navValues[i], navValues[i].length);
		copyDiv[i] = Arrays.copyOf(dividends[i], dividends[i].length);
	    }
	    navValues = new float[this.simulationDays][etfSize];
	    dividends = new float[this.simulationDays][etfSize];

	    for (int i = 0; i < simulationDays; i++)
	    {
		for (int etf_index = 0; etf_index < etfSize; etf_index++)
		{
		    if (etf_index < indexMax)
		    {
			navValues[i][etf_index] = copyNav[i][etf_index];
			dividends[i][etf_index] = copyDiv[i][etf_index];
		    } else
		    {
			navValues[i][etf_index] = copyNav[i][etf_index + 1];
			dividends[i][etf_index] = copyDiv[i][etf_index + 1];
		    }
		}
	    }
	    
	    time = System.currentTimeMillis() - time;
	    appendMessage(
		    "Simulation end: " + Calendar.getInstance().getTime());
	    appendMessage("Simulation took: " + time + "[ms]");

	    logResultsToFile(results);
	    this.population.clear();
	    this.results.clear();
	    
	    RESULTS_CSV = new File(getResultCSVFileName(chosenETFs.size()));
	}
	appendMessage("Chosen etf:");
	for (String etfName : chosenETFs)
	{
	    appendMessage(etfName);
	}

    }

    private void initPopulation(int eftSize)
    {
	for (int i = 0; i < POPULATION_SIZE; i++)
	{
	    population
		    .add(new UnitGAmpl(eftSize, UnitSequenceGenerator.getID()));
	}
    }

    private void logResultsToFile(List<Float> results)
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

    @SuppressWarnings("unused")
    private boolean was90PercentReached(int cycle)
    {

	List<Unit> winners = getWinners(cycle, 1);
	if (winners == null || winners.size() == 0)
	{
	    appendMessage("Winner[" + cycle + "]: not found!");
	    return false;
	}

	Unit winner = winners.get(0);
	appendMessage("Winner[" + cycle + "]: " + winner.getID());

	int counter = 0;

	for (int etf_index = 0; etf_index < winner
		.getBuyPreferenceMap().length; etf_index++)
	{
	    if (winner.getBuyPreferenceMap()[etf_index] > 98)
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

    private void crossoverPopulation(List<Unit> winners, int it, int cycle)
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

	    Unit first = winners.get(firstChosen);
	    Unit second = winners.get(secondChosen);
	    Unit newUnit = first.crossOver(second);
	    crossover++;

	    if (MUTATE >= Uniform.staticNextFloatFromTo(0.0f, 1.0f))
	    {
		newUnit.mutate();
		mutated++;
	    }

	    newUnit.addCash(first.getInvesment() - Unit.BASE_CASH);

	    population.add(newUnit);
	    newUnit.appendHistory(it, cycle, navValues);
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

    private void oneStep(int it, int cycle, Unit unit)
    {
	if (cycle > 0 && cycle % INVESTMENT_PERIOD == 0)
	{
	    unit.addCash(INVESTMENT);
	}

	performAction(it, cycle, unit);

	calculateDividends(it, cycle, unit);

	unit.appendHistory(it, cycle, navValues);
    }

    private void calculateDividends(int it, int cycle, Unit unit)
    {
	unit.calculateDividends(dividends, cycle);
    }

    private void performAction(int it, int cycle, Unit unit)
    {
	unit.performAction(navValues, it, cycle);
    }

    private float[] getEtfValueMap(int cycle, Date startDate, boolean dividends)
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

    private enum ChosenType
    {
	Winner, Victim
    }

    private List<Unit> getWinners(int cycle, int num)
    {
	return getChosen(cycle, num, ChosenType.Winner);
    }

    private List<Unit> getChosen(int cycle, int num, ChosenType chosenType)
    {
	List<Unit> chosenList = new ArrayList<Unit>(num);
	Map<Float, List<Unit>> map = new HashMap<>(num);

	for (Unit unit : population)
	{
	    Float key = Float.valueOf(unit.netAssetValue(cycle, navValues));
	    if (map.containsKey(key))
	    {
		map.get(key).add(unit);
	    } else
	    {
		List<Unit> u = new LinkedList<>();
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

}
