package tid.provisioningManager.objects.openflow;

import org.json.JSONObject;
import org.json.simple.JSONArray;

public class PushFlowRYU extends PushFlowController {
	
	
	

	public PushFlowRYU(StaticFlow flow,String ip, String port)
	{
		super(flow,ip,port);
		this.directory = "/stats/flowentry/add";
		this.url="http://"+ip+":"+port+directory;	
	}





	@Override
	public JSONObject generateJSON()
	{
		JSONObject requestJSON = new JSONObject();
		Long l16 = Long.parseLong(this.flow.getSwitchID().replace(":",""),16);

		requestJSON.put("dpid", l16.toString());
		requestJSON.put("priority", flow.getPriority());
		requestJSON.put("name", this.flow.getName());


		JSONObject match = new JSONObject();
		match.put("dl_src", flow.getSrcMAC().replace(":", ""));
		match.put("dl_dst", flow.getDstMAC().replace(":", ""));
		match.put("in_port", flow.getIngressPort());
		//match.put("out_port", flow.getIngressPort());
		//match.put("tp_src", flow.getIngressPort());

		requestJSON.put("match", match);

		JSONArray actions = new JSONArray();

		if (!this.flow.getVlan_id().equals(StaticFlow.noVlan))
		{	
			JSONObject vlan = new JSONObject();
			vlan.put("type", "SET_VLAN_VID");
			vlan.put("vlan_vid", this.flow.getVlan_id());
			actions.add(vlan);
		}

		for (int i = 0; i <  this.flow.getPorts().size(); i++) 
		{
			JSONObject port = new JSONObject();
			port.put("type", "OUTPUT");
			port.put("port", this.flow.getPorts().get(i));
			actions.add(port);
		}

		requestJSON.put("actions", actions);

		return requestJSON;
	}
}
