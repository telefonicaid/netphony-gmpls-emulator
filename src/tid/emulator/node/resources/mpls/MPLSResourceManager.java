package tid.emulator.node.resources.mpls;

import java.net.Inet4Address;
import java.util.Hashtable;

import tid.emulator.node.resources.ResourceManager;
import tid.emulator.node.tedb.SimpleLocalTEDB;
import tid.emulator.node.transport.lsp.LSPKey;
import tid.emulator.node.transport.lsp.te.LSPTE;
import tid.pce.pcep.messages.PCEPRequest;
import tid.pce.tedb.MDTEDB;
import tid.rsvp.messages.RSVPPathTearMessage;
import tid.rsvp.messages.te.RSVPTEPathMessage;
import tid.rsvp.messages.te.RSVPTEResvMessage;

/**
 * 
 * Class implementing an MPLS resource manager
 * 
 * @author Fernando Mu�oz del Nuevo
 *
 */

public class MPLSResourceManager implements ResourceManager{

	private SimpleLocalTEDB ted;
	private Inet4Address localID;
	
	public MPLSResourceManager(SimpleLocalTEDB tedb, Inet4Address localID){
		this.ted=tedb;
		this.localID=localID;
	}
	
	public MPLSResourceManager(SimpleLocalTEDB tedb, Inet4Address localID, MDTEDB MDted){
		this.ted=tedb;
		this.localID=localID;
	}
	
	@Override
	public boolean checkResources(LSPTE lsp) {
		
		return false;
	}

	@Override
	public PCEPRequest getPCEPRequest(LSPTE lspInfo) {
		
		return null;
	}

	@Override
	public RSVPTEPathMessage forwardRSVPpath(LSPTE lsp, RSVPTEPathMessage path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean freeResources(LSPTE lsp) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Hashtable<LSPKey, Inet4Address> getPreviousHopIPv4List() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable<LSPKey, Inet4Address> getProxHopIPv4List() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RSVPPathTearMessage getRSVPPathTearMessage(LSPTE lsp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RSVPTEResvMessage getRSVPResvMessageFromDestination(RSVPTEPathMessage path,
			LSPTE lsp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reserveResources(LSPTE lsp, Inet4Address dstNodeLink) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RSVPTEPathMessage getRSVPTEPathMessageFromPCEPResponse(LSPTE lsp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProxHopIPv4List(
			Hashtable<LSPKey, Inet4Address> proxHopIPv4List) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPreviousHopIPv4List(
			Hashtable<LSPKey, Inet4Address> previousHopIPv4List) {
		// TODO Auto-generated method stub
		
	}
}
