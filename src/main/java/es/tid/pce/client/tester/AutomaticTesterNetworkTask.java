
package es.tid.pce.client.tester;


import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.emulator.node.transport.EmulatedPCCPCEPSession;
import es.tid.pce.client.ClientRequestManager;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMonReq;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;


public class AutomaticTesterNetworkTask  extends TimerTask {

	private ClientRequestManager crm;
	private Logger log;
	private boolean PCMonReqBool;
	static long requestID=123;

	/*Variable used for counter how many requests there are*/

	PCEPMessage request;
	AutomaticTesterNetworkTask(PCEPMessage request,EmulatedPCCPCEPSession ps,boolean PCMonReqBool){
		this.request = request;
		log=LoggerFactory.getLogger("PCCClient");
		this.crm=ps.crm;
		this.PCMonReqBool = PCMonReqBool;
	}
	

	
	@Override
	public void run() {
		log.info("Starting Automatic Client Interface");
		PCEPResponse pr;
		if (PCMonReqBool){
			pr=crm.newRequest((PCEPMonReq)this.request);
		}else {	
			pr=crm.newRequest((PCEPRequest)this.request);
			requestID++;
		}
	}//End run
}
