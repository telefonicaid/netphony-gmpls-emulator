package tid.emulator.node.resources.wson;

import java.net.Inet4Address;
import java.util.Hashtable;
import java.util.logging.Logger;

import es.tid.pce.pcep.constructs.Request;
import es.tid.pce.pcep.messages.PCEPRequest;
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
import tid.emulator.node.resources.ResourceManager;
import tid.emulator.node.tedb.SimpleLocalTEDB;
import tid.pce.client.PCCPCEPSession;
import tid.pce.client.lsp.LSPKey;
import tid.pce.client.lsp.te.LSPTE;
import tid.pce.tedb.MDTEDB;

/**
 * 
 * Class implementing an WSON resource manager
 * 
 * @author Fernando Mu�oz del Nuevo
 *
 */

public class WSONResourceManager implements ResourceManager{

	private SimpleLocalTEDB ted;
	
	private Inet4Address localID;
	
	private int Lambda;
		
	/**
	 * FIXME: Explicar que hace esta tabla
	 */
	private Hashtable<LSPKey,Integer> lspLambdaAllocation;
	private Hashtable<LSPKey,Inet4Address> proxHopIPv4List;
	private Hashtable<LSPKey,Inet4Address> previousHopIPv4List;
	
    private Logger log;
	
	public WSONResourceManager(SimpleLocalTEDB tedb, Inet4Address localID){
		this.ted=tedb;
		this.localID=localID;
		log=Logger.getLogger("ROADM");
		lspLambdaAllocation = new Hashtable<LSPKey,Integer>();
		proxHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
		previousHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
	}
	// Constructor Multi Domain
	public WSONResourceManager(SimpleLocalTEDB tedb, Inet4Address localID, MDTEDB MDted){
		this.ted=tedb;
		this.localID=localID;
		log=Logger.getLogger("ROADM");
		lspLambdaAllocation = new Hashtable<LSPKey,Integer>();
		proxHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
		previousHopIPv4List = new Hashtable<LSPKey,Inet4Address>();
	}
	
	@Override
	public boolean checkResources(LSPTE lsp) {
		
		boolean found = false, lambda_found = false;
		Inet4Address prox = null;
		long interfaceID = 0;
		Lambda = 0;		
		for (int i=0; i<(lsp.getEro()).getEroSubobjects().size(); i++){
			//System.out.println("Subobjeto :"+((lsp.getEro()).getEroSubobjects().get(i)));
			if (((lsp.getEro()).getEroSubobjects().get(i)).getType()==SubObjectValues.ERO_SUBOBJECT_IPV4PREFIX){
				
				if (found == true){
					prox = ((IPv4prefixEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getIpv4address();
					break;
				}else if ((found == false) && ((IPv4prefixEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getIpv4address().equals(localID)){
					found = true;
				}
			}
			else if (((lsp.getEro()).getEroSubobjects().get(i)).getType() == SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_IF_ID){
				//System.out.println("Subobjeto en Unnumbered :"+((lsp.getEro()).getEroSubobjects().get(i)));
				if (found == true){
					prox = ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID();
					break;
				}
				else if ((found == false) && ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID().equals(localID)){
					//actual = ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getRouterID();
					interfaceID = ((UnnumberIfIDEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getInterfaceID();
					found = true;
				}
			}
			else if (((lsp.getEro()).getEroSubobjects().get(i)).getType() == SubObjectValues.ERO_SUBOBJECT_LABEL){
				if (lambda_found == false){
					Lambda = ((GeneralizedLabelEROSubobject) (lsp.getEro()).getEroSubobjects().get(i)).getDwdmWavelengthLabel().getN();
					lambda_found = true;
				}
				continue;
			}
		}
		if (ted.CheckLocalResources(interfaceID, Lambda)==true){
			LSPKey lspKey = new LSPKey(lsp.getIdSource(), lsp.getIdLSP());
			lspLambdaAllocation.put(lspKey, Lambda);
			proxHopIPv4List.put(lspKey, prox);
			lsp.setLambda(Lambda);	
			return true;
		}else
			return false;
	}
	
	@Override
	public boolean reserveResources(LSPTE lsp, Inet4Address dstNodeLink) {
		System.out.println("Reserving Resources for link from "+localID.toString()+" to "+dstNodeLink.toString());
		long interfaceId=((SimpleLocalTEDB)ted).getNetworkGraph().getEdge(localID, dstNodeLink).getSrc_if_id();
		LSPKey key = new LSPKey(lsp.getIdSource(), lsp.getIdLSP());
		if (ted.AddResourcesConfirmation(interfaceId, lspLambdaAllocation.get(key)) == true){
			System.out.println("Reservation done in WSON!");
			return true;
		}else
			return false;
	}

	public boolean freeResources(LSPTE lsp) {
		LSPKey key = new LSPKey(lsp.getIdSource(), lsp.getIdLSP());
		Inet4Address prox = proxHopIPv4List.get(key);
		long interfaceId=((SimpleLocalTEDB)ted).getNetworkGraph().getEdge(localID, prox).getSrc_if_id();
		System.out.println("Releasing Resources for link from "+localID.toString()+" to "+prox.toString());
		if (ted.FreeResourcesConfirmation(interfaceId, lspLambdaAllocation.get(key)) == true){
			lspLambdaAllocation.remove(key);
			previousHopIPv4List.remove(key);
			return true;
		}else
			return false;
	}

	public PCEPRequest getPCEPRequest(LSPTE lspInfo) {
		//Meter los campos de la RequestedLSPinformation
		
		PCEPRequest p_r = new PCEPRequest();
		Request req = new Request();
		
		RequestParameters rp= new RequestParameters();
		req.setRequestParameters(rp);
		rp.setRequestID(PCCPCEPSession.getNewReqIDCounter());
		
		rp.setPrio(1);
		rp.setBidirect(lspInfo.isBidirectional());

		EndPointsIPv4 ep = new EndPointsIPv4();
		
		ep.setSourceIP(lspInfo.getIdSource());
		ep.setDestIP(lspInfo.getIdDestination());
		req.setEndPoints(ep);
		ObjectiveFunction objectiveFunction = new ObjectiveFunction();
		objectiveFunction.setOFcode(lspInfo.getOFcode());
		//System.out.println("OF code : "+lspInfo.getOFcode());
		req.setObjectiveFunction(objectiveFunction);
		
		//Pre Reserva
		Reservation reservation = new Reservation();
		reservation.setTimer(10000);
		req.setReservation(reservation);
		
		p_r.addRequest(req);
			
		return p_r;
	}

	@Override
	public RSVPTEPathMessage getRSVPTEPathMessageFromPCEPResponse(LSPTE lsp) {
		//FIXME: Podar ERO
		
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
		
		RSVPTEPathMessage path = new RSVPTEPathMessage();
		
		SessionLSPTunnelIPv4 s = new SessionLSPTunnelIPv4(lsp.getIdDestination(), lsp.getIdLSP().intValue(), lsp.getIdDestination());
		System.out.println("SESSION EGRESS NODE :"+s.getEgressNodeAddress().toString());
		
		//Falta meterle la interfaz del nodo por la que enviamos el RSVP Path
		RSVPHopIPv4 hop = new RSVPHopIPv4(localID, 0);
				
		TimeValues t = new TimeValues(20000);
		LabelRequestWOLabelRange l = new LabelRequestWOLabelRange(lsp.getLambda());
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
		System.out.println("RSVP path creado y codificado!");
		return path;
	}
	
	public RSVPPathTearMessage getRSVPPathTearMessage(LSPTE lsp){
		
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
		}
    	return tear;
	}
	
	public RSVPTEPathMessage forwardRSVPpath(LSPTE lsp,RSVPTEPathMessage path){
		RSVPHopIPv4 previous = (RSVPHopIPv4) path.getRsvpHop();
		//previous.setNext_previousHopAddress(roadm.getId());
		System.out.println("I am a transit node recieving and sending a RSVP Path Message!");
		
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
		
		System.out.println("PREVIOUS HOP PRUEBA en nodo INTERMEDIO:"+previous.getNext_previousHopAddress());
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
	
	public RSVPTEResvMessage getRSVPResvMessageFromDestination(RSVPTEPathMessage path, LSPTE lsp){
		RSVPTEResvMessage resv = new RSVPTEResvMessage();
		
		SessionLSPTunnelIPv4 s = (SessionLSPTunnelIPv4) path.getSession();
		System.out.println("SESSION EGRESS NODE :"+s.getEgressNodeAddress().toString());
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
		System.out.println("Node sender : "+st_ipv4.getSenderNodeAddress());
		
		FilterSpecLSPTunnelIPv4 fs = new FilterSpecLSPTunnelIPv4(st_ipv4.getSenderNodeAddress(), st_ipv4.getLSPId());
		FlowSpec fws = new FlowSpec(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 300);
		
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

	public int getLambda() {
		return Lambda;
	}

	public void setLambda(int lambda) {
		Lambda = lambda;
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
}