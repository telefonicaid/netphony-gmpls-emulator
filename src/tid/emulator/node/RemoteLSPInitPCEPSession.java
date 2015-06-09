package tid.emulator.node;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Timer;
import java.util.logging.Logger;

import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.constructs.Response;
import es.tid.pce.pcep.constructs.UpdateRequest;
import es.tid.pce.pcep.messages.PCEPClose;
import es.tid.pce.pcep.messages.PCEPError;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPMonReq;
import es.tid.pce.pcep.messages.PCEPNotification;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.messages.PCEPUpdate;
import es.tid.pce.pcep.objects.Bandwidth;
import es.tid.pce.pcep.objects.BandwidthRequested;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.LSP;
import es.tid.pce.pcep.objects.NoPath;
import es.tid.pce.pcep.objects.PCEPErrorObject;
import es.tid.pce.pcep.objects.RequestParameters;
import tid.emulator.node.transport.LSPCreationException;
import tid.emulator.node.transport.lsp.LSPCreationErrorTypes;
import tid.emulator.node.transport.lsp.LSPManager;
import tid.emulator.node.transport.lsp.te.LSPTE;
import tid.pce.client.emulator.AutomaticTesterStatistics;
import tid.pce.pcepsession.DeadTimerThread;
import tid.pce.pcepsession.GenericPCEPSession;
import tid.pce.pcepsession.KeepAliveThread;
import tid.pce.pcepsession.PCEPSessionsInformation;
import tid.pce.pcepsession.PCEPValues;
import tid.pce.server.NotificationDispatcher;
import tid.pce.server.communicationpce.CollaborationPCESessionManager;
import tid.pce.server.communicationpce.RollSessionType;

public class RemoteLSPInitPCEPSession extends GenericPCEPSession {
	
	private NotificationDispatcher notificationDispatcher;
	
	private long internalSessionID;
	
	private static long lastInternalSessionID=0;

	private  CollaborationPCESessionManager collaborationPCESessionManager=null;
	
	private LSPManager lspManager;
	
	private Inet4Address idRoadm;
	
	private Hashtable<Long,Long> lspIdsCorelation;
	
	private AutomaticTesterStatistics stats=null;
	
	public RemoteLSPInitPCEPSession(Socket s, LSPManager lspManager, Inet4Address idRoadm, PCEPSessionsInformation pcepSessionManager) {
		super(pcepSessionManager);
		this.lspManager=lspManager;
		this.socket=s;
		this.idRoadm=idRoadm;
		this.log=Logger.getLogger("ROADM");
		timer=new Timer();
		lspIdsCorelation = new Hashtable<Long,Long>();
	}
	
	public void run() {
		initializePCEPSession(false,2,30000,false,false,null,null,0);
		//Session is UP now, start timers
			
		//Poner qu� tipo de session es?? como lo se??
		if (collaborationPCESessionManager!=null){
			int roll=RollSessionType.COLLABORATIVE_PCE;/*Como seeeee esl rollllll*/
			//Si el roll es de PCE de backup, tengo que meter el Dataoutput en collaborative PCEs			
			collaborationPCESessionManager.getOpenedSessionsManager().registerNewSession(/*this.remoteDomainId,*//*this.remotePCEId,*/ out,roll);
		}
		
		this.deadTimerT=new DeadTimerThread(this, this.deadTimerLocal);
		startDeadTimer();	
		this.keepAliveT=new KeepAliveThread(out, this.keepAliveLocal);
		startKeepAlive();

		//Listen to new messages
		try{
			while(this.FSMstate==PCEPValues.PCEP_STATE_SESSION_UP) {
				try {
					/*if(params.isOptimizedRead()){
						this.msg=readMsgOptimized(in);
					}else {*/
					log.info("Read a New PCEP Message!");
						this.msg = readMsg(in);//Read a new message	
					//}

				}catch (IOException e){
					cancelDeadTimer();
					cancelKeepAlive();
					timer.cancel();
					try {
						in.close();
						out.close();
					} catch (Exception e1) {
						log.warning("AYAYAYYA");
					}
					log.warning("Finishing PCEP Session abruptly!");
					return;
				}
				if (this.msg != null) {//If null, it is not a valid PCEP message								
					boolean pceMsg = true;//By now, we assume a valid PCEP message has arrived
					//Depending on the type a different action is performed
					log.info("PCEP Message - Switching Type");
					switch(PCEPMessage.getMessageType(this.msg)) {

					case PCEPMessageTypes.MESSAGE_OPEN:
						log.info("OPEN message received");
						//After the session has been started, ignore subsequent OPEN messages
						log.warning("OPEN message ignored");
						break;

					case PCEPMessageTypes.MESSAGE_KEEPALIVE:
						log.info("KEEPALIVE message received");
						//The Keepalive message allows to reset the deadtimer
						break;

					case PCEPMessageTypes.MESSAGE_CLOSE:
						log.info("CLOSE message received");

						try {
							PCEPClose m_close=new PCEPClose(this.msg);		
							log.warning("Closing due to reason "+m_close.getReason());
							this.killSession();
						} catch (PCEPProtocolViolationException e1) {
							log.warning("Problem decoding message, closing session"+e1.getMessage());
							this.killSession();
							return;
						}					
						return;

					case PCEPMessageTypes.MESSAGE_ERROR:
						log.info("ERROR message received");
						//Up to now... we do not do anything in the server side
						break;

					case PCEPMessageTypes.MESSAGE_NOTIFY:
						log.info("Received NOTIFY message");			
						PCEPNotification m_not;
						try {
							m_not=new PCEPNotification(this.msg);		
							notificationDispatcher.dispatchNotification(m_not);
						} catch (PCEPProtocolViolationException e1) {
							log.warning("Problem decoding notify message, ignoring message"+e1.getMessage());
							e1.printStackTrace();
						}						
						break;

					case PCEPMessageTypes.MESSAGE_PCREP:
						log.info("Received PC RESPONSE message");
						break;

					case PCEPMessageTypes.MESSAGE_PCREQ:
						log.info("PCREQ message received");
						long time1= System.nanoTime();
						lspManager.setTimeIni_Node(System.nanoTime());
						PCEPRequest p_req;
						
						try {
							p_req=new PCEPRequest(msg);
							log.info(p_req.toString());
						} catch (PCEPProtocolViolationException e) {
							e.printStackTrace();
							return;
						}
						int OFcode=p_req.getRequestList().get(0).getObjectiveFunction().getOFcode();
						log.info("OF: "+OFcode);
						
						boolean bidirectional = false;
						boolean error=false;
						float bw=0;
						Bandwidth bww=p_req.getRequestList().get(0).getBandwidth();
						if (bww!=null){
							if (bww instanceof BandwidthRequested) {
								bw=((BandwidthRequested)bww).getBw();
							}
						}
							//(float)10000000L;
						Inet4Address destinationId=((EndPointsIPv4)p_req.getRequest(0).getEndPoints()).getDestIP();
						long lsp_id = 0;
						long automatic_request_id = p_req.getRequestList().get(0).getRequestParameters().getRequestID();
						try {
							lsp_id = lspManager.addnewLSP(destinationId, bw, bidirectional, OFcode);
						} catch (LSPCreationException e) {
							error = true;
							log.info("Error when adding new LSP");
							switch(e.getErrorType()){
							case(LSPCreationErrorTypes.ERROR_REQUEST):{
								log.info("ERROR in Req");	    				
								break;
							}
							case(LSPCreationErrorTypes.NO_PATH):{
								log.info("ERROR: No PATH!");
								
								RequestParameters requestParameters = p_req.getRequestList().get(0).getRequestParameters();
								PCEPResponse resp = new  PCEPResponse();
								Response r = new Response();
								r.setRequestParameters(requestParameters);
								NoPath noPath = new NoPath();
								r.setNoPath(noPath);
								resp.addResponse(r);
								
								/*Response npResponse = resp.ResponseList.getFirst();
										
								PCEPResponse noPathMsg = new PCEPResponse();
								noPathMsg.getResponseList().add(npResponse);
										
								*/
								
								sendPCEPMessage(resp);
								
								break;
							}
							case(LSPCreationErrorTypes.NO_RESOURCES):{
								log.info("ERROR in Resources");	    				
								break;
							}
							case(LSPCreationErrorTypes.NO_RESPONSE):{
								log.info("ERROR in Response");	  
								
								//Poner un noPath al statistics
																
								PCEPError perror= new PCEPError();
								PCEPErrorObject perrorObject=new PCEPErrorObject();
								perror.getErrorObjList().add(perrorObject);				
								sendPCEPMessage(perror);		
								break;
							}
							default:{
								log.info("ERROR!!!!!!!!");
								System.exit(-1);
								break;
							}
							}
						}
						
						
						if (error == false){
							lspManager.waitForLSPaddition(lsp_id, 10000);
							
							LSPTE lsp=lspManager.getLSP(lsp_id, idRoadm);	
							
							
							log.info("LSP CORRECTO: ENVIO RESPONSE");
							RequestParameters requestParameters = lsp.getPcepResponse().getRequestParameters();
							requestParameters.setRequestID(automatic_request_id);
							lsp.getPcepResponse().setRequestParameters(requestParameters);
							PCEPResponse resp = new  PCEPResponse();
							resp.addResponse(lsp.getPcepResponse());
							
							log.info("LSP Created, sending message");
							lspIdsCorelation.put(automatic_request_id, lsp_id);
							sendPCEPMessage(resp);
							log.info("Message Send!");
						}
						break;
					case PCEPMessageTypes.MESSAGE_UPDATE:
						PCEPUpdate p_upd;
						try {
							p_upd=new PCEPUpdate(msg);
							UpdateRequest ur =p_upd.getUpdateRequestList().getFirst();
							LSP lsp_upd = ur.getLSP();
							log.info("El automatic lsp id es:" + lsp_upd.getLspId());
							//this.lspManager.deleteLSP(idRoadm, lspIdsCorelation.remove(lsp_upd.getLspId()));
							//sendPCEPMessage(p_upd);
							//log.info("Message sent correctly:" + lsp_upd.getLspId());
							//log.info(p_req.toString());
						} catch (PCEPProtocolViolationException e) {
							log.severe("PROBLEMON");
							e.printStackTrace();
							return;
						}
						break;
					case PCEPMessageTypes.MESSAGE_PCMONREQ:
						log.info("PCMonREQ message received");
						PCEPMonReq p_mon_req=new PCEPMonReq();
						try {
							p_mon_req.decode(msg);
						} catch (PCEPProtocolViolationException e) {
							e.printStackTrace();
							break;
						}
					case PCEPMessageTypes.MESSAGE_PCMONREP:
						log.info("PCMonREP message received");
						break;	
					default:
						log.warning("ERROR: unexpected message received");
						pceMsg = false;
					}

					if (pceMsg) {
						log.fine("Reseting Dead Timer as PCEP Message has arrived");
						resetDeadTimer();
					}
				} 
			}
		}finally{
			log.severe("SESSION "+ internalSessionID+" IS KILLED");
			this.FSMstate=PCEPValues.PCEP_STATE_IDLE;
			endSession();
		}

	}
	
	@Override
	protected void endSession() {
		// TODO Auto-generated method stub

	}

}
