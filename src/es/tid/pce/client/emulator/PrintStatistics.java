package es.tid.pce.client.emulator;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Print statistics to write in a file the emulation statistics 
 * Es una tarea que se ejecutara externamente
 * @author mcs
 *
 */
public class PrintStatistics   extends TimerTask {

	AutomaticTesterStatistics ats;
	private Logger statsLog;

	public PrintStatistics(AutomaticTesterStatistics ats){
		statsLog=LoggerFactory.getLogger("stats");
		this.ats=ats;
	}

	public void run(){
		statsLog.info(ats.print());
	}
	
}


