package org.eft.evol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.etf.provider.ConfigProvider;

import com.etfdatadatabase.domain.ETF;
import com.etfdatadatabase.domain.NavData;
import com.etfdatadatabase.loader.FileLoader;


public class NormMain
{

    private static final File RESULT = new File(
	    ConfigProvider.DIR + "result.csv");

    public static void main(String[] args) throws IOException
    {
	List<ETF> loadedETFS = FileLoader.loadAllUSD(10);

	List<ETF> loadedETFStemp = new ArrayList<>(loadedETFS);
	for (ETF etf : loadedETFStemp)
	{
	    if (etf.getNavDataList().size() < ETF.NUM_OF_DAYS / 3)
	    {
		loadedETFS.remove(etf);
	    }
	}

	// computeGeneralNormOfSelf(loadedETFS);

	computeDataNorms(loadedETFS);
	Map<String,List<String>> similar = new HashMap<>();
		
	for(ETF etf : loadedETFS){
	    float[] normalised = etf.normalised();
	    
	    similar.put(etf.getTicket(), new ArrayList<String>());
	    
	    for(ETF etf2 : loadedETFS){
		if(etf.getTicket().equals(etf2.getTicket())){
		    continue;
		}
		
		float[] normalised2 = etf2.normalised();
		
		int index = etf.startIndex;
		if(etf2.startIndex > index){
		    index = etf2.startIndex;
		}
		
		double[] divs = new double[normalised2.length - index];
		for(int i = index,j=0;i<normalised2.length;i++,j++){
		    
		    divs[j] = Math.round(normalised[i]/normalised2[i]);		    
		    //System.out.println(normalised[i]+"/"+normalised2[i]+" = "+divs[j]);
		}
		//System.out.println(etf.getTicket()+"/"+etf2.getTicket());
		List<Double> distinct = Arrays.stream(divs).distinct().boxed().collect(Collectors.toList());
		for(Double val : distinct){
		    long c = Arrays.stream(divs).filter(d -> d == val).count();
		    //System.out.println(String.format("%f (%d)",val,c));
		    if((c/divs.length)*100.0f > 80.0f){
			if(!similar.get(etf.getTicket()).contains(etf2.getTicket())){
			    similar.get(etf.getTicket()).add(etf2.getTicket());
			}
		    }
		}
	    }
	    
	    for(String etfname : similar.keySet()){
		System.out.println(etfname+":");
		similar.get(etfname).stream().forEach(e -> System.out.print(e+","));
		System.out.println();
	    }
	}
	

    }

    private static void computeDataNorms(List<ETF> loadedETFS)
    {
	float[][] norms = new float[loadedETFS.size()][loadedETFS.size()];

	StringBuilder builder = new StringBuilder();
	builder.append(';');
	for (ETF etf : loadedETFS)
	{
	    builder.append(etf.getTicket());
	    builder.append(';');
	}
	builder.append('\n');
	appendMessage(builder.toString());

	for (ETF etf : loadedETFS)
	{
	    System.out.println(String.format("Processing:%s", etf.getTicket()));
	    builder = new StringBuilder();
	    builder.append(etf.getTicket());
	    builder.append(';');
	    int etf_index = loadedETFS.indexOf(etf);
	    for (ETF etf2 : loadedETFS)
	    {
		if (etf.getTicket().equals(etf2.getTicket()))
		{
		    builder.append(';');
		    continue;
		}
		int etf_index2 = loadedETFS.indexOf(etf2);

//		norms[etf_index][etf_index2] = norm(etf.normalised(),
//			etf2.normalised(), etf.startIndex, etf2.startIndex,
//			2.0f);
		float[] etf1Normalised = etf.normalised();
		float[] etf2Normalised = etf2.normalised();
		
		int index = etf.startIndex;
		if(etf2.startIndex > index){
		    index = etf2.startIndex;
		}
		
		float norm1 = normOne(etf1Normalised,index,2.0f);		//System.out.println(norm1);
		float norm2 = normOne(etf2Normalised,index,2.0f);		//System.out.println(norm2);
		
		float[] etf1Array = Arrays.copyOf(etf1Normalised, etf1Normalised.length);
		float[] etf2Array = Arrays.copyOf(etf2Normalised, etf2Normalised.length);
		double cosAngle = 0.0d;
		for(int i = index;i < etf1Array.length;i++){
		    etf1Array[i] = etf1Array[i]/norm1;
		    etf2Array[i] = etf2Array[i]/norm2;
		    
		    cosAngle +=etf1Array[i] * etf2Array[i];
		}
		
//		builder.append(
//			String.format("%.2f;", norms[etf_index][etf_index2]));
		builder.append(
			String.format("%f;", cosAngle));

	    }
	    builder.append('\n');
	    appendMessage(builder.toString());
	}
    }

    private static float normOne(float[] etf, Integer startIndex1, float p)
    {
	float sum = 0.0f;

	int index = 0;
	

	for (int i = startIndex1; i < ETF.NUM_OF_DAYS; i++)
	{
	    sum += Math.pow(Math.abs(etf[i]), p);
	}

	return (float) Math.pow(sum, 1 / p);
    }
    
    private static float norm(float[] etf1, float[] etf2, Integer startIndex1,
	    Integer startIndex2, float p)
    {
	float sum = 0.0f;

	int index = 0;
	if (startIndex1 > startIndex2)
	{
	    index = startIndex1;
	} else
	{
	    index = startIndex2;
	}

	for (int i = index; i < ETF.NUM_OF_DAYS; i++)
	{
	    sum += Math.pow(Math.abs(etf1[i] - etf2[i]), p);
	}

	return (float) Math.pow(sum, 1 / p);
    }

    private static void computeGeneralNormOfSelf(List<ETF> loadedETFS)
    {
	for (ETF etf : loadedETFS)
	{
	    System.out.println("Processing:" + etf.getTicket());

	    float norm2 = norm(etf, 2.0f);
	    float norm3 = norm(etf, 3.0f);
	    float norm4 = norm(etf, 4.0f);
	    float norm5 = norm(etf, 5.0f);
	    float norm10 = norm(etf, 10.0f);
	    float mean = mean(etf);
	    float variance = variance(etf, mean);
	    DecimalFormat formatter = new DecimalFormat("#0.00");
	    StringBuilder builder = new StringBuilder();
	    builder.append(etf.getTicket());
	    builder.append(';');
	    builder.append(formatter.format(norm2));
	    builder.append(';');
	    builder.append(formatter.format(norm3));
	    builder.append(';');
	    builder.append(formatter.format(norm4));
	    builder.append(';');
	    builder.append(formatter.format(norm5));
	    builder.append(';');
	    builder.append(formatter.format(norm10));
	    builder.append(';');
	    builder.append(formatter.format(mean));
	    builder.append(';');
	    builder.append(formatter.format(variance));
	    builder.append('\n');
	    appendMessage(builder.toString());
	}
    }

    private static float variance(ETF etf, float mean)
    {
	float sum = 0.0f;

	for (NavData navData : etf.getNavDataList())
	{
	    sum += Math.pow(navData.getNav(), 2.0f);
	}

	float oneUnder = 1.0f / etf.getNavDataList().size();
	double distance = Math.pow(sum, 1.0f / 2.0f);
	return (float) (oneUnder * distance - Math.pow(mean, 1.0f / 2.0f));
    }

    private static float mean(ETF etf)
    {
	float sum = 0.0f;

	for (NavData navData : etf.getNavDataList())
	{
	    sum += navData.getNav();
	}
	return sum / (float) etf.getNavDataList().size();
    }

    private static float norm(ETF etf, float p)
    {
	float sum = 0.0f;

	for (NavData navData : etf.getNavDataList())
	{
	    sum += Math.pow(navData.getNav(), p);
	}

	return (float) Math.pow(sum, 1 / p);
    }

    public static void appendMessage(String msg)
    {
	if (!msg.endsWith("\n"))
	{
	    msg += "\n";
	}
	if (!Files.exists(RESULT.toPath()))
	{
	    try
	    {
		RESULT.createNewFile();
	    } catch (IOException e)
	    {
		e.printStackTrace();
		return;
	    }
	}
	try
	{
	    Files.write(RESULT.toPath(), msg.getBytes(),
		    StandardOpenOption.APPEND);
	} catch (IOException e)
	{
	    e.printStackTrace();
	}

    }

}
