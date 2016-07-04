package es.tid.emulator.pccPrueba;
public class RemoteAutomaticPCC {public void startRemoteAutomaticPCC() {}}
//FIXME: Class not working, uncomment to fix it
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package tid.emulator.pccPrueba;
//
//import java.io.BufferedReader;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Inet4Address;
//import java.net.Socket;
//import java.net.UnknownHostException;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import tid.pce.client.PCCPCEPSession;
//import tid.pce.client.tester.Activity;
//import tid.pce.client.tester.AutomaticClientTask;
//import tid.pce.client.tester.DummyActivity;
//import tid.pce.pcep.PCEPProtocolViolationException;
//import tid.pce.pcep.constructs.Request;
//import tid.pce.pcep.messages.PCEPRequest;
//import tid.pce.pcep.messages.PCEPResponse;
//import tid.pce.pcep.objects.Bandwidth;
//import tid.pce.pcep.objects.EndPointsIPv4;
//import tid.pce.pcep.objects.ObjectiveFunction;
//import tid.pce.pcep.objects.RequestParameters;
//import tid.pce.pcepsession.PCEPSessionsInformation;
//import cern.jet.random.Exponential;
//import cern.jet.random.engine.MersenneTwister;
//
//
//public class RemoteAutomaticPCC {
//
//	private ScheduledThreadPoolExecutor requestExecutor;
//	
//    public static Logger log,log2;
//    
//    private ClientRequestManagerPrueba crm; 
//    
//    private boolean running=false;
//    
//  
//   
//     private RemoteLSPInitPCEPSessionServer rlsserver;
//    /**
//     *
//     * Default constructor. Initializes all attributes
//     *
//     */
//
//    public RemoteAutomaticPCC(){
//    	
//    	// Create the Logs
//        log=LoggerFactory.getLogger("ROADM");
//
//		log2 = LoggerFactory.getLogger("PCCClient");
//		try{
//			FileHandler fh = new FileHandler("Roadm.log", false);
//			log.addHandler(fh);
//			FileHandler fh2 = new FileHandler("PCEPC.log", false);
//			log2.addHandler(fh2);
//		}catch(IOException e){
//		}
//		log.info("RemoteAutomaticPCC Created");
//             
//		//Automatic PCCNode Session
//		//crm = new ClientRequestManagerPrueba();
//		
//    }
//    
//    public void startRemoteAutomaticPCC() {
//    			
//		//Start the Automatic PCCNode Session
//		Thread sessionServer = new Thread(rlsserver);
//		sessionServer.start();
//		
//		running=true;
//		while (running) {
//			try {
//				Thread.sleep (1000); 
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("-------------");
//			System.out.println("Enter action: ");
//			System.out.println("-------------");
//			System.out.println(" 1- 192.168.1.1   to   192.168.1.18");
//		
//			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//	
//			String command = null;
//	
//			try {
//				command = br.readLine();
//			} catch (IOException ioe) {
//				log.error("IO error trying to read your command");
//				System.exit(1);
//			}
//			//New Requests
//			
//							// First Request //
//			if (command.equals("1")) {
//				PCEPRequest p_r = new PCEPRequest();
//				Request req = createRequest("192.168.1.1", "192.168.1.18");
//				
//				p_r.addRequest(req);
//				PCEPResponse pr = null;
//				crm = new ClientRequestManagerPrueba();
//				Socket socket = null;
//				rlsserver = new RemoteLSPInitPCEPSessionServer(((EndPointsIPv4)req.getEndPoints()).getSourceIP().toString(),
//						true, new PCEPSessionsInformation(), crm);
//				try {
//					log.info("Abriendo Socket");
//					socket = new Socket("192.168.1.1", 2222);
//					/*if (no_delay){
//						this.socket.setTcpNoDelay(true);
//						log.info("No delay activated");
//					}*/
//				} catch (IOException e) {
//					log.error("Couldn't get I/O for connection to in port 2222");
//					//FIXME: Salir de manera limpia
//					System.exit(1);
//				}
//				rlsserver.start();
//								
//				try {
//					p_r.encode();
//				} catch (PCEPProtocolViolationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				byte[] msg = null;
//				
//				msg = p_r.getBytes();
//				//First get the input and output stream
//				try {
//					log.info("Esperando wait 10 secs");
//					try {
//						Thread.sleep(10000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//					DataInputStream in = new DataInputStream(socket.getInputStream());
//					
//					out.write(msg);
//					
//					
//				} catch (IOException e) {
//					log.error("Problem in the sockets, ending PCEPSession");
//				    //killSession();
//				    return;
//				}
//				//pr = crm.newRequest(p_r);
//				
//			}
//		}
//		
//	}
//    
//    public Request createRequest(String src_ip, String dst_ip){
//		Request req = new Request();
//		RequestParameters rp= new RequestParameters();
//		rp.setPbit(true);
//		req.setRequestParameters(rp);		
//		rp.setRequestID(PCCPCEPSession.getNewReqIDCounter());
//		Bandwidth bw = new Bandwidth();
//		bw.setBw(100000000);
//		req.setBandwidth(bw);
//		int prio = 1;;
//		rp.setPrio(prio);
//		boolean reo = false;
//		rp.setReopt(reo);
//		boolean bi = false;
//		rp.setBidirect(bi);
//		boolean lo = false;
//		rp.setLoose(lo);
//		EndPointsIPv4 ep=new EndPointsIPv4();				
//		req.setEndPoints(ep);
//		//String src_ip= "1.1.1.1";
//		Inet4Address ipp;
//		try {
//			ipp = (Inet4Address)Inet4Address.getByName(src_ip);
//			ep.setSourceIP(ipp);								
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ObjectiveFunction of=new ObjectiveFunction();
//		of.setOFcode(1002);
//		req.setObjectiveFunction(of);
//		//br2 = new BufferedReader(new InputStreamReader(System.in));
//		//String dst_ip="172.16.101.101";
//		Inet4Address i_d;
//		try {
//			i_d = (Inet4Address)Inet4Address.getByName(dst_ip);
//			ep.setDestIP(i_d);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return req;
//	}
//    
//    public void createRequestsSource(int numberRequest){
//    	
//    	// Semilla que queremos
//		int semilla = 1000;
//		//MersenneTwister
//		MersenneTwister mersenneTwisterSendRequest = new MersenneTwister(semilla);
//		MersenneTwister mersenneTwisterConnectionTime = new MersenneTwister(semilla);
//		MersenneTwister mersenneTwisterFirstTBR = new MersenneTwister(semilla);
//		//Exponential
//		//Usada para programar cada cuanto de envia la misma peticion
//		Exponential expSendRequest=null;
//		//Usada para el tiempo de conexion, cu�nto estaran las peticiones activas
//		Exponential expConnectionTime = null;
//		//Usada para programar en que momento se enviara cada peticion que hemos programado
//		Exponential exponentialTime=null;
//		long expTime=0;
//		//Creo mi testeador
//		
//		// Exponencial Siempre
//		double MeanTimeBetweenRequest = 1800000;
//		double lambdaExpTime = 1/(MeanTimeBetweenRequest);
//		double lambdaSendRequest = 1/(MeanTimeBetweenRequest);
//		double lambdaConnectionTime = 1/(6000); // 6000 Mean Connection Time
//		expSendRequest = new Exponential(lambdaSendRequest, mersenneTwisterSendRequest);
//		expConnectionTime = new Exponential(lambdaConnectionTime, mersenneTwisterConnectionTime);
//		exponentialTime= new Exponential(lambdaExpTime, mersenneTwisterFirstTBR);
//			
//		requestExecutor = new ScheduledThreadPoolExecutor(3*numberRequest);		
//		for (int i=0;i< numberRequest; i++){   
//			Activity activity = null;   
//			
//			activity = new DummyActivity(expConnectionTime,planificationTimer);		
//			//activity.addStatistics(stats);
//			}
//
//			//int numberPCESession=findPCEPSessionToSendRequest(testerParams.getRequestToSendList().get(i).getSource());
//			//if (numberPCESession != -1){
//			double expTimeD=exponentialTime.nextDouble(); 
//			expTime =(long)expTimeD;
//			AutomaticClientTask automaticClientTask = new AutomaticClientTask(expSendRequest, requestExecutor, PCEsessionList,testerParams,stats,i,numberPCESession/*,System.nanoTime(), expTime*/, cadenaBW);
//				automaticClientTask.setThingsToDo(activity);
//				if (expSendRequest!=null){
//					//				try {
//					//					Thread.sleep(100);
//					//				} catch (InterruptedException e) {
//					//					// TODO Auto-generated catch block
//					//					e.printStackTrace();
//					//				}
//
//					//				logPrueba.info("Empieza en "+expTime);
//					requestExecutor.schedule(automaticClientTask, expTime,TimeUnit.MILLISECONDS);
//				}
//				else{
//					requestExecutor.schedule(automaticClientTask,Math.round (testerParams.getMeanTimeBetweenRequest()),TimeUnit.MILLISECONDS);
//				}
//			}
//		}
//		return;
//	}
//}
