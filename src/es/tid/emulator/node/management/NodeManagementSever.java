package es.tid.emulator.node.management;

import java.net.ServerSocket;
import java.util.logging.Logger;

import es.tid.emulator.node.NetworkNode;

public class NodeManagementSever extends Thread {
	
	private Logger log;
	private NetworkNode node;

		
	public NodeManagementSever(NetworkNode node){
		log =Logger.getLogger("PCEServer");
		this.node=node;
	}

	@Override
	public void run(){
		ServerSocket serverSocket = null;
	    boolean listening=true;
		try {
			log.info("Listening on port 6666");	
	        serverSocket = new ServerSocket(6666);
		}
		catch (Exception e){
			log.severe("Could not listen management on port 6666");
			e.printStackTrace();
			return;
		}
		try {
			while (listening) {
				new NodeManagementSession(serverSocket.accept(), node).start();
	        }
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
	    }
	}
}