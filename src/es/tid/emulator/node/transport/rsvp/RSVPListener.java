/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.tid.emulator.node.transport.rsvp;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import es.tid.rocksaw.net.RawSocket;

import es.tid.emulator.node.NetworkNode;
import es.tid.rsvp.RSVPProtocolViolationException;
import es.tid.rsvp.messages.RSVPMessage;
import es.tid.rsvp.messages.RSVPMessageTypes;
import es.tid.rsvp.messages.RSVPPathTearMessage;
import es.tid.rsvp.messages.te.RSVPTEPathMessage;
import es.tid.rsvp.messages.te.RSVPTEResvMessage;

/**
 * Takes care of every RSVP message that the ROADM receives.
 * @author fmn
 */
public class RSVPListener extends Thread{
	
	private static final int TIMEOUT = 0;
	private RawSocket socket;
    private NetworkNode roadm;
    public static Logger log;
    private int tipo;
    private LinkedBlockingQueue<RSVPMessage> RSVPMessageQueue;

    public RSVPListener(LinkedBlockingQueue<RSVPMessage> RSVPMessageQueue,RawSocket socket ){
    	this.socket=socket;
    	this.RSVPMessageQueue=RSVPMessageQueue;
    	log=Logger.getLogger("ROADM");
    }

    private int getTipo(){
        return tipo;
    }
    
	public void run(){
    	log.info("Listener RSVP Started");
    	boolean running = true;
    	
   	    while(running){
   	    	
    		try{
    			
        		int bufferSize = socket.getReceiveBufferSize();
           		byte[] data2 = new byte[bufferSize];
    			
        		socket.read(data2);
        		
           		byte[] data = new byte[bufferSize-20];	// Cabecera IP fuera
        		
           		System.arraycopy(data2, 20, data, 0, bufferSize-20);
           		
           		int messageType = RSVPMessage.getMsgType(data);
        		int length = RSVPMessage.getMsgLength(data);
        		    			
        		switch(messageType){
        			case RSVPMessageTypes.MESSAGE_PATH:
        				log.info("RSVP-TE Path Message Received");
        				        				        				
        				RSVPTEPathMessage path = new RSVPTEPathMessage(data, length);
        				try{
        					path.decode();
        					RSVPMessageQueue.add(path);
        					
        				}catch(RSVPProtocolViolationException e){
        					log.severe("Failure decoding RSVP-TE Path Message");
        				}
        				break;
        			case RSVPMessageTypes.MESSAGE_RESV:
        				log.info("RSVP-TE Resv Message Received");
        				RSVPTEResvMessage resv = new RSVPTEResvMessage(data, length);
        				try{
        					resv.decode();
        					RSVPMessageQueue.add(resv);
        				}catch(RSVPProtocolViolationException e){
        					log.severe("Failure decoding RSVP-TE Resv Message");
        				}
        				break;
        			case RSVPMessageTypes.MESSAGE_PATHTEAR:
        				log.info("RSVP-TE Path Tear Message Received");
        				RSVPPathTearMessage tear = new RSVPPathTearMessage(data, length);
        				try{
        					tear.decode();
        					RSVPMessageQueue.add(tear);
        				}catch(RSVPProtocolViolationException e){
        					log.severe("Failure decoding RSVP-TE Path Tear Message");
        				}
        				break;
        			default:
        				log.severe("Unrecongizable RSVP Message");
        				break;
        		}
    		}catch(IOException e){
        		e.printStackTrace();
        		System.exit(0);
        	}
    	}
    }
}
