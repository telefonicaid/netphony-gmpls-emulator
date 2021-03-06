package es.tid.pce.client.tester;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.Exponential;
import es.tid.netManager.NetworkLSPManager;
import es.tid.pce.client.emulator.AutomaticTesterStatistics;
import es.tid.pce.pcep.constructs.Path;
import es.tid.tedb.DomainTEDB;


public class LSPConfirmationDispatcher {
	
	private LinkedBlockingQueue<Path> pathList;
	
	private Logger log;
	
	private DomainTEDB tedb;
	
	private NetworkLSPManager networkLSPManager;
	
	private AutomaticTesterStatistics stats;
	private Exponential connectionTime;
	private Timer planificationTimer;
	private int IDLSP;
	
	public LSPConfirmationDispatcher(DomainTEDB tedb, NetworkLSPManager networkLSPManager, 
			AutomaticTesterStatistics stats){
		pathList=new LinkedBlockingQueue<Path>();
		this.stats=stats;
		this.tedb=tedb;
		this.networkLSPManager= networkLSPManager;
		
	}
	
	public void dispatchLSPConfirmation(Path path, int IDlsp){
		if (IDlsp == -1){
			stats.addStolenLambdasLSP();
			stats.analyzeLambdaBlockingProbability(1);
			stats.analyzeBlockingProbability(1);
		}
		else
			pathList.add(path);
	}

	public Exponential getConnectionTime() {
		return connectionTime;
	}

	public void setConnectionTime(Exponential connectionTime) {
		this.connectionTime = connectionTime;
		
		LSPConfirmationProcessorThread npt=new LSPConfirmationProcessorThread(pathList,tedb, networkLSPManager, 
				stats, connectionTime, planificationTimer);		
		
		npt.start();
		log=LoggerFactory.getLogger("PCCclient");
	}

	public Timer getPlanificationTimer() {
		return planificationTimer;
	}

	public void setPlanificationTimer(Timer planificationTimer) {
		this.planificationTimer = planificationTimer;
	}
}
