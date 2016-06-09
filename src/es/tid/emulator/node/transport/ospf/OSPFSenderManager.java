package es.tid.emulator.node.transport.ospf;

import java.net.Inet4Address;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;
import es.tid.ospf.ospfv2.lsa.LSA;
import es.tid.ospf.ospfv2.lsa.OSPFTEv2LSA;
import es.tid.ospf.ospfv2.lsa.tlv.LinkTLV;
import es.tid.ospf.ospfv2.lsa.tlv.subtlv.LinkID;
import es.tid.ospf.ospfv2.lsa.tlv.subtlv.LinkLocalRemoteIdentifiers;
import es.tid.ospf.ospfv2.lsa.tlv.subtlv.LocalInterfaceIPAddress;
import es.tid.ospf.ospfv2.lsa.tlv.subtlv.RemoteInterfaceIPAddress;
import es.tid.tedb.DomainTEDB;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.SimpleTEDB;

public class OSPFSenderManager {
	
	private DomainTEDB domainTEDB;
	
	public DomainTEDB getDomainTEDB() {
		return domainTEDB;
	}

	public void setDomainTEDB(DomainTEDB domainTEDB) {
		this.domainTEDB = domainTEDB;
	}

	private LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue;
	
	Logger log=LoggerFactory.getLogger("OSPFParser");
	
	public void sendMessageOSPF(Inet4Address src,Inet4Address dst){
		log.info("SEND OSPF");
		
		//changes for multilayer OSPF (UpperLayer and LowerLayer)
		IntraDomainEdge edge = null;
		
		edge=((SimpleTEDB)domainTEDB).getNetworkGraph().getEdge(src, dst);
		OSPFv2LinkStateUpdatePacket ospfv2Packet = new OSPFv2LinkStateUpdatePacket();
		ospfv2Packet.setRouterID(src);
		
		ospfv2Packet.setAreaID(domainTEDB.getReachabilityEntry().getDomainId());
			
		LinkedList<LSA> lsaList = new LinkedList<LSA>();
		OSPFTEv2LSA lsa = new OSPFTEv2LSA();
		LinkTLV linkTLV=new LinkTLV();
		lsa.setLinkTLV(linkTLV);
		lsa.setAdvertisingRouter(src);
/**		linkTLV.setMaximumBandwidth(edge.getTE_info().getMaximumBandwidth());
		linkTLV.setUnreservedBandwidth(edge.getTE_info().getUnreservedBandwidth());
		linkTLV.setMaximumReservableBandwidth(edge.getTE_info().getMaximumReservableBandwidth());
*/		LocalInterfaceIPAddress localInterfaceIPAddress= new LocalInterfaceIPAddress();
		LinkedList<Inet4Address> lista =localInterfaceIPAddress.getLocalInterfaceIPAddressList();
		lista.add(src);
		linkTLV.setLocalInterfaceIPAddress(localInterfaceIPAddress);
		RemoteInterfaceIPAddress remoteInterfaceIPAddress= new RemoteInterfaceIPAddress();
		LinkedList<Inet4Address> listar = remoteInterfaceIPAddress.getRemoteInterfaceIPAddressList();
		listar.add(dst);
		linkTLV.setRemoteInterfaceIPAddress(remoteInterfaceIPAddress);
		LinkLocalRemoteIdentifiers llri= new LinkLocalRemoteIdentifiers();
		llri.setLinkLocalIdentifier(edge.getSrc_if_id());
		llri.setLinkRemoteIdentifier(edge.getDst_if_id());
		linkTLV.setLinkLocalRemoteIdentifiers(llri);
		LinkID linkID = new LinkID();
		linkID.setLinkID(dst);
		linkTLV.setLinkID(linkID);
		if (edge.getTE_info().getAvailableLabels() != null){
			linkTLV.setAvailableLabels(edge.getTE_info().getAvailableLabels());
		}
		else
			log.info("Available Labels es NULL");
		lsaList.add(lsa);

		ospfv2Packet.setLSAlist(lsaList);
		sendingQueue.add(ospfv2Packet);
	}
	
	public LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> getSendingQueue() {
		return sendingQueue;
	}

	public void setSendingQueue(LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue) {
		this.sendingQueue = sendingQueue;
	}
}
