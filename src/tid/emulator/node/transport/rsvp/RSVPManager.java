/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tid.emulator.node.transport.rsvp;

import static com.savarese.rocksaw.net.RawSocket.PF_INET;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import tid.emulator.node.resources.ResourceManager;
import tid.emulator.node.transport.lsp.LSPManager;
import tid.rsvp.messages.RSVPMessage;
import tid.rsvp.messages.RSVPMessageTypes;

import com.savarese.rocksaw.net.RawSocket;

/**
 * RSVP Manager 
 * @author fmn
 */
public class RSVPManager{

	
	private Inet4Address localIPAddress;
	private ResourceManager resourceManager;
	
    //	El Listener es el encargado de leer de la tarjeta para encolar los mensajes
    private RSVPListener listener;

    //	private Vector<RSVPSession> RSVPSessions;
    private RawSocket rsvpSocket;
    private RSVPProcessor rsvpProcessor;
    
    public RSVPProcessor getRsvpProcessor() {
		return rsvpProcessor;
	}

	public void setRsvpProcessor(RSVPProcessor rsvpProcessor) {
		this.rsvpProcessor = rsvpProcessor;
	}

	//	FIXME: Pensar en si merece la pena crear colas estandar para todo el nodo
    private  LinkedBlockingQueue<RSVPMessage> RSVPMessageQueue;
    
    //	Timeout para el socket
	private static final int TIMEOUT = 0;
    public static Logger log;
    private LSPManager managerLSP;
    private Vector<RSVPSession> RSVPSessions;
    
    /**
     * 
     * @param roadm
     */
    public RSVPManager(){
    	RSVPSessions = new Vector<RSVPSession>();
        RSVPMessageQueue = new LinkedBlockingQueue<RSVPMessage>();
    	log=Logger.getLogger("ROADM");
	}
    
    public void configureRSVPManager (Inet4Address localIPAddress, ResourceManager resourceManager, LSPManager managerLSP){
    	this.localIPAddress=localIPAddress;
    	this.resourceManager=resourceManager;
    	this.managerLSP=managerLSP;
    }
    
    public void startRSVPManager(){
    	//FIXME: Hacer comprobaciones
    	rsvpSocket = new RawSocket();
        int rsvpProtocolNumber = 46;
        try{
        	rsvpSocket.open(PF_INET,rsvpProtocolNumber);
        	if (!(rsvpSocket.isOpen())){
				log.info("Error el socket no se ha abierto");
			}
        	rsvpSocket.setUseSelectTimeout(true);
        	rsvpSocket.setSendTimeout(TIMEOUT);
        	rsvpSocket.setReceiveTimeout(TIMEOUT);
        	//FIXME: Direccion de la DCN
        	rsvpSocket.bind(localIPAddress);
        	       	
        	log.info("RSVP RawSocket Opened");
        }catch(IOException e1){
        	log.info("No se creo el RSVP socket\n");
        }
        listener= new RSVPListener(RSVPMessageQueue, rsvpSocket);
        listener.start();
        
       	rsvpProcessor = new RSVPProcessor(this, managerLSP, RSVPMessageQueue, /*OF,*/ resourceManager);
		rsvpProcessor.start();
    }
    
    /**
     * 
     * @param msg
     * @param addr
     */
    
    public synchronized void sendRSVPMessage(RSVPMessage msg,Inet4Address addr){
    	if (msg == null){
    		log.info("Mensaje RSVP es null!");
    	}
    	byte [] data = msg.getBytes();
    	if(RSVPMessage.getMsgType(data) == RSVPMessageTypes.MESSAGE_PATH){
			try{
	    		rsvpSocket.write(addr, data);
	    	}catch(IOException e){
	    		log.info("IOException sending RSVP Path Message");
	    	}
		}
		
		else if (RSVPMessage.getMsgType(data) == RSVPMessageTypes.MESSAGE_RESV)
		{
			try{
	    		rsvpSocket.write(addr, data);
	    	}catch(IOException e){
	    		log.info("IOException sending RSVP Resv Message");
	    	}
		}else if (RSVPMessage.getMsgType(data) == RSVPMessageTypes.MESSAGE_PATHTEAR){
			try{
	    		rsvpSocket.write(addr, data);
	    	}catch(IOException e){
	    		log.warning("IOException sending RSVP Path Tear Message");
	    	}
		}
	}
   
    public void createRSVPSession(String ip_dest, int port){
    	log.info("Crear sesion RSVP");
        RSVPSession session = new RSVPSession(ip_dest, port, false);
        RSVPSessions.add(session);
        session.start();
    }

	public RawSocket getRsvpSocket() {
		return rsvpSocket;
	}

	public void setRsvpSocket(RawSocket rsvpSocket) {
		this.rsvpSocket = rsvpSocket;
	}

    public void startRSVPListener(){
        listener = new RSVPListener(RSVPMessageQueue, rsvpSocket);
        listener.start();
    }
}