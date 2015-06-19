package tid.emulator.node.transport;

import java.net.Inet4Address;
import java.util.logging.Logger;

import es.tid.pce.client.ClientRequestManager;
import es.tid.pce.client.PCEPClient;
import es.tid.pce.pcepsession.PCEPSessionsInformation;
import es.tid.pce.server.lspdb.ReportDB_Redis;
import tid.emulator.node.transport.lsp.LSPManager;

public class PathComputationClient {
    private PCEPClient clientPCE;
    private EmulatedPCCPCEPSession PCEsession;
    private int OF;

    // Auxiliar para pruebas, crear un pce client
    private EmulatedPCCPCEPSession pceSession;
    private ClientRequestManager crm;
    
    private Logger log;
	
	public PathComputationClient(){
		crm = new ClientRequestManager();
		clientPCE = new PCEPClient();
		log = Logger.getLogger("ROADM");
	}
	
	 public void addPCE(boolean manually, Inet4Address pceAddress, int pcepPort, boolean setStateful, boolean setActive, LSPManager lspManager,boolean setSRCapable,int MSD){
			log.info("Adding PCE");
			int pcepport = Integer.valueOf(pcepPort).intValue();
			PCEPSessionsInformation pcepSessionsInformation = new PCEPSessionsInformation();
			pcepSessionsInformation.setStateful(setStateful);
			pcepSessionsInformation.setStateful(setActive);
			pcepSessionsInformation.setSRCapable(setSRCapable);
			pcepSessionsInformation.setMSD(MSD);
			
			
			EmulatedPCCPCEPSession PCEsession = new EmulatedPCCPCEPSession(pceAddress.getCanonicalHostName(), pcepport,false,pcepSessionsInformation,lspManager);
			this.setPceSession(PCEsession);
			this.setCrm(PCEsession.crm);
			lspManager.setPCESession(PCEsession);
			PCEsession.start();
	}

	 public void addPCE(boolean manually, Inet4Address pceAddress, int pcepPort, 
			 			boolean setStateful, boolean setActive, boolean statefulDFlag, boolean statefulTFlag, boolean statefulSFlag, LSPManager lspManager,
			 			boolean setSRCapable,int MSD){
		 
			log.info("Adding PCE");
			int pcepport = Integer.valueOf(pcepPort).intValue();
			PCEPSessionsInformation pcepSessionsInformation = new PCEPSessionsInformation();
			pcepSessionsInformation.setStateful(setStateful);
			pcepSessionsInformation.setActive(setActive);
			pcepSessionsInformation.setStatefulDFlag(statefulDFlag);
			pcepSessionsInformation.setStatefulTFlag(statefulTFlag);
			pcepSessionsInformation.setStatefulSFlag(statefulSFlag);
			
			
			pcepSessionsInformation.setSRCapable(setSRCapable);
			pcepSessionsInformation.setMSD(MSD);
					
			EmulatedPCCPCEPSession PCEsession = new EmulatedPCCPCEPSession(pceAddress.getCanonicalHostName(), pcepport,false,pcepSessionsInformation,lspManager);
			this.setPceSession(PCEsession);
			this.setCrm(PCEsession.crm);
			lspManager.setPCESession(PCEsession);
			PCEsession.start();
	}	 
	 
	 
	 
	 
	 
	public PCEPClient getClientPCE() {
		return clientPCE;
	}

	public void setClientPCE(PCEPClient clientPCE) {
		this.clientPCE = clientPCE;
	}

	public EmulatedPCCPCEPSession getPCEsession() {
		return PCEsession;
	}

	public void setPCEsession(EmulatedPCCPCEPSession pCEsession) {
		PCEsession = pCEsession;
	}

	public int getOF() {
		return OF;
	}

	public void setOF(int oF) {
		OF = oF;
	}

	public EmulatedPCCPCEPSession getPceSession() {
		return pceSession;
	}

	public void setPceSession(EmulatedPCCPCEPSession pceSession) {
		this.pceSession = pceSession;
	}

	public ClientRequestManager getCrm() {
		return crm;
	}

	public void setCrm(ClientRequestManager crm) {
		this.crm = crm;
	}
}
