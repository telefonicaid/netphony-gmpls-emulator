package tid.provisioningManager.objects.openflow;

import org.json.JSONObject;

public class PushFlowFloodlight extends PushFlowController {
	

	public PushFlowFloodlight(StaticFlow flow,String ip, String port)
	{
		super(flow,ip,port);
		this.directory = "/wm/staticflowentrypusher/json";
		this.url="http://"+ip+":"+port+directory;	
	}




	@Override
	public JSONObject generateJSON()
	{
		log.info("inside generateJSON");
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("switch", this.flow.getSwitchID());
		requestJSON.put("name", this.flow.getName());
		requestJSON.put("priority", flow.getPriority());


		if (this.flow.getIngressPort() != null)
		{
			requestJSON.put("ingress-port", this.flow.getIngressPort());
		}

		requestJSON.put("active", this.flow.getActive());

		if (this.flow.getSrcMAC() != null)
		{
			requestJSON.put("src-mac", this.flow.getSrcMAC());
		}

		if (this.flow.getDstMAC() != null)
		{
			requestJSON.put("dst-mac", this.flow.getDstMAC());
		}

		String actions = "";

		if (!this.flow.getVlan_id().equals(StaticFlow.noVlan))
		{
			actions = "set-vlan-id=" + this.flow.getVlan_id() + ",";
		}

		for (int i = 0; i < this.flow.getPorts().size(); i++)
		{
			if (i == 0)
			{
				actions += "output=" + this.flow.getPorts().get(i);
			}
			else
			{
				actions += ",output=" + this.flow.getPorts().get(i);
			}
		}

		requestJSON.put("actions", actions);

		if (flow.getVlan_priority() != null)
		{
			requestJSON.put("vlan-priority", this.flow.getVlan_priority());
		}
		//requestJSON.put("vlan-id", this.flow.getVlan_id());
		return requestJSON;
	}
}
