package tid.emulator.node.resources;

import java.net.Inet4Address;
import java.util.Hashtable;

import es.tid.pce.pcep.messages.PCEPRequest;
import es.tid.rsvp.messages.RSVPPathTearMessage;
import es.tid.rsvp.messages.te.RSVPTEPathMessage;
import es.tid.rsvp.messages.te.RSVPTEResvMessage;
import tid.emulator.node.transport.lsp.LSPKey;
import tid.emulator.node.transport.lsp.te.LSPTE;


public interface ResourceManager {
		
	public boolean checkResources(LSPTE lsp);
	
	public boolean reserveResources(LSPTE lsp, Inet4Address dstNodeLink);
	
	
	public boolean freeResources(LSPTE lsp);
	
	public PCEPRequest getPCEPRequest(LSPTE lspInfo);
	
	//public boolean checkLocalResources(LSPTE lspInfo);
	
	public RSVPTEPathMessage getRSVPTEPathMessageFromPCEPResponse(LSPTE lsp);
	public RSVPPathTearMessage getRSVPPathTearMessage(LSPTE lsp);
	public RSVPTEResvMessage getRSVPResvMessageFromDestination(RSVPTEPathMessage path, LSPTE lsp);
	public RSVPTEPathMessage forwardRSVPpath(LSPTE lsp,RSVPTEPathMessage path);
	
	public Hashtable<LSPKey, Inet4Address> getProxHopIPv4List();

	public void setProxHopIPv4List(Hashtable<LSPKey, Inet4Address> proxHopIPv4List);

	public Hashtable<LSPKey, Inet4Address> getPreviousHopIPv4List();

	public void setPreviousHopIPv4List(Hashtable<LSPKey, Inet4Address> previousHopIPv4List);
}
