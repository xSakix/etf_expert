package org.eft.evol;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;

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
	List<ETF> loadedETFS = FileLoader.loadAllUSD(0);
	
	for(ETF etf : loadedETFS){
	    System.out.println("Processing:"+etf.getTicket());
	    
	    float norm2 = norm(etf,2.0f);
	    float norm3 = norm(etf,3.0f);
	    float norm4 = norm(etf,4.0f);
	    float norm5 = norm(etf,5.0f);
	    float norm10 = norm(etf,10.0f);
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
	
	for(NavData navData : etf.getNavDataList()){
	    sum += Math.pow(navData.getNav(), 2.0f);
	}
	
	
	float oneUnder = 1.0f/etf.getNavDataList().size();
	double distance = Math.pow(sum,1.0f/2.0f);
	return (float) (oneUnder*distance - Math.pow(mean,1.0f/2.0f));
    }

    private static float mean(ETF etf)
    {
	float sum = 0.0f;
	
	for(NavData navData : etf.getNavDataList()){
	    sum += navData.getNav();
	}
	return sum/(float)etf.getNavDataList().size();
    }

    private static float norm(ETF etf,float p)
    {
	float sum = 0.0f;
	
	for(NavData navData : etf.getNavDataList()){
	    sum += Math.pow(navData.getNav(), p);
	}
	
	return (float) Math.pow(sum, 1/p);
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
