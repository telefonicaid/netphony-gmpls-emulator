package tid.emulator.node.topology;

import java.util.concurrent.LinkedBlockingQueue;

import es.tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;
import tid.netManager.TCPOSPFSender;


public class HandlerTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParametersPrueba params= new ParametersPrueba();	
		params.initialize("parametersPrueba.xml");	
		Handler handler = new Handler();
		handler.XMLRead(params.getNetworkFile());
		LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket> sendingQueue;
		TCPOSPFSender TCPOSPFsender = new TCPOSPFSender(params.getPCETEDBAddressList(),params.getOSPF_TCP_PORTList());
		TCPOSPFsender.start();		
		sendingQueue=TCPOSPFsender.getSendingQueue();
		handler.sendTopology(sendingQueue);
	}

}
