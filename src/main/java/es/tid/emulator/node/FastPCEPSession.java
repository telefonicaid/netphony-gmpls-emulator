package es.tid.emulator.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.emulator.node.transport.LSPCreationException;
import es.tid.emulator.node.transport.lsp.LSPCreationErrorTypes;
import es.tid.emulator.node.transport.lsp.LSPManager;
import es.tid.emulator.node.transport.lsp.te.LSPTE;
import es.tid.pce.client.emulator.AutomaticTesterStatistics;
import es.tid.pce.pcep.PCEPProtocolViolationException;
import es.tid.pce.pcep.constructs.Path;
import es.tid.pce.pcep.constructs.StateReport;
import es.tid.pce.pcep.constructs.UpdateRequest;
import es.tid.pce.pcep.messages.PCEPError;
import es.tid.pce.pcep.messages.PCEPInitiate;
import es.tid.pce.pcep.messages.PCEPMessage;
import es.tid.pce.pcep.messages.PCEPMessageTypes;
import es.tid.pce.pcep.messages.PCEPReport;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.messages.PCEPUpdate;
import es.tid.pce.pcep.objects.Bandwidth;
import es.tid.pce.pcep.objects.BandwidthRequested;
import es.tid.pce.pcep.objects.EndPointsIPv4;
import es.tid.pce.pcep.objects.EndPointsUnnumberedIntf;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.pcep.objects.GeneralizedEndPoints;
import es.tid.pce.pcep.objects.LSP;
import es.tid.pce.pcep.objects.PCEPErrorObject;
import es.tid.pce.pcep.objects.RequestParameters;
import es.tid.pce.pcep.objects.SRP;
import es.tid.pce.pcep.objects.tlvs.PathSetupTLV;
import es.tid.rsvp.objects.ERO;
   
/**
 * 
 * @author ogondio
 *
 */
public class FastPCEPSession extends Thread{
	
	/**
	 * Socket of the communication between PCC and PCE
	 */
	private Socket socket = null; 
	
	/**
	 * 
	 */
	private DataOutputStream out=null;
	
	/**
	 * 
	 */
	private DataInputStream in;

	/**
	 * Byte array to store the PCEP message read.
	 */
	private byte[] msg = null;
	
	private Logger log;
	
	private LSPManager lspManager;
	
	private Inet4Address idRoadm;
	
	private int nodeTechnology;
	
	private AutomaticTesterStatistics stats;
	public FastPCEPSession(Socket s, LSPManager lspManager, Inet4Address idRoadm, int nodeTechnology, AutomaticTesterStatistics stats){
		this.socket=s;
		this.idRoadm=idRoadm;	
		log=LoggerFactory.getLogger("ROADM");
		//log.setLevel(Level.INFO);
		this.lspManager=lspManager;
		this.nodeTechnology=nodeTechnology;
		this.stats = stats;
	}
	

	public void run(){
		log.info("Staring new Fast PCEP session");
		
		//First get the input and output stream
		try {
		    out = new DataOutputStream(socket.getOutputStream());
		    in = new DataInputStream(socket.getInputStream());
		    msg = readMsg(in);
		} catch (IOException e) {
			log.info("Problem in the sockets, ending Fast PCEP Session");
		    return;
		}
		log.info("New Fast PCEP Session to create/delete LSPs from "+socket.getInetAddress());
		int messageType=PCEPMessage.getMessageType(this.msg);
		
		if (messageType==PCEPMessageTypes.MESSAGE_PCREQ){
			log.info("LSP Creation Request received");
			PCEPRequest p_req;
			
			try {
				log.info("El msg que obtenemos es: "+ msg + "---");
				p_req=new PCEPRequest(msg);
				log.info(p_req.toString());
				
			} catch (PCEPProtocolViolationException e) {
				e.printStackTrace(); 
				return;
			}
			int OFcode=p_req.getRequestList().get(0).getObjectiveFunction().getOFcode();
			boolean bidirectional = false;
			float bw=0;
			Bandwidth bww=p_req.getRequestList().get(0).getBandwidth();
			if (bww!=null){
				if (bww instanceof BandwidthRequested) {
					bw=((BandwidthRequested)bww).getBw();
				}
			}
			Inet4Address destinationId=((EndPointsIPv4)p_req.getRequest(0).getEndPoints()).getDestIP();
			long lsp_id = 0;
			try {
				lsp_id = lspManager.addnewLSP(destinationId, bw, bidirectional, OFcode);
				long time1= System.nanoTime();
				lspManager.waitForLSPaddition(lsp_id, 10000);
				LSPTE lsp=lspManager.getLSP(lsp_id, idRoadm);	
				
				
				if (lsp==null){
					log.info("LSP ES NULL: ENVIO ERROR");
					//Poner un noPath al statistics
					if (stats != null){//Adding Statistics
						stats.addStolenLambdasLSP();
						stats.analyzeLambdaBlockingProbability(1);
						stats.analyzeBlockingProbability(1);			
					}
					
					PCEPError perror= new PCEPError();
					PCEPErrorObject perrorObject=new PCEPErrorObject();
					perror.getErrorObjList().add(perrorObject);				
					sendPCEPMessage(perror);				
				}
				else {
					if (stats != null){//Adding Statistics
						long time2= System.nanoTime();
						double LSPTime = (time2-time1)/1000000;
						stats.analyzeLSPTime(LSPTime);
						stats.addSLResponse();	
						stats.addNumberActiveLSP();
						stats.analyzeBlockingProbability(0);
						stats.analyzeLambdaBlockingProbability(0);
						stats.analyzeblockProbabilityWithoutStolenLambda(0);
					}
					log.info("LSP CORRECTO: ENVIO RESPONSE");
					PCEPResponse resp = new  PCEPResponse();
					resp.addResponse(lsp.getPcepResponse());
					if (resp.ResponseList.getFirst().getNoPath()!=null){
						log.info("No PATH");
						sendPCEPMessage(resp);
					}
					else {
						RequestParameters rp = resp.getResponse(0).getRequestParameters();
						//rp.setRequestID(p_req.getRequest(0).getRequestParameters().getRequestID());
						rp.setRequestID(lsp_id);
						log.info("LSP Created, sending message");
						sendPCEPMessage(resp);
						log.info("Message Send!");
					}
				}
			} catch (LSPCreationException e) {
				log.info("Error when adding new LSP");
				sendPCEPErrorMessage(e.getErrorType());
				switch(e.getErrorType()){
				case(LSPCreationErrorTypes.ERROR_REQUEST):{
					log.info("ERROR in Req");	    				
					break;
				}
				case(LSPCreationErrorTypes.NO_PATH):{
					log.info("ERROR in Path");	    				
					break;
				}
				case(LSPCreationErrorTypes.NO_RESOURCES):{
					log.info("ERROR in Resources");	    				
					break;
				}
				case(LSPCreationErrorTypes.NO_RESPONSE):{
					log.info("ERROR in Response");	    				
					break;
				}
				default:{
					log.info("ERROR!!!!!!!!");
					System.exit(-1);
					break;
				}
				}
			}
			
	
		}else if(messageType==PCEPMessageTypes.MESSAGE_UPDATE){
			
			PCEPUpdate p_upd;
			try {
				p_upd=new PCEPUpdate(msg);
				UpdateRequest ur =p_upd.getUpdateRequestList().getFirst();
				LSP lsp = ur.getLsp();
				log.info("El lsp id es:" + lsp.getLspId());
				
				this.lspManager.deleteLSP(idRoadm, (int)lsp.getLspId());
				sendPCEPMessage(p_upd);
				log.info("Message sent correctly:" + lsp.getLspId());
				//log.info(p_req.toString());
			} catch (PCEPProtocolViolationException e) {
				log.error("PROBLEMON");
				e.printStackTrace();
				return;
			}
			
		}
		else if(messageType==PCEPMessageTypes.MESSAGE_INITIATE){
			log.info("MESSAGE_INTIATE Received!!");
			
			try {
				PCEPInitiate p_init = new PCEPInitiate(msg);
				lspManager.setStateful(true);

				long lsp_id = 1234568;
				p_init=new PCEPInitiate(msg);
				lspManager.setStateful(true);

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
					ExplicitRouteObject ero = p_init.getPcepIntiatedLSPList().get(0).getEro();

					ERO eroOther = new ERO();

					eroOther.setEroSubobjects(ero.getEROSubobjectList());

					//lspManager.startLSP(lsp, eroOther);

					log.info("jm ver tipo destino: "+p_init.getPcepIntiatedLSPList().get(0).getEndPoint().getClass().getName());
					//Inet4Address destinationId=((EndPointsIPv4)p_init.getPcepIntiatedLSPList().get(0).getEndPoint()).getDestIP();
					
					Inet4Address destinationId = null;
					destinationId = (Inet4Address)  Inet4Address.getByName(getDestinationIP(p_init.getPcepIntiatedLSPList().get(0).getEndPoint()));
					
					lspManager.setFastSession(this);
					lsp_id = lspManager.addnewLSP(destinationId, 1000, false, 1002,eroOther);
					long time1= System.nanoTime();
					lspManager.waitForLSPaddition(lsp_id, 10000);
					LSPTE lsp=lspManager.getLSP(lsp_id, idRoadm);	


					lspManager.notifyLPSEstablished(lsp_id, lspManager.getLocalIP());


					//UpdateRequest ur =p_init.getUpdateRequestList().getFirst();		
					//log.info(p_req.toString());

				}				
				
				
				/*
				 * ANTIGUO
				 * 
				PCEPInitiate p_init = new PCEPInitiate(msg);
				lspManager.setStateful(true);
				
				long lsp_id = 1234568;
				p_init=new PCEPInitiate(msg);
				lspManager.setStateful(true);
				
				//LSPTE lsp = new LSPTE(lsp_id, lspManager.getLocalIP(), ((EndPointsIPv4)p_init.getPcepIntiatedLSPList().get(0).getEndPoint()).getDestIP(), false, 1001, 10000, PathStateParameters.creatingLPS);
				
				ExplicitRouteObject ero = p_init.getPcepIntiatedLSPList().get(0).getEro();
				
				ERO eroOther = new ERO();
				
				eroOther.setEroSubobjects(ero.getEROSubobjectList());
				
				//lspManager.startLSP(lsp, eroOther);
				
				
				Inet4Address destinationId=((EndPointsIPv4)p_init.getPcepIntiatedLSPList().get(0).getEndPoint()).getDestIP();
				lspManager.setFastSession(this);
				lsp_id = lspManager.addnewLSP(destinationId, 1000, false, 1002,eroOther);
				long time1= System.nanoTime();
				lspManager.waitForLSPaddition(lsp_id, 10000);
				LSPTE lsp=lspManager.getLSP(lsp_id, idRoadm);	
				
				
				//lspManager.notifyLPSEstablished(lsp_id, lspManager.getLoca lIP());
					
				
				//UpdateRequest ur =p_init.getUpdateRequestList().getFirst();		
				//log.info(p_req.toString());
				*/
				
			} catch (Exception e) {
				log.error("PROBLEMON");
				e.printStackTrace();
				return;
			}
			
		}
		else{
		
			log.info("Only requests are accepted now");
			closeSession();
		}		
	}
	
	/**
	 * Read PCE message from TCP stream
	 * @param in InputStream
	 */
	private byte[] readMsg(DataInputStream in) throws IOException{
		byte[] ret = null;
		
		byte[] hdr = new byte[4];
		byte[] temp = null;
		boolean endHdr = false;
		int r = 0;
		int length = 0;
		boolean endMsg = false;
		int offset = 0;
		
		while (!endMsg) {
			try {
				if (endHdr) {
					r = in.read(temp, offset, 1);
				}
				else {
					r = in.read(hdr, offset, 1);
				}
			} catch (IOException e){
				log.warn("Error reading data: "+ e.getMessage());
				throw e;
		    }catch (Exception e) {
				log.warn("readMsg Oops: " + e.getMessage());
				throw new IOException();
			}
		    
			if (r > 0) {
				if (offset == 2) {
					length = ((int)hdr[offset]&0xFF) << 8;
				}
				if (offset == 3) {
					length = length | (((int)hdr[offset]&0xFF));
					temp = new byte[length];
					endHdr = true;
					System.arraycopy(hdr, 0, temp, 0, 4);
				}
				if ((length > 0) && (offset == length - 1)) {
					endMsg = true;
				}
				offset++;
			}
			else if (r==-1){
				log.warn("End of stream has been reached");
				throw new IOException();
			}
		}
		if (length > 0) {
			ret = new byte[length];
			System.arraycopy(temp, 0, ret, 0, length);
		}		
		return ret;
	}
	
	private void closeSession(){
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (this.socket != null) {
				log.warn("Closing socket");
				this.socket.close();
			}
			
		} catch (Exception e) {
			log.warn("Error closing connections: " + e.getMessage());
		}
	}
	
	public AutomaticTesterStatistics getStatistics() {
		return stats;
	}


	public void setStatistics(AutomaticTesterStatistics statistics) {
		this.stats = statistics;
	}


	public void sendPCEPMessage(PCEPMessage message) {
		try {
			message.encode();
		} catch (Exception e11) {
			log.error("ERROR ENCODING ERROR OBJECT, BUG DETECTED, INFORM!!! "+e11.getMessage());
			log.error("Ending Session");
			closeSession();
		}
		try {			
			out.write(message.getBytes());
			out.flush();
		} catch (IOException e) {
			log.error("Problem writing message, finishing session "+e.getMessage());
			closeSession();
		}
	}
	
	//This function sends a error message, which must contains the error type.
	public void sendPCEPErrorMessage (int ErrorType){
		//FIXME: Falta por definir los tipos de error en funcion de las nuevas RFCs.
		PCEPError perror= new PCEPError();
		PCEPErrorObject perrorObject=new PCEPErrorObject();
		perrorObject.setErrorValue(ErrorType);
		perror.getErrorObjList().add(perrorObject);				
		sendPCEPMessage(perror);
		
	}
	
public String getDestinationIP(Object endPoint) {
		
		String destinationIP=null;
		
		if (endPoint == null){
			log.info("jm endPoint es null");
			
		}else if (endPoint instanceof EndPointsIPv4){
			log.info("jm endPoint es de tipo EndPointsIPv4");
			destinationIP = ((EndPointsIPv4) endPoint).getDestIP().toString();
			
			
		}else if (endPoint instanceof EndPointsUnnumberedIntf){
			log.info("jm endPoint es de tipo EndPointsUnnumberedIntf");
			destinationIP = ((EndPointsUnnumberedIntf) endPoint).getDestIP().toString();
			
		}else if (endPoint instanceof GeneralizedEndPoints){
			log.info("jm endPoint es de tipo GeneralizedEndPoints");
			destinationIP = ((GeneralizedEndPoints) endPoint).getP2PEndpoints().getDestinationEndPoint().toString();
			
		}else log.info("jm endPoint NO es de tipo conocido");
		
		return destinationIP;
	}
	
}
