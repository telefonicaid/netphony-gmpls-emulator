package tid.emulator.pccPrueba;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Timer;
import java.util.logging.Logger;

import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPTELinkConfirmation;
import tid.emulator.node.transport.lsp.LSPManager;
import tid.pce.client.emulator.AutomaticTesterStatistics;
import tid.pce.computingEngine.ComputingResponse;
import tid.pce.pcepsession.DeadTimerThread;
import tid.pce.pcepsession.GenericPCEPSession;
import tid.pce.pcepsession.KeepAliveThread;
import tid.pce.pcepsession.PCEPSessionsInformation;
import tid.pce.server.NotificationDispatcher;
import tid.pce.server.PCEServerParameters;
import tid.pce.server.communicationpce.CollaborationPCESessionManager;

public class RemoteLSPInitPCEPSession extends GenericPCEPSession {
	
	private PCEServerParameters params;
	
	public  ClientRequestManagerPrueba crm;

	private NotificationDispatcher notificationDispatcher;
	
	private long internalSessionID;
	
	private static long lastInternalSessionID=0;

	private  CollaborationPCESessionManager collaborationPCESessionManager=null;
	
	private LSPManager lspManager;
	
	private Inet4Address idRoadm;
	
	private AutomaticTesterStatistics stats=null;
	
	private Socket serverSocket = null;
	
	/**
	 * Flag to indicate that the session is up
	 */
	private boolean running = true;
	
	public RemoteLSPInitPCEPSession(Socket socket, PCEPSessionsInformation pcepSessionManager, ClientRequestManagerPrueba crm) {
		super(pcepSessionManager);
		this.log=Logger.getLogger("ROADM");
		timer=new Timer();
		this.serverSocket=socket;
		this.crm=crm;
	}

	/**
	 * Initiates a Session between the Domain PCE and the peer PCC
	 */
	public void run() {
		
		running=true;
		
		/**
		 * Byte array to store the last PCEP message read.
		 */
		byte[] msg = null;
		//First get the input and output stream
		try {
		    out = new DataOutputStream(socket.getOutputStream());
		    in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			log.warning("Problem in the sockets, ending PCEPSession");
		    killSession();
		    return;
		}
		
		//initializePCEPSession(false, 15, 200,false,false,null,null);
		crm.setDataOutputStream(out);
		this.deadTimerT=new DeadTimerThread(this, this.deadTimerLocal);
		startDeadTimer();	
		this.keepAliveT=new KeepAliveThread(out, this.keepAliveLocal);
		startKeepAlive();
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
				log.warning("Finishing PCEP Session abruptly!");
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
					log.warning("OPEN message ignored");
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
					log.fine("Confirmation from the VNMT received!!!");
					//Establish the TE LINK in the UPPER LAYER
					PCEPTELinkConfirmation telinkconf;
					
					try {
						telinkconf = new PCEPTELinkConfirmation(this.msg);
						//LSPcreateIP.createLigthPath(telinkconf.getPath().geteRO().getEROSubobjectList());
						//LSPDispatcher.dispatchLSPConfirmation(telinkconf.getPath(), telinkconf.getLSPid());
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
					
				case PCEPMessageTypes.MESSAGE_PCREP:
					log.info("Received PCE RESPONSE message, FIX THE CODE");
					long timeIni=System.nanoTime();
					ComputingResponse pcres=new ComputingResponse();
//FIXME: THIS BLOCK DOESNT WORK, CHANGE IN THE FUTURE
//					try {
//						pcres.decode(msg);
//						
//						log.info("IdResponse: "+pcres.getResponse(0).getRequestParameters().getRequestID());
//						Object lock=crm.locks.get(new Long(pcres.getResponse(0).getRequestParameters().getRequestID()));
//						if (lock!=null){
//							synchronized (lock) {
//								crm.notifyResponse(pcres, timeIni);
//							}							
//						}
//						else{
//							log.warning("Ha llegado la request con ID: "+pcres.getResponse(0).getRequestParameters().getRequestID()+" Y el lock era null.");
//						}
//						
//					} catch (PCEPProtocolViolationException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						break;
//					}					
					break;
					
				case PCEPMessageTypes.MESSAGE_PCREQ:
					log.info("PCREQ message received");
					break;

				default:
					log.info("ERROR: unexpected message");
					pceMsg = false;
				}
				
				if (pceMsg) {
					log.info("Reseting Dead Timer as PCEP Message has arrived");
					resetDeadTimer();
				}
			}
		}

	}
	
	@Override
	protected void endSession() {
		// TODO Auto-generated method stub

	}

}
