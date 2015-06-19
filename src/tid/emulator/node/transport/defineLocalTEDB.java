package tid.emulator.node.transport;

import java.net.Inet4Address;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import es.tid.tedb.InterDomainEdge;
import es.tid.tedb.IntraDomainEdge;

public class defineLocalTEDB {
	private static Logger log;
	// Podate Simple TEDB
	public static SimpleDirectedWeightedGraph<Object, IntraDomainEdge> podateGraph(SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph, Inet4Address RoadmID){
		log = Logger.getLogger("ROADM");
		SimpleDirectedWeightedGraph<Object,IntraDomainEdge> local_graph = new SimpleDirectedWeightedGraph<Object,IntraDomainEdge>(IntraDomainEdge.class);
		local_graph = (SimpleDirectedWeightedGraph<Object, IntraDomainEdge>) Graph.clone();
		Set<IntraDomainEdge> links = Graph.edgeSet();
		Iterator<IntraDomainEdge> iteredges = links.iterator();
		IntraDomainEdge link;
		while (iteredges.hasNext())
		{
			link = iteredges.next(); 
			if (!(link.getSource().equals(RoadmID))){
				local_graph.removeEdge(link);
			}
		}
		
		Set<Object> nodes = Graph.vertexSet();
		Iterator<Object> iternodes = nodes.iterator();
		Object node = null;
		boolean found = true;
		while (iternodes.hasNext())
		{
			node = iternodes.next();
			links = local_graph.edgeSet();
			iteredges = links.iterator();
			found = false;
			while (iteredges.hasNext() && (found == false))
			{
				link = iteredges.next();
				if (!((link.getSource()).equals(node)) && !((link.getTarget()).equals(node))){
					found = false;
				}
				else
					found = true;
			}
			if (found == false)
				local_graph.removeVertex(node);		
		}
		return local_graph;
	}
	
	// Podate Multi Domain TEDB
	public static DirectedWeightedMultigraph<Object, InterDomainEdge> podateMDGraph(DirectedWeightedMultigraph<Object, InterDomainEdge> Graph, Object RoadmID){
		DirectedWeightedMultigraph<Object,InterDomainEdge> local_graph = new DirectedWeightedMultigraph<Object,InterDomainEdge>(InterDomainEdge.class);
		local_graph = (DirectedWeightedMultigraph<Object, InterDomainEdge>) Graph.clone();
		Set<InterDomainEdge> links = Graph.edgeSet();
		Iterator<InterDomainEdge> iteredges = links.iterator();
		InterDomainEdge link;
		while (iteredges.hasNext())
		{
			link = iteredges.next(); 
			if (!(link.getSource().equals(RoadmID))){
				log.info("Podate Link: "+link.toString());
				local_graph.removeEdge(link);
			}
		}
		Set<Object> nodes = Graph.vertexSet();
		Iterator<Object> iternodes = nodes.iterator();
		Object node = null;
		boolean found = true;
		while (iternodes.hasNext())
		{
			node = iternodes.next();
			links = local_graph.edgeSet();
			iteredges = links.iterator();
			found = false;
			while (iteredges.hasNext() && (found == false))
			{
				link = iteredges.next();
				if (!((link.getSource()).equals(node)) && !((link.getTarget()).equals(node))){
					found = false;
				}
				else
					found = true;
			}
			if (found == false){
				log.info("Delete Node: "+node.toString());
				local_graph.removeVertex(node);
			}
		}
		return local_graph;
	}
}