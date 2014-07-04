package tid.emulator.node;

import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.logging.Logger;

import tid.emulator.node.transport.lsp.LSPManager;
import tid.pce.client.emulator.AutomaticTesterStatistics;
import tid.pce.pcepsession.PCEPSessionsInformation;

public class RemoteLSPInitPCEPSessionServer implements Runnable {
	
	private Logger log;
	private Inet4Address idRoadm;
	private LSPManager lspManager;
	private int nodeTechnology;
	private AutomaticTesterStatistics stats;
	private boolean isStateful;
	
	public RemoteLSPInitPCEPSessionServer(LSPManager lspManager, Inet4Address idRoadm, int nodeTechnology, boolean isStateful)
	{
		log=Logger.getLogger("ROADM");
		this.idRoadm=idRoadm;
		this.lspManager=lspManager;
		this.nodeTechnology=nodeTechnology;
		this.isStateful = isStateful;
	}

	@Override
	public void run() {
		    ServerSocket serverSocket = null;
		    boolean listening=true;
			try {
		      	  log.info("Listening PCEP on port 4189");	
		          serverSocket = new ServerSocket(4189);
			}
			catch (Exception e){
				log.severe("Could not listen fast PCEP on port 4189");
				e.printStackTrace();
				return;
			}
			try {
		       	while (listening) {
		       		//Socket s, LSPManager lspManager, Inet4Address idRoadm, PCEPSessionsInformation pcepSessionManager)
		       		log.info("New PCEP Session Open with Client!");
		       		PCEPSessionsInformation pceSessionInf = new PCEPSessionsInformation();
		       		pceSessionInf.setStateful(isStateful);
		       		log.info("Session is stateful ? :"+ isStateful);
		       		new RemoteLSPInitPCEPSession(serverSocket.accept(), lspManager, idRoadm, pceSessionInf).start();	
		       	}
		       	serverSocket.close();
		    } catch (Exception e) {
		       	e.printStackTrace();
		    }				

	}

}
