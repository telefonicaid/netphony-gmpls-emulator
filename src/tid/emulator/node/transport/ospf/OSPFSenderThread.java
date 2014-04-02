package tid.emulator.node.transport.ospf;

import static com.savarese.rocksaw.net.RawSocket.PF_INET;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.savarese.rocksaw.net.RawSocket;

import tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;

public class OSPFSenderThread extends Thread{
	
	// Timeout para el socket
	private static final int TIMEOUT = 0;

	private Inet4Address NodeLocalAddress;
	
	private final String OSPFMulticastAddressString = "224.0.0.5";
	
	private Inet4Address OSPFMulticastAddress; 
	
	private LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue;
	
	Logger log=Logger.getLogger("OSPFParser");
	
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
			//FIXME: ESTE BIND ESTA A FUEGO
			//socket.bind(address);  /*InetAddress.getByName(address"172.16.1.1")*///Mi direccion
			socket.bind(NodeLocalAddress);  /*InetAddress.getByName(address"172.16.1.1")*///Mi direccion
			//dirPCE= (Inet4Address)Inet4Address.getByName(PCETEDBAddress/*"172.16.1.3"*/);//PCETEDBAddress
			log.info("Socket Opened!");
		}catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
		while (true){
			log.info("Socket is open??:" + socket.isOpen());
			try {
				
				OSPF_msg=sendingQueue.take();
			} catch (InterruptedException e) {
				log.severe("Exception tying to take a OSPF message from the sendingQueue in OSPFSender.");
				return;
			}
			try {
				//OSPFv2LinkStateUpdatePacket ospf_packet= new OSPFv2LinkStateUpdatePacket();
				//(ospf_packet.getLSAlist()).add(LSA_msg);
				//ospf_packet.encode();
				OSPF_msg.encode();
				//for (int i=0;i<PCETEDBAddressList.size();i++){
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
