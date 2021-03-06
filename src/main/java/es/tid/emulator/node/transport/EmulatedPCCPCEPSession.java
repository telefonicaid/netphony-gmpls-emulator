package es.tid.emulator.node.transport;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.emulator.node.transport.lsp.LSPKey;
import es.tid.emulator.node.transport.lsp.LSPManager;
import es.tid.emulator.node.transport.lsp.te.LSPTE;
import es.tid.pce.client.ClientRequestManager;
import es.tid.pce.client.tester.LSPConfirmationDispatcher;
import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.constructs.EndPoint;
import es.tid.pce.pcep.constructs.Path;
import es.tid.pce.pcep.constructs.StateReport;
import es.tid.pce.pcep.messages.PCEPInitiate;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPReport;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.messages.PCEPTELinkConfirmation;
import es.tid.pce.pcep.messages.PCEPUpdate;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.EndPointsUnnumberedIntf;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.pcep.objects.GeneralizedEndPoints;
import es.tid.pce.pcep.objects.LSP;
import es.tid.pce.pcep.objects.OPEN;
import es.tid.pce.pcep.objects.ObjectParameters;
import es.tid.pce.pcep.objects.SRP;
import es.tid.pce.pcep.objects.tlvs.PathSetupTLV;
import es.tid.pce.pcep.objects.tlvs.SymbolicPathNameTLV;
import es.tid.pce.pcepsession.DeadTimerThread;
import es.tid.pce.pcepsession.GenericPCEPSession;
import es.tid.pce.pcepsession.KeepAliveThread;
import es.tid.pce.pcepsession.PCEPSessionsInformation;
import es.tid.pce.pcepsession.PCEPValues;
import es.tid.rsvp.objects.ERO;
import es.tid.rsvp.objects.subobjects.EROSubobject;
import es.tid.util.UtilsFunctions;

/**
 * <p>PCEP Session initiated from the PCC side</p>
 * 
 * @author Oscar Gonzalez de Dios
 *
 */
public class EmulatedPCCPCEPSession extends GenericPCEPSession{
	
	/**
	 * Manager where the responses of the PCE should be sent
	 */
	public  ClientRequestManager crm;
	/**
	 * Address of the peer PCE
	 */
	private String peerPCE_IPaddress;
	/**
	 * Address of the peer PCE
	 */
	public String localAddress;
	
	/**
	 * Port of the peer PCE
	 */
	private int peerPCE_port;
	/**
	 * Used to obtain new session IDs 
	 */
	private static long sessionIDCounter=0;
	/**
	 * Used to obtain new request IDs 
	 */
	private static long reqIDCounter=0;
	
	/**
	 * Flag to indicate that the session is up
	 */
	private boolean running = true;
	
	private boolean no_delay=false;

	private  LSPConfirmationDispatcher LSPDispatcher;
	
	private PCEPSessionsInformation pcepSessionManager;
	
	private LSPManager lspManager;
		
	
	public Semaphore sessionStarted;
	
	/**
	 * Constructor of the PCE Session
	 * @param ip IP Address of the peer PCE
	 * @param port Port of the peer PCE
	 */
	public EmulatedPCCPCEPSession(String ip, int port, boolean no_delay, PCEPSessionsInformation pcepSessionManager) {
		super(pcepSessionManager);
		this.setFSMstate(PCEPValues.PCEP_STATE_IDLE);
		log=LoggerFactory.getLogger("PCCClient");
		//log.setLevel(Level.OFF);
		this.peerPCE_IPaddress=ip;
		this.peerPCE_port=port;
		crm= new ClientRequestManager();
		this.keepAliveLocal=30;
		this.deadTimerLocal=120;
		timer=new Timer();
		this.no_delay=no_delay;
		this.pcepSessionManager=pcepSessionManager;
		this.lspManager = null;
		this.sessionStarted=new Semaphore(0);
		
	}
	
	/**
	 * This construcutor also has the LSPManager
	 * It should be used for stateful PCE
	 * Constructor of the PCE Session
	 * @param ip IP Address of the peer PCE
	 * @param port Port of the peer PCE
	 */
	public EmulatedPCCPCEPSession(String ip, int port, boolean no_delay, PCEPSessionsInformation pcepSessionManager, LSPManager lspManager) {
		super(pcepSessionManager);
		this.setFSMstate(PCEPValues.PCEP_STATE_IDLE);
		log=LoggerFactory.getLogger("PCCClient");
		//log.setLevel(Level.ALL);
		this.peerPCE_IPaddress=ip;
		this.peerPCE_port=port;
		crm= new ClientRequestManager();
		this.keepAliveLocal=30;
		this.deadTimerLocal=120;
		timer=new Timer();
		this.no_delay=no_delay;
		this.pcepSessionManager=pcepSessionManager;
		this.lspManager = lspManager;
		log.info("this.lspManager.getOut(out):"+this.lspManager.getOut());
		this.lspManager.setOut(out);
		
	}
	
	
	/**
	 * Constructor of the PCE Session
	 * @param ip IP Address of the peer PCE
	 * @param port Port of the peer PCE
	 */
	public EmulatedPCCPCEPSession(String ip, int port, boolean no_delay, LSPConfirmationDispatcher LSPDispatcher, PCEPSessionsInformation pcepSessionManager) {
		super(pcepSessionManager);
		this.setFSMstate(PCEPValues.PCEP_STATE_IDLE);
		log=LoggerFactory.getLogger("PCCClient");
		//log.setLevel(Level.ALL);
		this.peerPCE_IPaddress=ip;
		this.peerPCE_port=port;
		crm= new ClientRequestManager();
		this.keepAliveLocal=30;
		this.deadTimerLocal=120;
		timer=new Timer();
		this.no_delay=no_delay;
		//LSPcreateIP = new LigthPathCreateIP();
		this.LSPDispatcher = LSPDispatcher;
		
	}
	/**
	 * Opens a PCEP session sending an OPEN Message
	 * Then, it launches the Keepalive process, the Deadtimer process and
	 * the listener of PCC messages. 
	 */
	public void run() 
	{
		
		running=true;
		log.info("Opening new PCEP Session with host "+ peerPCE_IPaddress + " on port " + peerPCE_port);
		
		if (socket == null)
		{
			try 
			{
				if (localAddress!=null){
					this.socket = new Socket(Inet4Address.getByName(peerPCE_IPaddress), peerPCE_port,Inet4Address.getByName(localAddress),0);
				}else {
					this.socket = new Socket(peerPCE_IPaddress, peerPCE_port);
			
				}
				
				if (no_delay)
				{
					this.socket.setTcpNoDelay(true);
					log.info("No delay activated");
				}
				log.info("Socket opened");
			} 
			catch (IOException e) 
			{
				log.info(UtilsFunctions.exceptionToString(e));
				log.error("Couldn't get I/O for connection to " + peerPCE_IPaddress + " in port "+ peerPCE_port);
				//FIXME: Salir de manera limpia
				System.exit(1);
			} 
		}

		initializePCEPSession(false, 15, 200,false,false,null,null, pcepSessionManager.isStateful()?(int)lspManager.getDataBaseVersion():(0));
		
		crm.setDataOutputStream(out);
		log.info("PCE Session "+this.toString()+" succesfully established!!");
		this.deadTimerT=new DeadTimerThread(this, this.deadTimerLocal);
		startDeadTimer();	
		this.keepAliveT=new KeepAliveThread(out, this.keepAliveLocal);
		startKeepAlive();
		
		log.info("Now PCE will be informed of all our LSPs");
		log.info("open:"+open);
		if ((pcepSessionManager.isStateful()) && (open != null) && (!(avoidSync(this.open))))
		{
			log.info("Actually sending params");
			sendPCELSPParameters(true, ObjectParameters.LSP_OPERATIONAL_UP, false);
		}
		//Session is up
		if (sessionStarted!=null){
			sessionStarted.release();
		}
		//Listen to new messages
		while(running) {
			try {
				log.info("Waiting for new PCEP Messages!");
			   this.msg = readMsg(in);//Read a new message
			}catch (IOException e){
				cancelDeadTimer();
				cancelKeepAlive();
				try {
					in.close();
					out.close();
				} catch (IOException e1) {
				}
				manageEndSession();
				log.warn("Finishing PCEP Session abruptly!");
				return;
			}
			if (this.msg != null) {//If null, it is not a valid PCEP message								
				log.info("New PCEP Message Read!");
				boolean pceMsg = true;//By now, we assume a valid PCEP message has arrived
				//Depending on the type a different action is performed
				switch(PCEPMessage.getMessageType(this.msg)) {
				
				case PCEPMessageTypes.MESSAGE_OPEN:
					log.info("OPEN message received");
					//After the session has been started, ignore subsequent OPEN messages
					log.warn("OPEN message ignored");
					break;
					
				case PCEPMessageTypes.MESSAGE_KEEPALIVE:
					log.info("KEEPALIVE message received");
					//The Keepalive message allows to reset the deadtimer
					break;
					
				case PCEPMessageTypes.MESSAGE_CLOSE:
					log.info("CLOSE message received");
					killSession();
					return;
					/**************************************************************************/
					/*                CONFIRMACION MULTILAYER                         */
					
					// CONFIRMATION FROM THE VNTM LSP ESTABLISHEMENT
				case PCEPMessageTypes.MESSAGE_TE_LINK_SUGGESTION_CONFIRMATION:
					log.debug("Confirmation from the VNMT received!!!");
					//Establish the TE LINK in the UPPER LAYER
					PCEPTELinkConfirmation telinkconf;
					
					try {
						telinkconf = new PCEPTELinkConfirmation(this.msg);
						//LSPcreateIP.createLigthPath(telinkconf.getPath().geteRO().getEROSubobjectList());
						LSPDispatcher.dispatchLSPConfirmation(telinkconf.getPath(), telinkconf.getLSPid());
					}catch (PCEPProtocolViolationException e) {
						e.printStackTrace();
					}
					//NOTIFY THE CANGE TO THE NETWORK EMULATOR
					break;
					
					
				/**********************************************************************/	
									
				case PCEPMessageTypes.MESSAGE_ERROR:
					log.info("ERROR message received");
					break;
					
				case PCEPMessageTypes.MESSAGE_NOTIFY:
					log.info("Received NOTIFY message");
					break;
					
				case PCEPMessageTypes.MESSAGE_REPORT:
					log.info("Received Report message");	
					long timeInii=System.nanoTime();
					try {
						PCEPReport m_report;
						m_report=new PCEPReport(this.msg);
						
						log.info(m_report.toString());
						crm.notifyResponseInit(m_report, timeInii);
						Semaphore semaphore = crm.semaphores.get(new Long(m_report.getStateReportList().get(0).getSRP().getSRP_ID_number()));
						semaphore.release();

					} catch (PCEPProtocolViolationException e1) {
						log.warn("Problem decoding report message, ignoring message"+e1.getMessage());
						e1.printStackTrace();
					}
					
					break;
					
					
					
				case PCEPMessageTypes.MESSAGE_UPDATE:
					log.info("Received Message Update");
					if (pcepSessionManager.isStateful())
					{
						try 
						{
							PCEPUpdate pupdt=new PCEPUpdate(msg);
							lspManager.updateLSP(pupdt);
						} 
						catch (PCEPProtocolViolationException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							break;
						}	
						break;
					}
					else
					{
						log.warn("Received Update message and sessions is not stateful");
						break;
					}
					
				case PCEPMessageTypes.MESSAGE_PCREP:
					log.info("Received PCE RESPONSE message");
					long timeIni=System.nanoTime();
					PCEPResponse pcres;
					try {
						pcres=new PCEPResponse(msg);
						
						log.info("IdResponse: "+pcres.getResponse(0).getRequestParameters().getRequestID());
						Object lock=crm.locks.get(new Long(pcres.getResponse(0).getRequestParameters().getRequestID()));
						if (lock!=null){
							synchronized (lock) {
								crm.notifyResponse(pcres, timeIni);
							}							
						}
						else{
							log.warn("Ha llegado la response con ID: "+pcres.getResponse(0).getRequestParameters().getRequestID()+" Y el lock era null.");
						}
						
					} catch (PCEPProtocolViolationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}					
					break;
					
				case PCEPMessageTypes.MESSAGE_INITIATE:
					log.info("Received INITIATE message");
					timeIni=System.nanoTime();
					try {
						PCEPInitiate p_init = new PCEPInitiate(msg);
						log.info("ini from "+this.remotePeerIP+":"+p_init);
						long sRP_ID_number = p_init.getPcepIntiatedLSPList().get(0).getRsp().getSRP_ID_number();
						//LSPTE lsp = new LSPTE(lsp_id, lspManager.getLocalIP(), ((EndPointsIPv4)p_init.getPcepIntiatedLSPList().get(0).getEndPoint()).getDestIP(), false, 1001, 10000, PathStateParameters.creatingLPS);
						PathSetupTLV pstlv = p_init.getPcepIntiatedLSPList().get(0).getRsp().getPathSetupTLV();
						if (pstlv != null && pstlv.isSR())
						{
							log.info("Found initiate message with segment routing..sending report");
							ExplicitRouteObject srero = p_init.getPcepIntiatedLSPList().get(0).getEro();					
							SRP rsp = p_init.getPcepIntiatedLSPList().get(0).getRsp();
							LSP lsp = p_init.getPcepIntiatedLSPList().get(0).getLsp();
							PCEPReport pcrep = new PCEPReport();
							StateReport srep = new StateReport();

							Path path = new Path();
							path.setEro(srero);
							
							srep.setSRP(rsp);
							srep.setLSP(lsp);
							srep.setPath(path);
							
							pcrep.addStateReport(srep);
							log.info("Sending message to pce...");
							sendPCEPMessage(pcrep);
							log.info("Message sent!");
							
						}
						else
						{
							log.info("Found initiate message without segment routing.");
							
							SRP srpp = p_init.getPcepIntiatedLSPList().get(0).getRsp();
							boolean delete=srpp.isrFlag();
							
							
							if (delete){
								
								long lsp_id = p_init.getPcepIntiatedLSPList().get(0).getLsp().getLspId();
								
								LSPTE lspp=lspManager.getLSP(lsp_id,lspManager.getLocalIP());
								
								ExplicitRouteObject ero =new ExplicitRouteObject();
								
								ero.addEROSubobjectList(lspp.getEro().getEroSubobjects());
								
								
								log.info("Delete EmulatedPCCPCEPSession");
								
								this.lspManager.deleteLSP(this.lspManager.getLocalIP(), p_init.getPcepIntiatedLSPList().get(0).getLsp().getLspId());
									
								log.info("Getting the ero from the p_init");
																		
								PCEPReport pcrep = new PCEPReport();
								StateReport srep = new StateReport();
	
								Path path = new Path();
								path.setEro(ero);
									
								SRP srp = new SRP();
								srp.setSRP_ID_number(sRP_ID_number);
								srp.setRFlag(true);
								srep.setSRP(srp);
								LSP lsp = new LSP();
								lsp.setLspId((int)lsp_id);
								srep.setLSP(lsp);
								
								srep.setPath(path);
								SymbolicPathNameTLV symbolicPathNameTLV_tlv = new SymbolicPathNameTLV();
								log.info( "XXXX p_init:"+p_init);
								log.info(" XXXX p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv():"+p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv());
								log.info( "XXXX p_init ... getSymbolicPathNameID():"+p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv().getSymbolicPathNameID());
								
								
								if (p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv()!=null){
									symbolicPathNameTLV_tlv.setSymbolicPathNameID(p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv().getSymbolicPathNameID());
									lsp.setSymbolicPathNameTLV_tlv(symbolicPathNameTLV_tlv);
									log.info("XXXX lsp.getSymbolicPathNameTLV_tlv(): "+lsp.getSymbolicPathNameTLV_tlv());
									
								}else {
									log.warn("NO SYMBOLIC PATH NAME TLV!!!" );
								}
								
								pcrep.addStateReport(srep);
								
								pcrep.encode();
								log.info("sending: "+ pcrep.toString());					
								this.socket.getOutputStream().write(pcrep.getBytes());
								this.socket.getOutputStream().flush();
									
								log.info("Sending Report message to pce...");
									
								
								
								log.info(" Finish Delete EmulatedPCCPCEPSession");
								
								
							} else {
								ExplicitRouteObject ero = p_init.getPcepIntiatedLSPList().get(0).getEro();

								ERO eroOther = new ERO();

								eroOther.setEroSubobjects(ero.getEROSubobjectList());

								//lspManager.startLSP(lsp, eroOther);

								Inet4Address destinationId = null;
								destinationId = (Inet4Address)  Inet4Address.getByName(getDestinationIP(p_init.getPcepIntiatedLSPList().get(0).getEndPoint()));
								
								long lsp_id = lspManager.addnewLSP(destinationId, 1000, false, 1002,eroOther);
								log.info("LSPList: "+lspManager.getLSPList().size()+" "+(new LSPKey(lspManager.getLocalIP(), lsp_id)).toString());
								long time1= System.nanoTime();
								lspManager.waitForLSPaddition(lsp_id, 1000);
								
								LSPTE lspp=lspManager.getLSP(lsp_id,lspManager.getLocalIP());
								
								if (lspp!=null){
									log.info("LSP with id "+lsp_id+" has been established");
									
									PCEPReport pcrep = new PCEPReport();
									StateReport srep = new StateReport();

									Path path = new Path();
									path.setEro(ero);
									
									SRP srp = new SRP();
									srp.setSRP_ID_number(sRP_ID_number);
									srep.setSRP(srp);
									LSP lsp = new LSP();
									lsp.setLspId((int)lsp_id);
									srep.setLSP(lsp);
									
									//srep.setLSP(lsp);
									srep.setPath(path);
									SymbolicPathNameTLV symbolicPathNameTLV_tlv = new SymbolicPathNameTLV();
									log.info( "XXXX p_init:"+p_init);
									log.info(" XXXX p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv():"+p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv());
									log.info( "XXXX p_init ... getSymbolicPathNameID():"+p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv().getSymbolicPathNameID());
									
									
									if (p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv()!=null){
										symbolicPathNameTLV_tlv.setSymbolicPathNameID(p_init.getPcepIntiatedLSPList().get(0).getLsp().getSymbolicPathNameTLV_tlv().getSymbolicPathNameID());
										lsp.setSymbolicPathNameTLV_tlv(symbolicPathNameTLV_tlv);
										log.info("XXXX lsp.getSymbolicPathNameTLV_tlv(): "+lsp.getSymbolicPathNameTLV_tlv());
										
									}else {
										log.warn("NO SYMBOLIC PATH NAME TLV!!!" );
									}
									
									pcrep.addStateReport(srep);
									
									pcrep.encode();
									log.info("sending: "+ pcrep.toString());					
									this.socket.getOutputStream().write(pcrep.getBytes());
									this.socket.getOutputStream().flush();
									
									
									log.info("Sending Report message to pce...");
									
								}else {
									log.warn("LSP with id "+lsp_id+" has NOT been established");
								}
								
							}
							
							
							
							//lspManager.notifyLPSEstablished(lsp_id, lspManager.getLocalIP());
							
							//UpdateRequest ur =p_init.getUpdateRequestList().getFirst();		
							//log.info(p_req.toString());

						}				
						
						

						
					} catch (Exception e) {
						log.error("PROBLEMON");
						e.printStackTrace();
						break;
					}								
					
					break;					
					
				case PCEPMessageTypes.MESSAGE_PCREQ:
					log.info("PCREQ message received");
					break;


				default:
					log.info("ERROR: unexpected message PCCCEPSession with type : "+PCEPMessage.getMessageType(this.msg));
					pceMsg = false;
				}
				
				
				
				
				
				
				
				if (pceMsg) {
					log.info("Reseting Dead Timer as PCEP Message has arrived");
					resetDeadTimer();
				}
			}
		}
	}
	
	private void manageEndSession() 
	{
		while(true)
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e)
			{
				log.info(UtilsFunctions.exceptionToString(e));
			}
			
			try 
			{
				this.socket = new Socket(peerPCE_IPaddress, peerPCE_port);
				log.info("Socket opened in retry after connection went down");
				//socket.close();
				
			//	crm= new ClientRequestManager();
			//	timer=new Timer();
			//	this.lspManager.setOut((DataOutputStream)socket.getOutputStream());
				
				
				
				run();
				return;
			} 
			catch (IOException e)
			{
				log.info("Wasn't able to connect this time");
			} 
			
		}	
	}

	public int getPeerPCE_port() {
		return peerPCE_port;
	}

	public String getPeerPCE_IPaddress() {
		return peerPCE_IPaddress;
	}
	synchronized public long getNewSessionIDCounter(){
		long newSessionId;
		if (sessionIDCounter==0){
			Calendar rightNow = Calendar.getInstance();
			newSessionId=rightNow.get(Calendar.SECOND);		
			sessionIDCounter=newSessionId;
		}
		else {
			if (sessionIDCounter>=0xFFFFFFFDL){
				newSessionId=1;
				sessionIDCounter=newSessionId;
			}else{
				newSessionId=sessionIDCounter+1;
				sessionIDCounter=newSessionId;	
			}
		}	
		return newSessionId;
	}
	
	synchronized static public long getNewReqIDCounter(){
		long newReqId;
		if (reqIDCounter==0){
			Calendar rightNow = Calendar.getInstance();
			newReqId=rightNow.get(Calendar.SECOND);		
			reqIDCounter=newReqId;
		}
		else {
			if (reqIDCounter>=0xFFFFFFFDL){
				newReqId=1;
				reqIDCounter=newReqId;
			}else {
				newReqId=reqIDCounter+1;
				reqIDCounter=newReqId;	
			}
			
		}	
		return newReqId;
	}
	
	public void endSession()
	{

		log.info("Ending PCC session, abruptly?, who knows");

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "("+peerPCE_IPaddress+" - "+ peerPCE_port+")";
	}
	
	/**
	 * Initiates a Session between the Domain PCE and the peer PCC
	 */
	private void sendPCELSPParameters(boolean dFlag, int opFlags, boolean rFlag)
	{	
		Hashtable<LSPKey, LSPTE> lsps = lspManager.getLSPList();
		Enumeration<LSPKey> keys = lsps.keys();
		log.info("Thought this node there are "+lsps.size()+" LSPs");
		while( keys.hasMoreElements() )
	    {
			 LSPKey key = keys.nextElement();
		     LSPTE lspte = lsps.get(key);
		     //Means this PCC owns this LSP and must send information to PCE
		     if (lspte.getIdSource().equals(lspManager.getLocalIP()))
		     {
		    	 lspManager.getNotiLSP().notify(lspte, true, true, false, true, out);
	    	 }
	     }
		log.info("Sending las PCEPReport to stop sync");
		
		LSPTE lspte = new LSPTE(0, lspManager.getLocalIP(), lspManager.getLocalIP(), false, 0, 0, 0);
		ERO ero = new ERO();
		ero.setEroSubobjects(new LinkedList<EROSubobject>());
		lspte.setEro(ero);
		
		//Sync flag to 0 cause it's the last pcrpt to notify end of sync
		lspManager.getNotiLSP().notify(lspte, true, true, false, false, out);
		 
	}
	
	private boolean avoidSync(OPEN open) 
	{
		log.info("Open :"+open.toString());

		if (open.getStateful_capability_tlv() == null)
		{
			return true;
		}
		if (open.getLsp_database_version_tlv() == null)
		{
			return true;
		}
		long dataBaseId = open.getLsp_database_version_tlv().getLSPStateDBVersion();
		log.info("dataBaseId:"+dataBaseId);
		log.info("lspManager.getDataBaseVersion() :"+lspManager.getDataBaseVersion() );
		boolean PCESyncFlag = open.getStateful_capability_tlv().issFlag();
		boolean mySyncFlag = pcepSessionManager.isStatefulSFlag();
		if (PCESyncFlag && mySyncFlag && lspManager.getDataBaseVersion() != dataBaseId)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
public String getDestinationIP(Object endPoint) {
		
		String destinationIP=null;
		
		if (endPoint == null){
			log.info(" endPoint es null");
			
		}else 
			
			
			if (endPoint instanceof EndPointsIPv4){
			log.info(" endPoint es de tipo EndPointsIPv4");
			destinationIP = ((EndPointsIPv4) endPoint).getDestIP().toString();
			
			
			}else{ 
				
				if (endPoint instanceof GeneralizedEndPoints){   
					log.info(" endPoint es de tipo GeneralizedEndPoints");
											
					EndPoint destEndPoint= ((GeneralizedEndPoints) endPoint).getP2PEndpoints().getDestinationEndPoint();
					if (destEndPoint.getEndPointIPv4TLV()!=null){
						destinationIP=destEndPoint.getEndPointIPv4TLV().getIPv4address().getHostAddress();
					}else if (destEndPoint.getUnnumberedEndpoint()!=null) {
						destinationIP=destEndPoint.getUnnumberedEndpoint().getIPv4address().getHostAddress();
					}
						
				}else log.info(" endPoint NO es de tipo conocido");
			}
				
			
			
			
			
			/*	
		}else if (endPoint instanceof EndPointsUnnumberedIntf){
			log.info("jm endPoint es de tipo EndPointsUnnumberedIntf");
			destinationIP = ((EndPointsUnnumberedIntf) endPoint).getDestIP().toString();
			
		}else if (endPoint instanceof GeneralizedEndPoints){
			log.info("jm endPoint es de tipo GeneralizedEndPoints");
			destinationIP = ((GeneralizedEndPoints) endPoint).getP2PEndpoints().getDestinationEndPoint().toString();
			
		}else log.info("jm endPoint NO es de tipo conocido");
		*/
			
				
		return destinationIP;
	}

}







