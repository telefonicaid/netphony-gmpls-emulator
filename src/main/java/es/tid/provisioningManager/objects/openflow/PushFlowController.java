package es.tid.provisioningManager.objects.openflow;

import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.json.JSONObject;

public class PushFlowController {
	
	StaticFlow flow = new StaticFlow();
	
	
	String directory;
	String url;
	Logger log=LoggerFactory.getLogger("PushFlow");

	public PushFlowController(StaticFlow flow,String ip, String port)
	{
		this.flow = flow;
	}


	public void sendRequest()
	{
		log.info("Sending request to controller at "+ url + " with content" + generateJSON().toString());
		//Call ALTO Server Ep
		ByteArrayBuffer inputStream;
		ContentExchange contentExchange = new ContentExchange();
		try 
		{
			inputStream = new ByteArrayBuffer(generateJSON().toString(), "UTF-8");				
			contentExchange.setRequestContent(inputStream);
			contentExchange.setURL(url);
			contentExchange.setMethod(HttpMethods.POST);
		} 
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpClient httpClient = new HttpClient();
		//set up httpClient
		try 
		{
			httpClient.start();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		try 
		{
			httpClient.send(contentExchange);
			contentExchange.waitForDone();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("Response status: "+ contentExchange.getResponseStatus());
		try 
		{
			System.err.print(contentExchange.getResponseContent());
		} 
		catch (UnsupportedEncodingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}


	//To be implemented for each controller
	public JSONObject generateJSON()
	{
		return null;
	}
}
