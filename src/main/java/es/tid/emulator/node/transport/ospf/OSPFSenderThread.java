package es.tid.emulator.node.transport.ospf;

import static es.tid.rocksaw.net.RawSocket.PF_INET;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.rocksaw.net.RawSocket;
import es.tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;

public class OSPFSenderThread extends Thread{
	
	// Timeout para el socket
	private static final int TIMEOUT = 0;

	private Inet4Address NodeLocalAddress;
	
	private final String OSPFMulticastAddressString = "224.0.0.5";
	
	private Inet4Address OSPFMulticastAddress; 
	
	private LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue;
	
	Logger log=LoggerFactory.getLogger("OSPFParser");
	
	public OSPFSenderThread (LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingqueue, Inet4Address NodeLocalAddress){
		this.sendingQueue=sendingqueue;
		this.NodeLocalAddress = NodeLocalAddress;
		try{
			OSPFMulticastAddress = (Inet4Address)Inet4Address.getByName(OSPFMulticastAddressString);
		}catch(Exception e){
		}
	}
	
	public void run(){
		OSPFv2LinkStateUpdatePacket OSPF_msg;
		RawSocket socket = new RawSocket();
		
		try{
			socket.open(PF_INET, 89);
			socket.setUseSelectTimeout(true);
			socket.setSendTimeout(TIMEOUT);
			socket.setReceiveTimeout(TIMEOUT);
			socket.bind(NodeLocalAddress);
			log.info("Raw Socket Opened for OSPF with local address binded to "+NodeLocalAddress);
		}catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
		while (true){
			log.info("Socket is open??:" + socket.isOpen());
			try {
				
				OSPF_msg=sendingQueue.take();
			} catch (InterruptedException e) {
				log.error("Exception tying to take a OSPF message from the sendingQueue in OSPFSender.");
				return;
			}
			try {
				//OSPFv2LinkStateUpdatePacket ospf_packet= new OSPFv2LinkStateUpdatePacket();
				//(ospf_packet.getLSAlist()).add(LSA_msg);
				//ospf_packet.encode();
				OSPF_msg.encode();
				//for (int i=0;i<PCETEDBAddressList.size();i++){
				//log.info("Going to write to "+OSPFMulticastAddress+" "+OSPF_msg.getBytes().length +" bytes" );
				socket.write(OSPFMulticastAddress,OSPF_msg.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	public LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> getSendingQueue() {
		return sendingQueue;
	}

	public void setSendingQueue(LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue) {
		this.sendingQueue = sendingQueue;
	}
}
