package tid.emulator.node.transport.rsvp;

import java.net.Inet4Address;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import es.tid.rsvp.RSVPProtocolViolationException;
import es.tid.rsvp.constructs.te.FFFlowDescriptorTE;
import es.tid.rsvp.constructs.te.SenderDescriptorTE;
import es.tid.rsvp.messages.RSVPMessage;
import es.tid.rsvp.messages.RSVPMessageTypes;
import es.tid.rsvp.messages.RSVPPathTearMessage;
import es.tid.rsvp.messages.te.RSVPTEPathMessage;
import es.tid.rsvp.messages.te.RSVPTEResvMessage;
import es.tid.rsvp.objects.FilterSpecLSPTunnelIPv4;
import es.tid.rsvp.objects.FlowSpec;
import es.tid.rsvp.objects.RSVPHopIPv4;
import es.tid.rsvp.objects.RSVPObject;
import es.tid.rsvp.objects.SenderTemplateLSPTunnelIPv4;
import es.tid.rsvp.objects.SessionLSPTunnelIPv4;
import tid.emulator.node.resources.ResourceManager;
import tid.emulator.node.transport.LSPCreationException;
import tid.pce.client.lsp.LSPCreationErrorTypes;
import tid.pce.client.lsp.LSPKey;
import tid.pce.client.lsp.LSPManager;
import tid.pce.client.lsp.LSPParameters;
import tid.pce.client.lsp.te.LSPTE;
import tid.pce.client.lsp.te.PathStateParameters;

public class RSVPProcessor extends Thread{


	private int messageType;
	private LSPManager managerLSP;

	private Logger log;
	private boolean running;
	private  LinkedBlockingQueue<RSVPMessage> RSVPMessageQueue;
	private RSVPManager rsvpManager;
	private int OF;
	
	private ResourceManager resourceManager;

	public RSVPProcessor(RSVPManager rsvpManager, LSPManager managerLSP, LinkedBlockingQueue<RSVPMessage> rsvpMessageQueue, /*int OF,*/ ResourceManager resourceManager){
		running=true;
		this.managerLSP = managerLSP;
		this.RSVPMessageQueue=rsvpMessageQueue;
		log=Logger.getLogger("ROADM");
		this.rsvpManager=rsvpManager;
		/*this.OF=OF;*/
		this.resourceManager=resourceManager;
	}
	
	/**
	 *
	 *<p>  2.2. Operation of LSP Tunnels

		   This section summarizes some of the features supported by RSVP as
		   extended by this document related to the operation of LSP tunnels.
		   These include: (1) the capability to establish LSP tunnels with or
		   without QoS requirements, (2) the capability to dynamically reroute
		   an established LSP tunnel, (3) the capability to observe the actual
		   route traversed by an established LSP tunnel, (4) the capability to
		   identify and diagnose LSP tunnels, (5) the capability to preempt an
		   established LSP tunnel under administrative policy control, and (6)
		   the capability to perform downstream-on-demand label allocation,
		   distribution, and binding.  In the following paragraphs, these
		   features are briefly described.  More detailed descriptions can be
		   found in subsequent sections of this document.
		
		   To create an LSP tunnel, the first MPLS node on the path -- that is,
		   the sender node with respect to the path -- creates an RSVP Path
		   message with a session type of LSP_TUNNEL_IPv4 or LSP_TUNNEL_IPv6 and
		   inserts a LABEL_REQUEST object into the Path message.  The
		   LABEL_REQUEST object indicates that a label binding for this path is
		   requested and also provides an indication of the network layer
		   protocol that is to be carried over this path.  The reason for this
		   is that the network layer protocol sent down an LSP cannot be assumed
		   to be IP and cannot be deduced from the L2 header, which simply
		   identifies the higher layer protocol as MPLS.
		
		   If the sender node has knowledge of a route that has high likelihood
		   of meeting the tunnel's QoS requirements, or that makes efficient use
		   of network resources, or that satisfies some policy criteria, the
		   node can decide to use the route for some or all of its sessions.  To
		   do this, the sender node adds an EXPLICIT_ROUTE object to the RSVP
		   Path message.  The EXPLICIT_ROUTE object specifies the route as a
		   sequence of abstract nodes.
		
		   If, after a session has been successfully established, the sender
		   node discovers a better route, the sender can dynamically reroute the
		   session by simply changing the EXPLICIT_ROUTE object.  If problems
		   are encountered with an EXPLICIT_ROUTE object, either because it
		   causes a routing loop or because some intermediate routers do not
		   support it, the sender node is notified.
		
		   By adding a RECORD_ROUTE object to the Path message, the sender node
		   can receive information about the actual route that the LSP tunnel
		   traverses.  The sender node can also use this object to request
		   notification from the network concerning changes to the routing path.
		   The RECORD_ROUTE object is analogous to a path vector, and hence can
		   be used for loop detection.
		
		   Finally, a SESSION_ATTRIBUTE object can be added to Path messages to
		   aid in session identification and diagnostics.  Additional control
		   information, such as setup and hold priorities, resource affinities
		   (see [3]), and local-protection, are also included in this object.
		
		   Routers along the path may use the setup and hold priorities along
		   with SENDER_TSPEC and any POLICY_DATA objects contained in Path
		   messages as input to policy control.  For instance, in the traffic
		   engineering application, it is very useful to use the Path message as
		   a means of verifying that bandwidth exists at a particular priority
		   along an entire path before preempting any lower priority
		   reservations.  If a Path message is allowed to progress when there
		   are insufficient resources, then there is a danger that lower
		   priority reservations downstream of this point will unnecessarily be
		   preempted in a futile attempt to service this request.
		
		   When the EXPLICIT_ROUTE object (ERO) is present, the Path message is
		   forwarded towards its destination along a path specified by the ERO.
		   Each node along the path records the ERO in its path state block.
		   Nodes may also modify the ERO before forwarding the Path message.  In
		   this case the modified ERO SHOULD be stored in the path state block
		   in addition to the received ERO.
		
		   The LABEL_REQUEST object requests intermediate routers and receiver
		   nodes to provide a label binding for the session.  If a node is
		   incapable of providing a label binding, it sends a PathErr message
		   with an "unknown object class" error.  If the LABEL_REQUEST object is
		   not supported end to end, the sender node will be notified by the
		   first node which does not provide this support.
		
		   The destination node of a label-switched path responds to a
		   LABEL_REQUEST by including a LABEL object in its response RSVP Resv
		   message.  The LABEL object is inserted in the filter spec list
		   immediately following the filter spec to which it pertains.
		
		   The Resv message is sent back upstream towards the sender, following
		   the path state created by the Path message, in reverse order.  Note
		   that if the path state was created by use of an ERO, then the Resv
		   message will follow the reverse path of the ERO.
		
		   Each node that receives a Resv message containing a LABEL object uses
		   that label for outgoing traffic associated with this LSP tunnel.  If
		   the node is not the sender, it allocates a new label and places that
		   label in the corresponding LABEL object of the Resv message which it
		   sends upstream to the PHOP.  The label sent upstream in the LABEL
		   object is the label which this node will use to identify incoming
		   traffic associated with this LSP tunnel.  This label also serves as
		   shorthand for the Filter Spec.  The node can now update its "Incoming
		   Label Map" (ILM), which is used to map incoming labeled packets to a
		   "Next Hop Label Forwarding Entry" (NHLFE), see [2].
		
		   When the Resv message propagates upstream to the sender node, a
		   label-switched path is effectively established.</p>
	 */
	
	public void run(){
		//FIXME; Operar con el mensaje
		RSVPMessage message = null;
		while (running){
			try {
				message=RSVPMessageQueue.take();
				log.info("Sacamos mensaje de la Cola");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
				break;
			}
			messageType=message.getMsgType();
			managerLSP.setTimeIni_Node(System.nanoTime());	
			if(this.messageType == RSVPMessageTypes.MESSAGE_PATH){
					
					log.finer("RSVP-TE Path message received");
					RSVPTEPathMessage path = (RSVPTEPathMessage) message;
					RSVPObject session = path.getSession();
					if(session.getcType() == 7){	// IPv4 Tunnel
						SessionLSPTunnelIPv4 tunnelIPv4 = (SessionLSPTunnelIPv4) session;
						SenderDescriptorTE sd = (SenderDescriptorTE) path.getSenderDescriptors().getFirst();
						SenderTemplateLSPTunnelIPv4 st_ipv4 = (SenderTemplateLSPTunnelIPv4) sd.getSenderTemplate();
						log.info("idLSP : "+tunnelIPv4.getTunnelId());
						log.info(" tunnelIPv4.getEgressNodeAddress() : "+ tunnelIPv4.getEgressNodeAddress());
						//FIXME: This code was taking a null LSP (exists, but null...)
						// LSP Exists?
						/*if(managerLSP.existLSP(tunnelIPv4.getTunnelId(), st_ipv4.getSenderNodeAddress())){
							//LSP exist
							log.info("Existent LSP Refreshing Session!");
					    	LSPTE lsp = managerLSP.getLSP(new Long(tunnelIPv4.getTunnelId()), tunnelIPv4.getEgressNodeAddress());
					    	// FIXME: Revisar el estado del LSP
					    	try {
								managerLSP.forwardRSVPpath(lsp,path);
							} catch (LSPCreationException e) {
								switch(e.getErrorType()){
								case(LSPCreationErrorTypes.NO_RESOURCES):{
									log.info("ERROR No Resources Available in TEDB!");	    				
									break;
								}
								default:{
									log.info("Different ERROR!!");
									System.exit(-1);
									break;
								}
								}
							}
					    }else{*/
							//Create a New LSP in the Node
							log.info("New LSP!");
							//FIXME: Mirar esto
							//Meter bien el ancho de banda, ponemos 0 de entrada luego se calcula en función de la m
							LSPTE lsp = new LSPTE(tunnelIPv4.getTunnelId(),st_ipv4.getSenderNodeAddress(),tunnelIPv4.getEgressNodeAddress(), false, OF, 0, PathStateParameters.creatingLPS);
					    	lsp.setEro(path.getEro());
					    	try {
								managerLSP.forwardRSVPpath(lsp,path);
							} catch (LSPCreationException e) {
								switch(e.getErrorType()){
								case(LSPCreationErrorTypes.NO_RESOURCES):{
									log.info("ERROR No Resources Available in TEDB!");	    				
									break;
								}
								default:{
									log.info("Different ERROR!!");
									System.exit(-1);
									break;
								}
								}
							}
						//}
					}else{
						log.info("Is not an IPv4 tunnel!");
					}
			} if (this.messageType == RSVPMessageTypes.MESSAGE_RESV) {
				log.info("RSVP-TE Resv message received!");
				RSVPTEResvMessage resv = (RSVPTEResvMessage) message;
				RSVPObject session = resv.getSession();
				SessionLSPTunnelIPv4 tunnelIPv4=null;
				if (session.getcType() == 7){
					tunnelIPv4 = (SessionLSPTunnelIPv4) session;
				}
				FFFlowDescriptorTE fd = (FFFlowDescriptorTE)resv.getFlowDescriptors().getFirst();
				FilterSpecLSPTunnelIPv4 fstIPv4 = (FilterSpecLSPTunnelIPv4) fd.getFilterSpec();
								
				int nodeType = LSPParameters.LSP_NODE_TYPE_TRANSIT;
							
				LSPTE lsp = managerLSP.getLSP(tunnelIPv4.getTunnelId(), fstIPv4.getSenderNodeAddress());
								
				if (lsp.getIdSource().equals(managerLSP.getLocalIP())){
					log.info("We have arrived to the SOURCE node!");
					nodeType = LSPParameters.LSP_NODE_TYPE_SOURCE;
								
					//Establish the resources in the TEDB
					boolean reserve = false;
					if (lsp.isInterDomain()){
						log.info("Inter Domain Link, don't Reserve!");
						reserve = true;
					}else{
						log.info("Doing Reservation in IntraDomain Link!");
						reserve = resourceManager.reserveResources(lsp, ((RSVPHopIPv4) resv.getRsvpHop()).getNext_previousHopAddress());
					}
					
					if (reserve == false){
						log.info("Couldn't Reserve the resources in the Link!");
					}
					else{
						log.info("LSP Established and reservation done!");
						managerLSP.setTimeEnd_Node(System.nanoTime());
						managerLSP.notifyLPSEstablished(lsp.getIdLSP(), lsp.getIdSource());
					}
				}

				if (nodeType == LSPParameters.LSP_NODE_TYPE_TRANSIT){
					boolean reservation;
					log.info("Transit Node in Reservation Process!");
					if (lsp.isInterDomain()){
						log.info("Inter Domain Link, don't Reserve!");
						reservation = true;
					}else{
						log.info("Doing Reservation in IntraDomain Link!");
						reservation = resourceManager.reserveResources(lsp, ((RSVPHopIPv4) resv.getRsvpHop()).getNext_previousHopAddress());
						if (reservation == false){
							log.info("Couldn't Reserve the resources in the Link!");
						}
					}
					if(reservation==true){
						log.info("Reservation OK! Forward RSVP Message!");
						//FIXME: meter bien la interfaz de salida segundo campo que hay --> puesto a 0			
						RSVPHopIPv4 hop = new RSVPHopIPv4(managerLSP.getLocalIP(), 0);
						FlowSpec flowSpec = new FlowSpec(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 300);
						fd.setFlowSpec(flowSpec);
						try {
							resv.setLength(RSVPMessageTypes.RSVP_MESSAGE_HEADER_LENGTH);
							resv.setBytes(new byte[0]);
							resv.setRsvpHop(hop);
							resv.addFlowDescriptor(fd);
							resv.encode();
							
						} catch (RSVPProtocolViolationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Inet4Address prox = resourceManager.getPreviousHopIPv4List().get(new LSPKey(lsp.getIdSource(), tunnelIPv4.getTunnelId()));
						log.info("Sending RSVP Resv Message to "+prox.toString()+" !");
						managerLSP.setTimeEnd_Node(System.nanoTime());
						log.info("LSP Time to Process RSVP Resv Messg in Node (ms): "+((managerLSP.getTimeEnd_Node()-managerLSP.getTimeIni_Node())/1000000));
						rsvpManager.sendRSVPMessage(resv, prox);
					}
				}
			}
			else if (this.messageType == RSVPMessageTypes.MESSAGE_PATHTEAR){
				log.info("RSVP-TE Message Path Tear received");
				RSVPPathTearMessage tear = (RSVPPathTearMessage) message;
				RSVPObject session = tear.getSession();
				
				if(session.getcType() == 7){	// IPv4 Tunnel
					SessionLSPTunnelIPv4 tunnelIPv4 = (SessionLSPTunnelIPv4) session;
					
					log.info("Recibido el tunnel");
					SenderDescriptorTE sd = (SenderDescriptorTE) tear.getSenderDescriptors().getFirst();
					SenderTemplateLSPTunnelIPv4 st_ipv4 = (SenderTemplateLSPTunnelIPv4) sd.getSenderTemplate();
				
					LSPTE lsp = managerLSP.getLSP(tunnelIPv4.getTunnelId(), st_ipv4.getSenderNodeAddress());
					
					int nodeType = LSPParameters.LSP_NODE_TYPE_TRANSIT;
					if((lsp.getIdDestination().getHostAddress()).equals(managerLSP.getLocalIP().getHostAddress())){
			    		//Soy destino
			    		nodeType = LSPParameters.LSP_NODE_TYPE_DESTINATION;
			    		log.info("Soy el nodo Destino");
			    	}
					
					if(nodeType == LSPParameters.LSP_NODE_TYPE_DESTINATION){
						managerLSP.killLSP(lsp.getIdLSP(), lsp.getIdSource());
						log.info("We are at destination Node PATH TEARED DOWN !");
					
					}else {
						log.info("Transit node receiving a Teardown Message!");
						managerLSP.killLSP(lsp.getIdLSP(), lsp.getIdSource());
						boolean free = resourceManager.freeResources(lsp);
						if (free==false){
							log.info("Some error while releasing the Resources in the TEDB");
						}else{
							SessionLSPTunnelIPv4 s = (SessionLSPTunnelIPv4) tear.getSession();
							RSVPHopIPv4 previous = (RSVPHopIPv4) tear.getRsvpHop();
							previous.setNext_previousHopAddress(managerLSP.getLocalIP());
							
							try {
								tear.setSession(s);
								tear.encode();
							} catch (RSVPProtocolViolationException e){
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Inet4Address prox = resourceManager.getProxHopIPv4List().get(new LSPKey (lsp.getIdSource(), lsp.getIdLSP()));
							log.info("Sending the RSVP PATH TEAR message to "+prox.toString()+" !");
							managerLSP.sendRSVPMessage(tear, prox);
							
						}
					}
				}
			}
		}
	}
}