package es.tid.emulator.node.tedb;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import es.tid.ospf.ospfv2.lsa.tlv.subtlv.MaximumReservableBandwidth;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.SimpleTEDB;
/**
 * Traffic Engineering Database of a Domain.
 *
 * @author smta
 *
 */
public class SimpleLocalTEDB extends SimpleTEDB{
		
	private Lock TEDBlock;
	private Logger log;
		
	public SimpleLocalTEDB(){
		TEDBlock=new ReentrantLock();
		log = Logger.getLogger("ROADM");
	}
	
	//Check resources SSON
	public boolean CheckLocalResources(long InterfaceID, int n, int M){
		SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
		Set<IntraDomainEdge> links = Graph.edgeSet();
		Iterator<IntraDomainEdge> iteredges = links.iterator();
		IntraDomainEdge link;
		while (iteredges.hasNext())
		{
			link = iteredges.next();
			if (link.getSrc_if_id() == InterfaceID)
			{
				log.info("n: "+n+", M: "+M);
				log.info("Bitmap edge: "+link.toString());
				log.info("Encontramos el Link");
				for (int i=n-M; i<=(n+M-1); i++){
					if (link.getTE_info().isWavelengthFree(i)){
						log.info("Lambda "+i+" is free!");
						continue;
					}
					else{
						log.info("Lambda "+i+" is not free!");
						return false;
					}
				}
				return true;
			}
		}
		log.info("No se ha encontrado el link");
		return false;		 
	}
	// Check Resources WSON
	public boolean CheckLocalResources(long InterfaceID, int n){
		
		SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
		
		Set<IntraDomainEdge> links = Graph.edgeSet();
		Iterator<IntraDomainEdge> iteredges = links.iterator();
		IntraDomainEdge link;
		while (iteredges.hasNext())
		{
			link = iteredges.next(); 
			if (link.getSrc_if_id() == InterfaceID)
			{
				if (link.getTE_info().isWavelengthFree(n))
					return true;
				else
					return false;
			}
		}
		return false;
	}
	
	//Check resources MPLS
	public boolean CheckLocalResources(long InterfaceID, float bw){
		SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
		Set<IntraDomainEdge> links = Graph.edgeSet();
		Iterator<IntraDomainEdge> iteredges = links.iterator();
		IntraDomainEdge link;
		while (iteredges.hasNext())
		{
			link = iteredges.next();
			
			if (link.getSrc_if_id() == InterfaceID)
			{
				if (link.getTE_info().getMaximumReservableBandwidth().maximumReservableBandwidth>= bw)
					return true;
				else
					return false;
			}
		}
		return false;		 
	}
	
	// Resources Confirmation SSON
	public boolean AddResourcesConfirmation(long InterfaceID, int n, int M){
		this.getTEDBlock().lock();
		try {
			SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
			
			log.info("N: "+n+" M: "+M);
			Set<IntraDomainEdge> links = Graph.edgeSet();
			Iterator<IntraDomainEdge> iteredges = links.iterator();
			IntraDomainEdge link;
			while (iteredges.hasNext())
			{
				link = iteredges.next(); 
				if (link.getSrc_if_id() == InterfaceID)
				{					
					for (int i=n-M; i<=(n+M-1); i++){
						link.getTE_info().setWavelengthOccupied(i);
					}
					if (M==0){
						link.getTE_info().setWavelengthOccupied(n);
						log.info("Reserving only one bit");
					}
					return true;
				}
			}
		}finally{
			this.getTEDBlock().unlock();
		}
		return false;		 
	}
	// Resources Confirmation MPLS
	public boolean AddResourcesConfirmation(long InterfaceID, float bw){
		this.getTEDBlock().lock();
		try {
			SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
			
			Set<IntraDomainEdge> links = Graph.edgeSet();
			Iterator<IntraDomainEdge> iteredges = links.iterator();
			IntraDomainEdge link;
			while (iteredges.hasNext())
			{
				link = iteredges.next(); 
				if (link.getSrc_if_id() == InterfaceID)
				{					
					float bw_link = (link.getTE_info().getMaximumReservableBandwidth().maximumReservableBandwidth);
					bw_link = bw_link - bw;
					MaximumReservableBandwidth maximumReservableBandwidth = null;
					maximumReservableBandwidth.setMaximumReservableBandwidth(bw_link);
					link.getTE_info().setMaximumReservableBandwidth(maximumReservableBandwidth);
					return true;
				}
			}
		}finally{
			this.getTEDBlock().unlock();
		}
		return false;		 
	}
	
	// Resources Confirmation WSON
	public boolean AddResourcesConfirmation(long InterfaceID, int n){
		this.getTEDBlock().lock();
		try {
			SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
			
			Set<IntraDomainEdge> links = Graph.edgeSet();
			Iterator<IntraDomainEdge> iteredges = links.iterator();
			IntraDomainEdge link;
			while (iteredges.hasNext())
			{
				link = iteredges.next(); 
				if (link.getSrc_if_id() == InterfaceID)
				{					
					link.getTE_info().setWavelengthOccupied(n);
					return true;
				}
			}
		}finally{
			this.getTEDBlock().unlock();
		}
		return false;	 
	}
	// Free Resources WSON
	public boolean FreeResourcesConfirmation(long InterfaceID, int n){
		this.getTEDBlock().lock();
		try {
			SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
			
			Set<IntraDomainEdge> links = Graph.edgeSet();
			Iterator<IntraDomainEdge> iteredges = links.iterator();
			IntraDomainEdge link;
			while (iteredges.hasNext())
			{
				link = iteredges.next(); 
				if (link.getSrc_if_id() == InterfaceID)
				{					
					link.getTE_info().setWavelengthFree(n);
					return true;
				}
			}
		}finally{
			this.getTEDBlock().unlock();
		}
		return false;
	}
	// Free Resources SSON
	public boolean FreeResourcesConfirmation(long InterfaceID, int n, int M){
		this.getTEDBlock().lock();
		try {
			SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
			
			Set<IntraDomainEdge> links = Graph.edgeSet();
			Iterator<IntraDomainEdge> iteredges = links.iterator();
			IntraDomainEdge link;
			while (iteredges.hasNext())
			{
				link = iteredges.next(); 
				if (link.getSrc_if_id() == InterfaceID)
				{					
					for (int i=n-M; i<=(n+M-1); i++){
						link.getTE_info().setWavelengthFree(i);
					}
					return true;
				}
			}
		}finally{
			this.getTEDBlock().unlock();
		}
		return false;
	}
	
	// Free Resources MPLS
	public boolean FreeResourcesConfirmation(long InterfaceID, float bw){
		this.getTEDBlock().lock();
		try {
			SimpleDirectedWeightedGraph<Object, IntraDomainEdge> Graph = this.getNetworkGraph();
			
			Set<IntraDomainEdge> links = Graph.edgeSet();
			Iterator<IntraDomainEdge> iteredges = links.iterator();
			IntraDomainEdge link;
			while (iteredges.hasNext())
			{
				link = iteredges.next(); 
				if (link.getSrc_if_id() == InterfaceID)
				{					
					float bw_link = (link.getTE_info().getMaximumReservableBandwidth().maximumReservableBandwidth);
					bw_link = bw_link + bw;
					MaximumReservableBandwidth maximumReservableBandwidth = null;
					maximumReservableBandwidth.setMaximumReservableBandwidth(bw_link);
					link.getTE_info().setMaximumReservableBandwidth(maximumReservableBandwidth);
					return true;
				}
			}
		}finally{
			this.getTEDBlock().unlock();
		}
		return false;
	}
}
