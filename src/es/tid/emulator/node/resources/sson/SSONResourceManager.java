package es.tid.emulator.node.resources.sson;

import java.net.Inet4Address;
import java.util.Hashtable;
import java.util.logging.Logger;

import es.tid.emulator.node.resources.ResourceManager;
import es.tid.emulator.node.tedb.SimpleLocalTEDB;
import es.tid.emulator.node.transport.EmulatedPCCPCEPSession;
import es.tid.emulator.node.transport.lsp.LSPKey;
import es.tid.emulator.node.transport.lsp.te.LSPTE;
import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.objects.BandwidthRequested;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.ObjectiveFunction;
import es.tid.pce.pcep.objects.RequestParameters;
import es.tid.pce.pcep.objects.Reservation;
import es.tid.rsvp.RSVPProtocolViolationException;
import es.tid.rsvp.constructs.te.FFFlowDescriptorTE;
import es.tid.rsvp.constructs.te.SenderDescriptorTE;
import es.tid.rsvp.messages.RSVPMessageTypes;
import es.tid.rsvp.messages.RSVPPathTearMessage;
import es.tid.rsvp.messages.te.RSVPTEPathMessage;
import es.tid.rsvp.messages.te.RSVPTEResvMessage;
import es.tid.rsvp.objects.ERO;
import es.tid.rsvp.objects.FilterSpecLSPTunnelIPv4;
import es.tid.rsvp.objects.FlowSpec;
import es.tid.rsvp.objects.IntservSenderTSpec;
import es.tid.rsvp.objects.LabelRequestWOLabelRange;
import es.tid.rsvp.objects.RSVPHopIPv4;
import es.tid.rsvp.objects.SenderTemplate;
import es.tid.rsvp.objects.SenderTemplateLSPTunnelIPv4;
import es.tid.rsvp.objects.SessionLSPTunnelIPv4;
import es.tid.rsvp.objects.Style;
import es.tid.rsvp.objects.TimeValues;
import es.tid.rsvp.objects.gmpls.GeneralizedLabel;
import es.tid.rsvp.objects.subobjects.GeneralizedLabelEROSubobject;
import es.tid.rsvp.objects.subobjects.IPv4prefixEROSubobject;
import es.tid.rsvp.objects.subobjects.SubObjectValues;
import es.tid.rsvp.objects.subobjects.UnnumberIfIDEROSubobject;
import es.tid.tedb.MDTEDB;

/**
 * 
 * Class implementing an SSON resource manager
 * 
 * @author Fernando Mu�oz del Nuevo
 *
 */

public class SSONResourceManager implements ResourceManager{

	private SimpleLocalTEDB ted;
	
	private MDTEDB MDted;
	
	private Inet4Address localID;
	
	/**
	 * FIXME: Explicar que hace esta tabla
	 */
	private Hashtable<LSPKey,Integer> M_Allocation;
	private Hashtable<LSPKey,Integer> N_Allocation;
	private Hashtable<LSPKey,Inet4Address> proxHopIPv4List;
	private Hashtable<LSPKey,Inet4Address> previousHopIPv4List;
	private Hashtable<LSPKey,Integer> outputInterfaceNode;
	
	private Logger log;
	
	// Single Domain
	public SSONResourceManager(SimpleLocalTEDB tedb, Inet4Address localID){
		this.ted=tedb;
		this.localID=localID;
		log=Logger.getLogger("ROADM");
		M_Allocation = new Hashtable<LSPKey,Integer>();
		N_Allocation = new Hashtable<LSPKey,Integer>();
		proxHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
		previousHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
		outputInterfaceNode = new Hashtable<LSPKey,Integer>();
	}
	
	//Constructor Multi Domain
	public SSONResourceManager(SimpleLocalTEDB tedb, Inet4Address localID, MDTEDB MDted){
		this.ted=tedb;
		this.MDted=MDted;
		this.localID=localID;
		log=Logger.getLogger("ROADM");
		M_Allocation = new Hashtable<LSPKey,Integer>();
		N_Allocation = new Hashtable<LSPKey,Integer>();
		proxHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
		previousHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
		outputInterfaceNode = new Hashtable<LSPKey,Integer>();
	}
	
	@Override
	public boolean checkResources(LSPTE lsp) {
		boolean found = false, lambda_found = false;
		boolean BRNode = false;
		Inet4Address prox = null/*, actual=null*/;
		int interfaceID = 0;
		int M = 0, N = 0;
				
		for (int i=0; i<(lsp.getEro()).getEroSubobjects().size(); i++){
			if (((lsp.getEro()).getEroSubobjects().get(i)).getType()==SubObjectValues.ERO_SUBOBJECT_IPV4PREFIX){
				log.info("Objeto IPV4PREFIX "+((IPv4prefixEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getIpv4address());
				if (found == true){
					prox = ((IPv4prefixEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getIpv4address();
					log.info("Prox Hop IPv4 is "+prox.toString());
					break;
				}else if ((found == false) && ((IPv4prefixEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getIpv4address().equals(localID)){
					if ((lsp.getEro()).getEroSubobjects().size()!=i+1){
					if (!(lsp.getIdDestination().equals(localID)) && ((lsp.getEro()).getEroSubobjects().get(i+1)).getType() == SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_IF_ID){
						log.info("Llegamos a un nodo de borde");
						log.info("Saltamos la IPv4 direction");
						BRNode = true;
						continue;
					}else
						found = true;
					}
				}
				continue;
			}else if (((lsp.getEro()).getEroSubobjects().get(i)).getType() == SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_IF_ID){
				log.info("Objeto UNNUMBERED_IF_ID "+((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID());
				if (found == true){
					prox = ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID();
					log.info("Prox Hop Unnumber is "+prox.toString());
					break;
				}
				else if ((found == false) && ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID().equals(localID)){
					//actual = ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID();
					interfaceID =  (int) ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getInterfaceID();
					found = true;
				}
			}else if (((lsp.getEro()).getEroSubobjects().get(i)).getType() == SubObjectValues.ERO_SUBOBJECT_LABEL){
				log.info("Objeto LABEL "+((GeneralizedLabelEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getDwdmWavelengthLabel().getN()+((GeneralizedLabelEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getDwdmWavelengthLabel().getM());
				if (found==true){
					if (lambda_found == false){
						N = ((GeneralizedLabelEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getDwdmWavelengthLabel().getN();
						M = ((GeneralizedLabelEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getDwdmWavelengthLabel().getM();
						lambda_found = true;
					}					
				}
				continue;
			}
		}
		log.info("N: "+N+", M: "+M);
		log.info("Tenemos Prox Hop check Resournes now!");
		//Check SSON Resources
		if (BRNode == false){
			log.info("Normal Node");
			log.info("Outgoing interface: "+interfaceID);
			if (ted.CheckLocalResources(interfaceID, N, M)==true){
				log.info("Create LSP Key with srcID "+lsp.getIdSource().toString()+" and IdLSP "+lsp.getIdLSP());
				LSPKey lspKey = new LSPKey(lsp.getIdSource(), lsp.getIdLSP());
				outputInterfaceNode.put(lspKey, interfaceID);
				N_Allocation.put(lspKey, N);
				M_Allocation.put(lspKey, M);
				proxHopIPv4List.put(lspKey, prox);
				lsp.setLambda(N);
				lsp.setM(M);
				lsp.setInterDomain(false);
				return true;
			}
		}
		else {
			log.info("Border Node Look for inter Domain Links in the MD TEDB");
			log.info("Outgoing interface: "+interfaceID);
			
			if (MDted.CheckLocalResources(interfaceID, localID)){
				LSPKey lspKey = new LSPKey(lsp.getIdSource(), lsp.getIdLSP());
				outputInterfaceNode.put(lspKey, interfaceID);
				N_Allocation.put(lspKey, 0);
				M_Allocation.put(lspKey, 0);
				proxHopIPv4List.put(lspKey, prox);
				lsp.setLambda(0);
				lsp.setM(0);
				lsp.setInterDomain(true);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean reserveResources(LSPTE lsp, Inet4Address dstNodeLink) {
		log.info("Reserving Resources for link from "+localID.toString()+" to "+dstNodeLink.toString());
		long interfaceId=ted.getNetworkGraph().getEdge(localID, dstNodeLink).getSrc_if_id();
		if (ted.AddResourcesConfirmation(interfaceId, lsp.getLambda(), lsp.getM()) == true){
			log.info("Rservation DONE");
			return true;
		}
		else {
			log.info("Reservation NOT DONE");
			return false;
	}}

	@Override
	public boolean freeResources(LSPTE lsp) {
		LSPKey key = new LSPKey(lsp.getIdSource(), lsp.getIdLSP());
		Inet4Address prox = proxHopIPv4List.get(key);
		if (ted.containsVertex(prox) == false){
			return true;
		}
		long interfaceId=ted.getNetworkGraph().getEdge(localID, prox).getSrc_if_id();
		log.info("Releasing Resources for link from "+localID.toString()+" to "+prox.toString());
		if (ted.FreeResourcesConfirmation(interfaceId, N_Allocation.get(new LSPKey(lsp.getIdSource(), lsp.getIdLSP())), lsp.getM()) == true)
			return true;
		else
			return false;
	}

	@Override
	public PCEPRequest getPCEPRequest(LSPTE lspInfo) {
		// Meter los campos en función de la petición RequestedLSPinformation
		
		PCEPRequest p_r = new PCEPRequest();
		Request req = new Request();
		
		RequestParameters rp= new RequestParameters();
		req.setRequestParameters(rp);
		rp.setRequestID(EmulatedPCCPCEPSession.getNewReqIDCounter());
		
		rp.setPrio(1);
		rp.setBidirect(lspInfo.isBidirectional());

		EndPointsIPv4 ep = new EndPointsIPv4();
		
		ep.setSourceIP(lspInfo.getIdSource());
		ep.setDestIP(lspInfo.getIdDestination());
		req.setEndPoints(ep);
		ObjectiveFunction objectiveFunction = new ObjectiveFunction();
		objectiveFunction.setOFcode(lspInfo.getOFcode());
		   			
		req.setObjectiveFunction(objectiveFunction);
		
		BandwidthRequested bandwidth = new BandwidthRequested();
		bandwidth.setBw(lspInfo.getBw());
		req.setBandwidth(bandwidth);
		
		//Pre Reserva
		Reservation reservation = new Reservation();
		reservation.setTimer(10000);
		req.setReservation(reservation);
		
		p_r.addRequest(req);
			
		return p_r;
	}

	public RSVPTEPathMessage getRSVPTEPathMessageFromPCEPResponse(LSPTE lsp) {
				
		//FIXME: Podar ERO
		
		ERO ero = new ERO();
		ero = lsp.getEro();
		
		// Podate ero
		ero.getEroSubobjects().removeFirst(); //Remove IPv4 Node Address
		if (ero.getEroSubobjects().isEmpty()){
			//FIXME: añadir excepcion para este caso
		}if (ero.getEroSubobjects().getFirst().getType() == SubObjectValues.ERO_SUBOBJECT_LABEL){
			ero.getEroSubobjects().removeFirst(); //Borramos el subonjeto LABEL
		}
		
		RSVPTEPathMessage path = new RSVPTEPathMessage();
		
		SessionLSPTunnelIPv4 s = new SessionLSPTunnelIPv4(lsp.getIdDestination(), lsp.getIdLSP().intValue(), lsp.getIdDestination());
				
		//Falta meterle la interfaz del nodo por la que enviamos el RSVP Path
		log.info("lsp.getIdSource()::"+lsp.getIdSource());
		log.info("lsp.getIdLSP()::"+lsp.getIdLSP());
		RSVPHopIPv4 hop = new RSVPHopIPv4(localID, outputInterfaceNode.get(new LSPKey(lsp.getIdSource(), lsp.getIdLSP())));
				
		TimeValues t = new TimeValues(20000);
		LabelRequestWOLabelRange l = new LabelRequestWOLabelRange(N_Allocation.get(new LSPKey(lsp.getIdSource(), lsp.getIdLSP())));
		SenderTemplate st = new SenderTemplateLSPTunnelIPv4(localID,lsp.getIdLSP().intValue());
		IntservSenderTSpec ists = new IntservSenderTSpec();
		
		try {
			SenderDescriptorTE sd = new SenderDescriptorTE(st,ists,null,null);
			path.setSession(s);
			path.setRsvpHop(hop);
			path.setTimeValues(t);
			path.setLabelRequest(l);
			path.addSenderDescriptor(sd);
			path.setEro(ero);
			path.encode();
		} catch (RSVPProtocolViolationException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
	
	@Override
	public RSVPPathTearMessage getRSVPPathTearMessage(LSPTE lsp) {
		RSVPPathTearMessage tear = new RSVPPathTearMessage();
		
		SessionLSPTunnelIPv4 s = new SessionLSPTunnelIPv4(lsp.getIdDestination(), lsp.getIdLSP().intValue(), lsp.getIdDestination());
		RSVPHopIPv4 hop = new RSVPHopIPv4(localID, 0);
		SenderTemplate st = new SenderTemplateLSPTunnelIPv4(localID,lsp.getIdLSP().intValue());
		IntservSenderTSpec ists = new IntservSenderTSpec();
		
		try {
			SenderDescriptorTE sd = new SenderDescriptorTE(st,ists,null,null);
			tear.addSenderDescriptor(sd);
			tear.setRsvpHop(hop);
			tear.setSession(s);
			tear.encode();
		} catch (RSVPProtocolViolationException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	return tear;
	}

	@Override
	public RSVPTEPathMessage forwardRSVPpath(LSPTE lsp, RSVPTEPathMessage path) {
		RSVPHopIPv4 previous = (RSVPHopIPv4) path.getRsvpHop();
		//previous.setNext_previousHopAddress(roadm.getId());
					
		ERO ero = new ERO();
		ero = lsp.getEro();
		
		// Podate ero
		ero.getEroSubobjects().removeFirst(); //Remove IPv4 Node Address
		if (ero.getEroSubobjects().isEmpty()){
			//FIXME: añadir excepcion para este caso
		}
		if (ero.getEroSubobjects().getFirst().getType() == SubObjectValues.ERO_SUBOBJECT_LABEL){
			ero.getEroSubobjects().removeFirst(); //Borramos el subonjeto LABEL
		}
		//forwardingRSVPMessages.setForwardingNode(previous.getNext_previousHopAddress(), lsp.getIdLSP(), lsp.getIdSource());
		previousHopIPv4List.put(new LSPKey (lsp.getIdSource(), lsp.getIdLSP()), previous.getNext_previousHopAddress());
		
		previous.setNext_previousHopAddress(localID);
		
		try {
			path.setLength(RSVPMessageTypes.RSVP_MESSAGE_HEADER_LENGTH);
			path.setBytes(new byte[0]);
			path.encode();
		}catch (RSVPProtocolViolationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return path;
	}
	
	@Override
	public RSVPTEResvMessage getRSVPResvMessageFromDestination(RSVPTEPathMessage path, LSPTE lsp) {
		RSVPTEResvMessage resv = new RSVPTEResvMessage();
		
		SessionLSPTunnelIPv4 s = (SessionLSPTunnelIPv4) path.getSession();
		//Meter el interfaz del puerto por el que enviamos el RSVP Resv
		SenderDescriptorTE sd = (SenderDescriptorTE) path.getSenderDescriptors().getFirst();
		SenderTemplateLSPTunnelIPv4 st_ipv4 = (SenderTemplateLSPTunnelIPv4) sd.getSenderTemplate();

		/**The IP destination address of a Resv message is
         the unicast address of a previous-hop node, obtained from the
         path state.  The IP source address is an address of the node
         that sent the message.
        */
		
		RSVPHopIPv4 previous_hop = (RSVPHopIPv4) path.getRsvpHop();
		Inet4Address prox_resv = previous_hop.getNext_previousHopAddress();
		previousHopIPv4List.put(new LSPKey (lsp.getIdSource(), lsp.getIdLSP()), prox_resv);
		
		RSVPHopIPv4 hop = new RSVPHopIPv4(localID, 0);
		
		TimeValues t = new TimeValues(path.getTimeValues().getRefreshPeriod());
		Style sty = new Style(0, 10);
		
		FilterSpecLSPTunnelIPv4 fs = new FilterSpecLSPTunnelIPv4(st_ipv4.getSenderNodeAddress(), st_ipv4.getLSPId());
		FlowSpec fws = new FlowSpec(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 300);
		//log.info("jm buscando error al encontrar un lsp");
		//log.info("jm path: "+path);
		//log.info("rub path.getLabelRequest(): "+path.getLabelRequest()+" ...");
		//log.info("jm path.getLabelRequest().getL3PID(): "+path.getLabelRequest().getL3PID());
		GeneralizedLabel label = new GeneralizedLabel(path.getLabelRequest().getL3PID());
		try {
			FFFlowDescriptorTE fd = new FFFlowDescriptorTE(fws, fs, label, null, true);
			resv.setSession(s);
			resv.addFlowDescriptor(fd);
			resv.setRsvpHop(hop);
			resv.setTimeValues(t);
			resv.setStyle(sty);
			resv.encode();
		} catch (RSVPProtocolViolationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resv;
	}
	
	public Hashtable<LSPKey, Inet4Address> getProxHopIPv4List() {
		return proxHopIPv4List;
	}

	public void setProxHopIPv4List(Hashtable<LSPKey, Inet4Address> proxHopIPv4List) {
		this.proxHopIPv4List = proxHopIPv4List;
	}

	public Hashtable<LSPKey, Inet4Address> getPreviousHopIPv4List() {
		return previousHopIPv4List;
	}

	public void setPreviousHopIPv4List(
			Hashtable<LSPKey, Inet4Address> previousHopIPv4List) {
		this.previousHopIPv4List = previousHopIPv4List;
	}

	public Hashtable<LSPKey, Integer> getM_Allocation() {
		return M_Allocation;
	}

	public void setM_Allocation(Hashtable<LSPKey, Integer> mAllocation) {
		M_Allocation = mAllocation;
	}

	public Hashtable<LSPKey, Integer> getN_Allocation() {
		return N_Allocation;
	}

	public void setN_Allocation(Hashtable<LSPKey, Integer> nAllocation) {
		N_Allocation = nAllocation;
	}
}