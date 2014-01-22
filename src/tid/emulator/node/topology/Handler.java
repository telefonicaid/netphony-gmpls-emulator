package tid.emulator.node.topology;

import java.net.Inet4Address;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import tid.netManager.emulated.LayerTypes;
import tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;
import tid.ospf.ospfv2.lsa.LSA;
import tid.ospf.ospfv2.lsa.OSPFTEv2LSA;
import tid.ospf.ospfv2.lsa.tlv.LinkTLV;
import tid.ospf.ospfv2.lsa.tlv.subtlv.LinkID;
import tid.ospf.ospfv2.lsa.tlv.subtlv.LocalInterfaceIPAddress;
import tid.ospf.ospfv2.lsa.tlv.subtlv.RemoteInterfaceIPAddress;
import tid.pce.tedb.DomainTEDB;
import tid.pce.tedb.IntraDomainEdge;
import tid.pce.tedb.MultiLayerTEDB;

/**
 * Class which reads a topology network from an XML file, and sends it by OSPF.
 * @author mcs
 *
 */
public class Handler {
	//La topologia TEDB
	DomainTEDB  ted;
	
	/**
	 * Metodo que lee la topologia de un XML file
	 */
	DomainTEDB  XMLRead(String file){
		ted = new MultiLayerTEDB();
		if (file !=null){		
			ted.initializeFromFile(file);
		}		
		return ted;
	}
	/**
	 * Metodo que envia por OSPF la topologia.
	 * @param sendingQueue Cola de env�o de mensajes OSPF
	 */
	void sendTopology(LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue){
		int layer;
		//UPPER_LAYER
		SimpleDirectedWeightedGraph<Object,IntraDomainEdge> graphIP = ((MultiLayerTEDB)ted).getUpperLayerGraph();
		if (graphIP != null){
			Set<IntraDomainEdge> edgeSet= graphIP.edgeSet();
			Iterator <IntraDomainEdge> edgeIterator=edgeSet.iterator();
			layer = LayerTypes.UPPER_LAYER;
			while (edgeIterator.hasNext()){
				IntraDomainEdge edge= edgeIterator.next();
				sendMessageOSPF(sendingQueue,(Inet4Address)edge.getSource(),(Inet4Address)edge.getTarget(), layer);
			}
		}

		//LOWER LAYER
		SimpleDirectedWeightedGraph<Object,IntraDomainEdge> graphOP = ((MultiLayerTEDB)ted).getLowerLayerGraph();
		if (graphOP != null){
			Set<IntraDomainEdge> edgeSet1= graphOP.edgeSet();
			Iterator <IntraDomainEdge> edgeIterator1=edgeSet1.iterator();
			layer = LayerTypes.LOWER_LAYER;
			while (edgeIterator1.hasNext()){
				IntraDomainEdge edge= edgeIterator1.next();
				sendMessageOSPF(sendingQueue,(Inet4Address)edge.getSource(),(Inet4Address)edge.getTarget(), layer);

			}
		}

	}
	/**
	 * Metodo que mete en la cola de envio de OSPF un paquete
	 * @param sendingQueue Cola de envio de mensajes OSPF
	 * @param src origen del link que estamos informando
	 * @param dst destino del link que estamos informando
	 * @param layer layer del link que estamos informando
	 */
	private void sendMessageOSPF(LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue, Inet4Address src,Inet4Address dst,  int layer){

		IntraDomainEdge edge = null;		
		edge=((MultiLayerTEDB)ted).getLowerLayerGraph().getEdge(src, dst);		
		OSPFv2LinkStateUpdatePacket ospfv2Packet = new OSPFv2LinkStateUpdatePacket();
		ospfv2Packet.setRouterID(src);
		LinkedList<LSA> lsaList = new LinkedList<LSA>();
		OSPFTEv2LSA lsa = new OSPFTEv2LSA();
		

		LinkTLV linkTLV=new LinkTLV();
		lsa.setLinkTLV(linkTLV);
		linkTLV.setMaximumBandwidth(edge.getTE_info().getMaximumBandwidth());
		if (edge.getTE_info().getUnreservedBandwidth() != null)
			linkTLV.setUnreservedBandwidth(edge.getTE_info().getUnreservedBandwidth());
		if (layer == LayerTypes.UPPER_LAYER){	
			linkTLV.setMaximumReservableBandwidth(edge.getTE_info().getMaximumReservableBandwidth());
		}
		
		LocalInterfaceIPAddress localInterfaceIPAddress= new LocalInterfaceIPAddress();
		LinkedList<Inet4Address> lista =localInterfaceIPAddress.getLocalInterfaceIPAddressList();
		lista.add(src);
		linkTLV.setLocalInterfaceIPAddress(localInterfaceIPAddress);
		RemoteInterfaceIPAddress remoteInterfaceIPAddress= new RemoteInterfaceIPAddress();
		LinkedList<Inet4Address> listar = remoteInterfaceIPAddress.getRemoteInterfaceIPAddressList();
		listar.add(dst);
		linkTLV.setRemoteInterfaceIPAddress(remoteInterfaceIPAddress);
		LinkID linkID = new LinkID();
		linkID.setLinkID(dst);
		linkTLV.setLinkID(linkID);
		if (edge.getTE_info().getAvailableLabels() != null){
			linkTLV.setAvailableLabels(edge.getTE_info().getAvailableLabels());		
		}
		lsaList.add(lsa);
		ospfv2Packet.setLSAlist(lsaList);
		sendingQueue.add(ospfv2Packet);

	}
}
