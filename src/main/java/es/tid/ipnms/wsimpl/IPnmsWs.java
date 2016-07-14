package es.tid.ipnms.wsimpl;

import es.tid.ipnms.datamodel.router.RouterDesc;
import es.tid.ipnms.datamodel.router.GRETunnel.GRETunnelDesc;
import es.tid.ipnms.datamodel.router.IPinterface.IPInterfaceConfig;
import es.tid.ipnms.datamodel.router.IPinterface.IPInterfaceDesc;
import es.tid.ipnms.datamodel.router.LabelSwitchedPath.LabelSwitchedPath;
import es.tid.ipnms.datamodel.router.LabelSwitchedPath.LabelSwitchedPathWithUnnumIf;
import es.tid.ipnms.datamodel.router.routing.StaticRouteDesc;
import es.tid.ipnms.datamodel.router.routing.acl.ACLDesc;
import es.tid.ipnms.datamodel.router.routing.acl.ForwardingRuleDesc;
import es.tid.ipnms.datamodel.router.routing.routingprotocol.RProtocolDesc;

public interface IPnmsWs {
	
	/**Service endpoint to configure IP interface*/
	public int configureIPInterface(RouterDesc desc, IPInterfaceDesc ifDesc, IPInterfaceConfig config);
	
	
	/**Service endpoint to create a GRE tunnel Interface*/
	public int createGREInterface (RouterDesc desc, GRETunnelDesc tunnDesc);
	
	/**Service endpoint to configure the routing protocol*/
	public int configureRoutingProtocol(RouterDesc desc, RProtocolDesc rDesc);

	/**Service endpoint to configure a Static Route*/
	public int configureStaticRoute(RouterDesc desc, StaticRouteDesc rDesc);

	
	/**Service endpoint to configure an ACL based static forwarding rule*/
	public int configureACLStaticRoute(RouterDesc desc, ACLDesc aclDesc, ForwardingRuleDesc ruleDesc);


	/**Service endpoint to configure a Label Switched Path*/
	public int configureLabelSwitchedPath(RouterDesc desc, LabelSwitchedPath lsp);
	
	/**Service endpoint to configure a Label Switched Path*/
	public int configureLabelSwitchedPathWithUnnIf(RouterDesc desc, LabelSwitchedPathWithUnnumIf lsp);
	
}
