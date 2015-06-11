package tid.emulator.node;

import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.logging.Logger;

import tid.emulator.node.transport.lsp.LSPManager;
import tid.pce.client.emulator.AutomaticTesterStatistics;

public class FastPCEPSessionServer extends Thread {
	
	private Logger log;
	private Inet4Address idRoadm;
	private LSPManager lspManager;
	private int nodeTechnology;
	private AutomaticTesterStatistics stats;
	
	public FastPCEPSessionServer(LSPManager lspManager, Inet4Address idRoadm, int nodeTechnology)
	{
		log=Logger.getLogger("ROADM");
		this.idRoadm=idRoadm;
		this.lspManager=lspManager;
		this.nodeTechnology=nodeTechnology;
	}

	public void run(){
	    ServerSocket serverSocket = null;
	    boolean listening=true;
		try {
	      	  log.info("Listening Fast PCEP on port 2222 ooohhh");	
	          serverSocket = new ServerSocket(2222);
		}
		catch (Exception e){
			log.severe("Could not listen fast PCEP on port 222");
			e.printStackTrace();
			return;
		}
		try {
	       	while (listening) {
	       		log.info("Waiting for connection Fast PCEP");
	       		new FastPCEPSession(serverSocket.accept(), lspManager, idRoadm, nodeTechnology,stats).start();	
	       	}
	       	serverSocket.close();
	    } catch (Exception e) {
	       	e.printStackTrace();
	    }
		log.info("Ending Fast PCEP Session!!");
	}

	public AutomaticTesterStatistics getStats() {
		return stats;
	}

	public void setStats(AutomaticTesterStatistics stats) {
		this.stats = stats;
	}
}
