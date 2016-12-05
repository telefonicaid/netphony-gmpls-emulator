package es.tid.provisioningManager.objects.openflow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.apache.commons.codec.binary.Base64;

import es.tid.util.UtilsFunctions;

public class PushFlowODL extends PushFlowController {

	public PushFlowODL(StaticFlow flow,String ip, String port)
	{
		super(flow,ip,port);
		this.directory = "/controller/nb/v2/flowprogrammer/default/node/OF/";
		this.url="http://"+ip+":"+port+directory + this.flow.getSwitchID()+"/staticFlow/"+this.flow.getName();	
	}

	@Override
	public void sendRequest()
	{
		String credentials = "admin:admin";
		String credentialsEncoded = new Base64().encodeToString(credentials.getBytes());		
		String jsonString = generateJSON().toString();

		try
		{


			URL topoplogyURL = new URL(url);
			URLConnection yc = topoplogyURL.openConnection();

			HttpURLConnection httpcon = (HttpURLConnection) yc;
			httpcon.setDoOutput(true);
			httpcon.setRequestProperty("Content-Type", "application/json");
			httpcon.setRequestProperty("Authorization", "Basic "+credentialsEncoded);				
			httpcon.setRequestMethod("PUT");
			OutputStreamWriter osw = new OutputStreamWriter(httpcon.getOutputStream());
			osw.write(jsonString,0,jsonString.length());
			osw.close();
			System.out.println("url:" +url);
			System.out.println("json:" +jsonString);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							httpcon.getInputStream()));
			String inputLine;

			String response="";
			while ((inputLine = in.readLine()) != null) 
			{
				response = response + inputLine;
			}
			in.close();
			log.info("res: "+response);
		}
			catch(Exception e)
			{
				log.info(UtilsFunctions.exceptionToString(e));		
			}

		
	}



	/**
	 * 

	curl -u admin:admin -H 'Content-type: application/json' -X PUT -d 

	{
	"installInHw":"true", 
	"name":"flowHuko", 
	"node": {
				"id":"00:00:00:1c:c4:d8:35:02", 
				"type":"OF"
			}, 
	"dlSrc": "00:1e:c9:bb:7e:54", 
	"dlDst":"00:1c:c4:da:ba:c2", 
	"vlanId":"349", 
	"priority":"30",
	"actions": [
					"OUTPUT=2"
			   ]
	}

	'http://localhost:8080/controller/nb/v2/flowprogrammer/default/node/OF/00:00:00:1c:c4:d8:35:02/staticFlow/flowHuko'


	 */
	@Override 
	public JSONObject generateJSON(){
		JSONObject requestJSON = new JSONObject();

		requestJSON.put("installInHw","true");
		requestJSON.put("name", this.flow.getName());

		JSONObject nodeJSON = new JSONObject();
		nodeJSON.put("id", this.flow.getSwitchID());
		//TODO: type = OF por defecto??		
		nodeJSON.put("type", "OF");

		requestJSON.put("node",nodeJSON);

		if (this.flow.getIngressPort() != null)	requestJSON.put("ingressPort", this.flow.getIngressPort());
		requestJSON.put("priority", flow.getPriority());		

		if (this.flow.getSrcMAC() != null)	requestJSON.put("dlSrc", this.flow.getSrcMAC());
		if (this.flow.getDstMAC() != null)	requestJSON.put("dlDst", this.flow.getDstMAC());


		if (!this.flow.getVlan_id().equals(StaticFlow.noVlan)) 	requestJSON.put("vlanId", this.flow.getVlan_id());
		if (flow.getVlan_priority() != null) requestJSON.put("vlanPriority", this.flow.getVlan_priority());		


		JSONArray actionsJSON = new JSONArray();
		for (int i = 0; i < this.flow.getPorts().size(); i++)
		{
			String action = "OUTPUT="+ this.flow.getPorts().get(i);
			actionsJSON.add(action);
		}
		requestJSON.put("actions", actionsJSON);


		//TODO: src Mac dst Mac y vlan
		return requestJSON;		
	}

}
