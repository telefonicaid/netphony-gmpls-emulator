package es.tid.vntm;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import es.tid.tedb.DomainTEDB;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.MultiLayerTEDB;

public class LigthPathManagement extends TimerTask {
	
	private LigthPathCreateIP LSPDelete;
	private DomainTEDB ted; 
	
	private  Hashtable<Integer, IntraDomainEdge> LigthPathList;
	
	LigthPathManagement(){
		LigthPathList = new Hashtable<Integer, IntraDomainEdge>();
	}

	@Override
	public void run(){
		SimpleDirectedWeightedGraph<Object,IntraDomainEdge> graph_IP;
		graph_IP = ((MultiLayerTEDB)ted).getInterLayerGraph();
		
		Set<IntraDomainEdge> fiberEdges= graph_IP.edgeSet();
		Iterator<IntraDomainEdge> iterFiberLink;
		iterFiberLink = fiberEdges.iterator();
	
		while (iterFiberLink.hasNext()){
			IntraDomainEdge fiberEdge =iterFiberLink.next();
			float bw = fiberEdge.getTE_info().getUnreservedBandwidth().getUnreservedBandwidth()[0];
			if (bw == fiberEdge.getTE_info().getMaximumBandwidth().getMaximumBandwidth()){
				// Ligth path unused
								
				if (LigthPathList.get(fiberEdge)==null)
					LigthPathList.put(0, fiberEdge);
			}
		}
	}
}
