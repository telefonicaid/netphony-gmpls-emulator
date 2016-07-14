package es.tid.pce.client.tester;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.Exponential;
import es.tid.emulator.node.transport.EmulatedPCCPCEPSession;
import es.tid.netManager.NetworkLSPManager;
import es.tid.pce.client.emulator.AutomaticTesterStatistics;
import es.tid.pce.computingEngine.ComputingResponse;
import es.tid.pce.pcep.constructs.Path;
import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.pce.pcep.messages.PCEPResponse;
import es.tid.pce.pcep.objects.BandwidthRequestedGeneralizedBandwidth;
import es.tid.pce.pcep.objects.ExplicitRouteObject;
import es.tid.pce.pcep.objects.LSP;
import es.tid.rsvp.objects.subobjects.SubObjectValues;
import es.tid.rsvp.objects.subobjects.UnnumberIfIDEROSubobject;
import es.tid.vntm.LigthPathManagement;

public class DummyActivity implements Activity{
	private AutomaticTesterStatistics stats;
	private PCEPRequest request;
	private ComputingResponse response;
	private Logger log;
	private Exponential connectionTime;	
	private Timer planificationTimer;
	private Hashtable<Integer,EmulatedPCCPCEPSession> PCEsessionList = new Hashtable<Integer,EmulatedPCCPCEPSession>();
	private boolean staticConnections= false;
	
	public DummyActivity(Exponential connectionTime,Timer planificationTimer, Hashtable<Integer,EmulatedPCCPCEPSession> PCEsessionList, boolean staticConnections){
		this.connectionTime=connectionTime;
		this.planificationTimer=planificationTimer;
		log=LoggerFactory.getLogger("PCCClient");
		this.PCEsessionList=PCEsessionList;
		this.staticConnections=staticConnections;
//		logErrores=LoggerFactory.getLogger("PruebaLambdas");
//		logPrueba=LoggerFactory.getLogger("mmmerrores");
	}
	
	@Override
	public void run() {
		if (response == null){
			stats.addNoResponse();
			log.warn("Response null");			
			return;
		}
		if (response.getResponseList().isEmpty()){
			log.error("ERROR in response");
			System.exit(1);
		}else {
			try{
				if (response.getResponseList().get(0).getNoPath()!=null){
					log.info("NO PATH");
					stats.addNoPathResponse();
					stats.analyzeBlockingProbability(1);
					stats.analyzeblockProbabilityWithoutStolenLambda(1);
					return;	
				}else {
					log.info("Response actualizamos estadisticas");
					Path path=response.getResponseList().get(0).getPath(0);
					
					long id = response.getResponseList().getFirst().getRequestParameters().getRequestID();
					LinkedList <LSP> listLSP = new LinkedList <LSP>();
					LSP lsp = new LSP();
					lsp.setLspId((int)id);
					LinkedList <ExplicitRouteObject> eroList = new LinkedList <ExplicitRouteObject>();
					ExplicitRouteObject ero = path.geteRO();
					eroList.add(ero);
					//FIXME: Esta linea reventaba
					//lsp.set LspTLV(eroList);
					listLSP.add(lsp);
				
					
					/*long time1 = System.nanoTime();
					
					long time2= System.nanoTime();
					double LSPTime = (time2-time1)/1000000;
					stats.analyzeLSPTime(LSPTime);*/
					stats.addSLResponse();	
					stats.addNumberActiveLSP();
					stats.analyzeBlockingProbability(0);
					stats.analyzeLambdaBlockingProbability(0);
					stats.analyzeblockProbabilityWithoutStolenLambda(0);
					log.info("ERO atencion! : "+ero.getEROSubobjectList().get(0).toString());
					Inet4Address IDsession = null;
					if (ero.getEROSubobjectList().get(0).getType() == SubObjectValues.ERO_SUBOBJECT_UNNUMBERED_IF_ID){
						IDsession = ((UnnumberIfIDEROSubobject)ero.getEROSubobjectList().get(0)).getRouterID();
					}else
						IDsession = ((UnnumberIfIDEROSubobject)ero.getEROSubobjectList().get(1)).getRouterID();
					int sessionID = findPCEPSessionToSendRequest(IDsession);
					log.info("Session ID: "+sessionID);
					EmulatedPCCPCEPSession PCEPsession = PCEsessionList.get(sessionID);
					log.info("Ip dest --> "+PCEsessionList.get(sessionID).getPeerPCE_IPaddress());
					if (PCEsessionList==null){
						log.info("La lista es Null!");
					}else
						log.info("Lista : "+PCEsessionList.toString());
					if (PCEPsession==null){
						log.info("PCEP session null");
					}
					
					if (staticConnections==false){
						RealiseControlPlaneCapacityTask realiseCapacity = new RealiseControlPlaneCapacityTask(listLSP,stats,request.getRequestList().getFirst().getRequestParameters().isBidirect(),(BandwidthRequestedGeneralizedBandwidth)path.getBandwidth(), PCEPsession);
						long duration =Math.round(connectionTime.nextDouble());
						log.info("LSP duration: "+duration);
						planificationTimer.schedule(realiseCapacity,duration);
					}else{
						log.info("Conexiones estaticas en la red, no borramos LSPs!");
					}
					/*}else
					{
						//Logger logPrueba=LoggerFactory.getLogger("LogPrueba");
						stats.addStolenLambdasLSP();
						stats.analyzeLambdaBlockingProbability(1);
						stats.analyzeBlockingProbability(1);
					}*/			
				}
			}catch(Exception e){
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public int findPCEPSessionToSendRequest(Inet4Address source){		
		int size = PCEsessionList.size();		
		for (int i=0;i<size;i++){
			log.info("Source:"+source.toString()+" - PCE address " +PCEsessionList.get(i).getPeerPCE_IPaddress());
			Inet4Address sourceSocket;
			try {
				sourceSocket = ((Inet4Address)Inet4Address.getByName(PCEsessionList.get(i).getPeerPCE_IPaddress()));
				if (source.equals(sourceSocket))
					return i;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		log.info("return -1");
		return -1;
	
	}
		
	@Override
	public void addVNTMSession(EmulatedPCCPCEPSession VNTMSession) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNetworkEmulator(NetworkLSPManager networkLSPManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addStatistics(AutomaticTesterStatistics stats) {
		this.stats = stats;
		
	}

	@Override
	public void addRequest(PCEPRequest request) {
		this.request = request;
		
	}


	public void addResponse(ComputingResponse response) {
		log.info("Añadimos la response para actualizar estadisticas");
		this.response=response;
	}

	@Override
	public void addPCEsessionVNTM(EmulatedPCCPCEPSession vNTMSession) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLigthPathManagement(LigthPathManagement ligthPathManagement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addResponse(PCEPResponse response) {
		// TODO Auto-generated method stub
		
	}

}
