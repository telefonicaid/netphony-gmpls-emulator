/*
 * PCEP KeepAlive management Thread
 * 
 * Carlos Garcia Argos (cgarcia@novanotio.es)
 * Feb. 11 2010
 */

package tid.emulator.node.transport.ospf;

import java.net.Inet4Address;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import es.tid.tedb.DomainTEDB;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.SimpleTEDB;

public class OSPFSendAllTopology extends Thread {

	private boolean running;
	//private Roadm roadm;
	private DomainTEDB domainTEDB;
	private OSPFSenderManager ospfSenderManager;
	private Logger log;

	public OSPFSendAllTopology(DomainTEDB domainTEDB, OSPFSenderManager ospfsenderManager) {
		//this.roadm=roadm;
		this.domainTEDB=domainTEDB;
		this.ospfSenderManager=ospfsenderManager;
		log = Logger.getLogger("OSPFParser");
	}
	/**
	 * Starts the keepAliveLSP process
	 */
	
	public void run(){
		running=true;
		while (running) {
			try {
				sleep(10000); // send all topology every 10 seconds
				log.info("SEND ALL TOPOLOGY");
				SimpleDirectedWeightedGraph<Object,IntraDomainEdge> networkGraph = ((SimpleTEDB)domainTEDB).getNetworkGraph();
				Set<IntraDomainEdge> edgeSet= networkGraph.edgeSet();
				Iterator <IntraDomainEdge> edgeIterator=edgeSet.iterator();
				while (edgeIterator.hasNext()){
					IntraDomainEdge edge= edgeIterator.next();
					ospfSenderManager.sendMessageOSPF((Inet4Address)edge.getSource(),(Inet4Address)edge.getTarget());
				}	
			} catch (InterruptedException e) {
				if (running==false){
					log.info("Ending SendAllTopology - Thread");
					return;
				}
				else {
					//Keep-alive Timer is reseted
					log.info("Reseting SendAllTopology timer");
				}
			}
		}
	}
	/**
	 * Sets the running variable to false. After this, an interrupt will cause 
	 * the KeepaliveThread to end.
	 */
	public void stopRunning(){
		running=false;
	}
}
