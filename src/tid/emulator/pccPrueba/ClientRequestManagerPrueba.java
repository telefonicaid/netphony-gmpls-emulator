package tid.emulator.pccPrueba;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Logger;

import tid.pce.computingEngine.ComputingResponse;
import tid.pce.pcep.PCEPProtocolViolationException;
import tid.pce.pcep.messages.PCEPMessage;
import tid.pce.pcep.messages.PCEPMonReq;
import tid.pce.pcep.messages.PCEPRequest;

public class ClientRequestManagerPrueba {
	//private PCCPCEPSession session;
	public Hashtable<Long,Object> locks;
	private Hashtable<Long,ComputingResponse> responses;
	private Logger log;
	private DataOutputStream out=null; //Use this to send messages to PCC
	private long lastTime;
	private Socket socket;
	
	public ClientRequestManagerPrueba(){
		//this.out=out;
		locks = new Hashtable<Long, Object>();
		responses=new Hashtable<Long, ComputingResponse>();
		log=Logger.getLogger("PCCClient");
	}
	public void notifyResponse(ComputingResponse pcres, long timeIni){
		lastTime=timeIni;
		long idRequest=pcres.getResponse(0).getRequestParameters().getRequestID();
		log.finer("Entrando en Notify Response");
		Object object_lock=locks.remove(new Long(idRequest));
		if (object_lock!=null){
			responses.put(new Long(idRequest), pcres);
			object_lock.notifyAll();	
		}
	}

	public void setDataOutputStream(DataOutputStream out){
		this.out=out;
	}
	public ComputingResponse newRequest( PCEPRequest pcreq){
		return newRequest(pcreq,30000);
	}
	
	public ComputingResponse newRequest( PCEPRequest pcreq, long maxTimeMs){
		log.info("New Request. Request:"+pcreq.toString());
		Object object_lock=new Object();		
		long idRequest=pcreq.getRequest(0).getRequestParameters().getRequestID();

		Long idReqLong=new Long(idRequest);
		long timeIni=System.nanoTime();
		locks.put(idReqLong, object_lock);
		sendRequest(pcreq);
		synchronized (object_lock) { 
			try {				
				log.fine("Request sent, waiting for response");
				log.info("ESPERAREMOS "+maxTimeMs);
				object_lock.wait(maxTimeMs);
			} catch (InterruptedException e){
			//	FIXME: Ver que hacer
			}
		}
		long timeIni2=System.nanoTime();
		//log.info("Response "+pr.toString());
		double reqTime_ms=(timeIni2-timeIni)/1000000;
		log.fine("Request or timeout");
		
		ComputingResponse resp=responses.remove(new Long(idRequest));
		if (resp==null){
			log.warning("NO RESPONSE!!!!! me deshago del lock... con idReqLong "+idRequest);
			locks.remove(idReqLong);
		}
		return resp;
	}
	
	public void sendRequest(PCEPRequest req){
		log.fine("Sending Request");
		log.info("Sending PCEP Request");
		sendPCEPMessage(req);
	}
	
	synchronized public  void sendPCEPMessage(PCEPMessage msg){
		try {
			msg.encode();
		} catch (PCEPProtocolViolationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			log.fine("Sending message");
			out.write(msg.getBytes());
			out.flush();
		} catch (IOException e) {
			log.warning("Error sending msg: " + e.getMessage());
		}
	}
	
	public ComputingResponse newRequest(PCEPMonReq pcreq){
		log.fine("New Request");
		Object object_lock=new Object();
		//RequestLock rl=new RequestLock();
		//((RequestParameters)(((Request)pcreq.getRequest(0)).getReqObject(0))).getRequestID();
		//long idRequest=((RequestParameters)(((Request)pcreq.getRequest(0)).getReqObject(0))).getRequestID();
		long idMonitoring=pcreq.getMonitoring().getMonitoringIdNumber();

		locks.put(new Long(idMonitoring), object_lock);
		sendRequest(pcreq);
		synchronized (object_lock) { 
			try {
				log.fine("Request sent, waiting for response");
				object_lock.wait(30000);
			} catch (InterruptedException e){
			//	FIXME: Ver que hacer
			}
		}
		log.fine("Request or timeout");
		
		ComputingResponse resp=responses.get(new Long(idMonitoring));
		if (resp==null){
			log.warning("NO RESPONSE!!!!!");
		}
		return resp;
	}

	synchronized public  void sendRequest(PCEPMonReq req){
		try {
			req.encode();
		} catch (PCEPProtocolViolationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			log.fine("Sending Request message");
			out.write(req.getBytes());
			out.flush();
		} catch (IOException e) {
			log.warning("Error sending REQ: " + e.getMessage());
		}
	}
}
