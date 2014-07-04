package tid.emulator.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Logger;

import tid.emulator.node.transport.lsp.te.TechnologyParameters;

public class NodeInformation {
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

	public void readNodeConfiguration(){
    	
    	log=Logger.getLogger("ROADM");
    	
		Properties props = new Properties();
		Properties props_node = new Properties();
			
		try{
			props.load(new FileInputStream("/usr/local/nodeConfig/defaultConfiguration.properties"));
			props_node.load(new FileInputStream("/usr/local/MynodeConfig/defaultConfiguration.properties"));
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
}
