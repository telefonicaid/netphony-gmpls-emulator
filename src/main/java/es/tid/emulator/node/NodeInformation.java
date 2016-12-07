package es.tid.emulator.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Properties;

import es.tid.vntm.topology.elements.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.tid.emulator.node.transport.lsp.te.TechnologyParameters;

public class NodeInformation {
    private final String mainNode;
    private final String defaultNode;
    /**
	 * This class loads the file with all the information referenced to the node
	 */
	
	/**
	 * Node ID: IPv4
	 */
    private Inet4Address id;
    /**
     * Technology of the Node
     */
    private int nodeTechnology=0;
    /**
     * Topology Name
     */
    private String topologyName;
    /**
     * IPv4 of the PCE
     */
    private Inet4Address pceID;
    /**
     * Port of the PCE
     */
    private int pcePort;
    
    private boolean setTraces;
    
    private boolean rsvpMode;
    
    private Logger log;
    
    private boolean isStatefull = false;
    
    private boolean isSRCapable = false;
    private int MSD = 0;

    /**
     * Default class constructor.
     */
	public NodeInformation(){
		this("/usr/local/nodeConfig/defaultConfiguration.properties", "/usr/local/MynodeConfig/defaultConfiguration.properties");
        log=LoggerFactory.getLogger("ROADM");
        log.info("Using default .properties configuration files and directories.");
	}

    /**
     * Class constructor with two parameters.
     * @param mainNode Main node properties configuration file.
     * @param defaultNode Default node properties configuration file.
     */
	public NodeInformation(String mainNode, String defaultNode) {
        log=LoggerFactory.getLogger("ROADM");
        this.mainNode = mainNode;
        this.defaultNode = defaultNode;
        
	}
	public void readNodeConfiguration(){
		Properties props = new Properties();
		Properties props_node = new Properties();
			
		try{
			log.debug("Reading from "+this.mainNode);
			props.load(new FileInputStream(this.mainNode));
			log.debug("Reading local nodeId from "+this.defaultNode);
			props_node.load(new FileInputStream(this.defaultNode));
			String nodeId = props_node.getProperty("nodeId").trim();
	        String pceAddress = props.getProperty("PCEAddress").trim();
	        String pcepPort = props.getProperty("PCEPPort").trim();
	        String flexi_s = props.getProperty("flexi").trim();
	        String mpls_s = props.getProperty("mpls").trim();
	        String rsvpM = props.getProperty("RSVPMode").trim();
	        String SetTraces = props.getProperty("SetTraces").trim();
	        
	        topologyName = props.getProperty("networkDescriptionFile"); 
	        rsvpMode = Boolean.parseBoolean(rsvpM);
	        pceID = (Inet4Address) (Inet4Address.getByName(pceAddress));      
	        pcePort = Integer.parseInt(pcepPort);
	        id = (Inet4Address)InetAddress.getByName(nodeId);
	        setTraces = Boolean.parseBoolean(SetTraces);
	        
	        if (Boolean.parseBoolean(flexi_s)==true){
	        	nodeTechnology = TechnologyParameters.SSON;	   
	        } else {
	        	if (Boolean.parseBoolean(mpls_s)==true){
	        		nodeTechnology = TechnologyParameters.MPLS;
	            }else{
	            	nodeTechnology = TechnologyParameters.WSON;
	            }
	        }
	    }catch(IOException e){
			e.printStackTrace();
			// FIXME: Meter mensaje de error
		}
	}
    
    public int getNodeTechnology() {
		return nodeTechnology;
	}

	public void setNodeTechnology(int nodeTechnology) {
		this.nodeTechnology = nodeTechnology;
	}
	
    /**
     * Method to extract the node id.
     * @return The ROADM identifier as int.
     */

    public Inet4Address getId(){
        return id;
    }

    /**
     *
     * @param id
     */

    public void setId(Inet4Address id){
    	this.id = id;
    }

    public String getTopologyName() {
		return topologyName;
	}

	public void setTopologyName(String topologyName) {
		this.topologyName = topologyName;
	}

	public Inet4Address getPceID() {
		return pceID;
	}

	public void setPceID(Inet4Address pceID) {
		this.pceID = pceID;
	}

	public int getPcePort() {
		return pcePort;
	}

	public void setPcePort(int pcePort) {
		this.pcePort = pcePort;
	}

	public boolean isRsvpMode() {
		return rsvpMode;
	}

	public void setRsvpMode(boolean rsvpMode) {
		this.rsvpMode = rsvpMode;
	}

	public boolean isSetTraces() {
		return setTraces;
	}

	public void setSetTraces(boolean setTraces) {
		this.setTraces = setTraces;
	}
	
	public boolean isStatefull() {
		return isStatefull;
	}

	public void setStatefull(boolean isStatefull) {
		this.isStatefull = isStatefull;
	}

	@Override
	public String toString() {
		return "NodeInformation [id=" + id + ", nodeTechnology="
				+ nodeTechnology + ", topologyName=" + topologyName
				+ ", pceID=" + pceID + ", pcePort=" + pcePort + ", setTraces="
				+ setTraces + ", rsvpMode=" + rsvpMode + ", isStatefull="
				+ isStatefull + ", isSRCapable=" + isSRCapable + "]";
	}
	
	
}
