package es.tid.emulator.node.transport.ospf;

import java.net.Inet4Address;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;
import es.tid.tedb.DomainTEDB;

public class OSPFController {
	private DomainTEDB domainTEDB;
	private OSPFSenderThread ospfSenderThread;
    private Inet4Address nodeID;
    private OSPFSendAllTopology ospfSendAllTopology;
    private OSPFSenderManager ospfSenderManager;

	/**
	 * Queue to read the messages to send to the PCE peer
	 */
	private LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue;

	Logger log=LoggerFactory.getLogger("OSPFParser");
	
	public OSPFController(){
		sendingQueue= new LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket>();
	}
	
	public void configureOSPFController(Inet4Address nodeID, DomainTEDB tedb){
		this.nodeID=nodeID;
		this.domainTEDB=tedb;
	}
	
	public void initialize (){
		ospfSenderThread = new OSPFSenderThread(sendingQueue, nodeID);
		ospfSenderThread.start();
		ospfSenderManager = new OSPFSenderManager();
		ospfSenderManager.setSendingQueue(sendingQueue);
		ospfSenderManager.setDomainTEDB(domainTEDB);
		ospfSendAllTopology = new OSPFSendAllTopology (domainTEDB, ospfSenderManager);	
		ospfSendAllTopology.start();
	}
		
	public DomainTEDB getDomainTEDB() {
		return domainTEDB;
	}

	public void setDomainTEDB(DomainTEDB domainTEDB) {
		this.domainTEDB = domainTEDB;
	}
	
	public LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> getSendingQueue() {
		return sendingQueue;
	}

	public void setSendingQueue(LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue) {
		this.sendingQueue = sendingQueue;
	}
}
