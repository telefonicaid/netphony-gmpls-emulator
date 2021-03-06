package es.tid.pce.client.emulator;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.emulator.node.transport.EmulatedPCCPCEPSession;
import es.tid.netManager.NetworkLSPManager;
import es.tid.netManager.NetworkLSPManagerParameters;
import es.tid.netManager.OSPFSender;
import es.tid.netManager.TCPOSPFSender;
import es.tid.netManager.emulated.AdvancedEmulatedNetworkLSPManager;
import es.tid.netManager.emulated.CompletedEmulatedNetworkLSPManager;
import es.tid.netManager.emulated.DummyEmulatedNetworkLSPManager;
import es.tid.netManager.emulated.SimpleEmulatedNetworkLSPManager;
import es.tid.ospf.ospfv2.OSPFv2LinkStateUpdatePacket;
import es.tid.pce.client.UserInterface;
import es.tid.pce.client.tester.InformationRequest;
import es.tid.pce.pcepsession.PCEPSessionsInformation;



	

public class TestClient_NetEmulated {

		private static InformationRequest testerParams;
		private static Hashtable<Integer,EmulatedPCCPCEPSession> PCEsessionList;
		private static UserInterface ui;
		private static EmulatedPCCPCEPSession PCEsession;
		public static final Logger Log =LoggerFactory.getLogger("PCCClient");
		private static String networkEmulatorFile="NetworkEmulatorConfiguration.xml";

		
		
		public static void main(String[] args) {
//			FileHandler fh;
//			FileHandler fh2;
			PCEPSessionsInformation PcepsessionManager = new PCEPSessionsInformation();
			try {
//				fh=new FileHandler("PCCClient.log");
//				fh2=new FileHandler("PCEPClientParser.log");
//				//fh.setFormatter(new SimpleFormatter());
//				Log.addHandler(fh);
//				Log.setLevel(Level.ALL);
				Logger log2=LoggerFactory.getLogger("PCEPParser");
//				log2.addHandler(fh2);
//				log2.setLevel(Level.ALL);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			}
			
			if (args.length < 2) {
				Log.info("Usage: ClientTester <host> <port>");
				return;
			}
			
			
			testerParams = new InformationRequest();
			testerParams.readFile("AutomaticTester.xml");
			String ip;
			int port;

			PCEsessionList= new Hashtable<Integer,EmulatedPCCPCEPSession>();
			ip = args[0];
			port = Integer.valueOf(args[1]).intValue();
			
			for (int i =0;i<testerParams.getPCCPCEPsessionParams().getNumSessions();i++){
				EmulatedPCCPCEPSession PCEsession = new EmulatedPCCPCEPSession(testerParams.getPCCPCEPsessionParams().getIpPCEList().get(i), testerParams.getPCCPCEPsessionParams().getPCEServerPortList().get(i), testerParams.getPCCPCEPsessionParams().isNoDelay(),PcepsessionManager );
				PCEsessionList.put(i, PCEsession);
				PCEsession.start();
			}
			
			NetworkLSPManager networkLSPManager = null;
			
			networkLSPManager = createNetworkLSPManager();
			Emulator emulator = new Emulator( testerParams, PCEsessionList,networkLSPManager, null, null);
			emulator.start();

		}
		
		 /**
		  * Create a Simple, advanced or complicated NetworkLSPManager 
		  * @param networkEmulatorParams 
		  * @return
		  */
		static NetworkLSPManager createNetworkLSPManager(){
			LinkedBlockingQueue<OSPFv2LinkStateUpdatePacket>  sendingQueue=null;
			NetworkLSPManagerParameters networkEmulatorParams= new NetworkLSPManagerParameters();
			networkEmulatorParams.initialize(networkEmulatorFile);
			NetworkLSPManager networkLSPManager=null;
			
			if (networkEmulatorParams.isOSPF_RAW_SOCKET()){
				OSPFSender ospfsender = new OSPFSender( networkEmulatorParams.getPCETEDBAddressList(), networkEmulatorParams.getAddress());
				ospfsender.start();	
				sendingQueue=ospfsender.getSendingQueue();
			}
			else {
				TCPOSPFSender TCPOSPFsender = new TCPOSPFSender(networkEmulatorParams.getPCETEDBAddressList(),networkEmulatorParams.getOSPF_TCP_PORTList());
				TCPOSPFsender.start();
				sendingQueue=TCPOSPFsender.getSendingQueue();
			}

			if (networkEmulatorParams.getNetworkLSPtype().equals("Simple")){
				networkLSPManager = new SimpleEmulatedNetworkLSPManager(sendingQueue, networkEmulatorParams.getNetworkFile() );
				
			} else if (networkEmulatorParams.getNetworkLSPtype().equals("Advanced")){
				networkLSPManager= new AdvancedEmulatedNetworkLSPManager(sendingQueue, networkEmulatorParams.getNetworkFile() );
			}
			else if (networkEmulatorParams.getNetworkLSPtype().equals("Completed")){
				networkLSPManager = new CompletedEmulatedNetworkLSPManager(sendingQueue, networkEmulatorParams.getNetworkFile(), null, networkEmulatorParams.isMultilayer() );
			}
			else if (networkEmulatorParams.getNetworkLSPtype().equals("dummy")){
				networkLSPManager = new DummyEmulatedNetworkLSPManager();
			}
			return networkLSPManager;
		}
		
	}
