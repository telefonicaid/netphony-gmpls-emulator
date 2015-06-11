package tid.pce.client.tester;

import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import tid.emulator.node.transport.EmulatedPCCPCEPSession;
import tid.netManager.NetworkLSPManager;
import tid.pce.client.emulator.AutomaticTesterStatistics;
import tid.vntm.LigthPathManagement;

/**
 * Interface to describe an activity you can simulate in the client. The posible activities are:
 * - NetworkEmulator
 * - VNTM
 * @author mcs
 *
 */
public interface Activity extends Runnable{
	
	public void addVNTMSession(EmulatedPCCPCEPSession VNTMSession);
	public void addNetworkEmulator(NetworkLSPManager networkLSPManager);
	public void addStatistics(AutomaticTesterStatistics stats);
	public void addRequest(PCEPRequest request);
	public void addResponse(PCEPResponse response);
	public void addPCEsessionVNTM(EmulatedPCCPCEPSession vNTMSession);
	public void addLigthPathManagement(LigthPathManagement ligthPathManagement);
	

}
