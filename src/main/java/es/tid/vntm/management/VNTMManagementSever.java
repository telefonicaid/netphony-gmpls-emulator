package es.tid.vntm.management;

import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.vntm.LSPManager;

public class VNTMManagementSever extends Thread {
	
	private Logger log;
	private LSPManager lspmanager;
	int port;
	public VNTMManagementSever( LSPManager lspmanager,int port){
		log =LoggerFactory.getLogger("PCEServer");
		this.lspmanager=lspmanager;
	}
	
	public void run(){
	    ServerSocket serverSocket = null;
	    boolean listening=true;
		try {
	      	  log.info("Listening on port "+port);	
	          serverSocket = new ServerSocket(port);
		  }
		catch (Exception e){
			 log.error("Could not listen management on port "+port);
			e.printStackTrace();
			return;
		}
		
		   try {
	        	while (listening) {
	        		new VNTMManagementSession(serverSocket.accept(),lspmanager).start();
	        	}
	        	serverSocket.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }				
	}
	  
}
