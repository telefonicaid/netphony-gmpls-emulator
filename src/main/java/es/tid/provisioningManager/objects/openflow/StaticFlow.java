package es.tid.provisioningManager.objects.openflow;

import java.util.ArrayList;

public class StaticFlow {
/*http://www.openflowhub.org/display/floodlightcontroller/Static+Flow+Pusher+API+%28New%29*/
	
	public static String noVlan = "0";
	
	/**
	 *  ID of the switch (data path) that this rule should be added to
	 *  xx:xx:xx:xx:xx:xx:xx:xx 
	 *  *
	 **/
	private String switchID;
	/**
	 * Name of the flow entry, this is the primary key, it MUST be unique  
	 */
	private String name;
	
	/**
	 * Deprecated variable actions. Now the actions is stored in the ports and vlan_id variables
	 */
	private String actions;
	/**
	 *  default is 32767 
	 *	maximum value is 32767 
	 **/
	private int priority = 32767;
	
	private Boolean active;
	/**
	 * switch port on which the packet is received
	 * Can be hexadecimal (with leading 0x) or decimal 
	 * 
	 */
	private String ingressPort ;
	/**
	 * Can be hexadecimal (with leading 0x) or decimal 
	 */
	private int ethertype;
	/**
	 * xx.xx.xx.xx 
	 */
	private int srcIp ;
	/**
	 * xx.xx.xx.xx 
	 */
	private int dstIp ;
	
	/**
	 * xx.xx.xx.xx.xx.xx
	 */
	private String srcMAC ;
	/**
	 * xx.xx.xx.xx.xx.xx 
	 */
	private String dstMAC ;
	
	private String vlan_id = StaticFlow.noVlan;
	
	private String vlan_priority = null;
	
	private ArrayList<String> ports = new ArrayList<String>();
	
	
	/**
	 * Getters and Setters
	 **/
	public String getSwitchID() {
		return switchID;
	}
	public void setSwitchID(String switchID) {
		this.switchID = switchID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getActions() {
		return actions;
	}
	public void setActions(String actions) {
		this.actions = actions;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getIngressPort() {
		return ingressPort;
	}
	public void setIngressPort(String ingressPort) {
		this.ingressPort = ingressPort;
	}
	public int getEthertype() {
		return ethertype;
	}
	public void setEthertype(int ethertype) {
		this.ethertype = ethertype;
	}
	public int getSrcIp() {
		return srcIp;
	}
	public void setSrcIp(int srcIp) {
		this.srcIp = srcIp;
	}
	public int getDstIp() {
		return dstIp;
	}
	public void setDstIp(int dstIp) {
		this.dstIp = dstIp;
	}
	public String getSrcMAC() {
		return srcMAC;
	}
	public void setSrcMAC(String srcMAC) {
		this.srcMAC = srcMAC;
	}
	public String getDstMAC() {
		return dstMAC;
	}
	public void setDstMAC(String dstMAC) {
		this.dstMAC = dstMAC;
	}
	public String getVlan_id() {
		return vlan_id;
	}
	public void setVlan_id(String vlan_id) {
		this.vlan_id = vlan_id;
	}
	public String getVlan_priority() {
		return vlan_priority;
	}
	public void setVlan_priority(String vlan_priority) {
		this.vlan_priority = vlan_priority;
	}
	public ArrayList<String> getPorts() {
		return ports;
	}
	public void setPorts(ArrayList<String> ports) {
		this.ports = ports;
	}
}
