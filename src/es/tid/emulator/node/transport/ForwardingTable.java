package es.tid.emulator.node.transport;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Properties;

import es.tid.emulator.node.NetworkNode;

/**
 * Forwarding Table Class
 * Implements methods to get the next hop to reach a destination node
 * @author fmn
 *
 */

public class ForwardingTable {

	private NetworkNode roadm;
	
	public ForwardingTable(NetworkNode roadm){
		
		this.roadm = roadm;
	}
	public static Inet4Address getNextHop(Inet4Address destinationAddress){
		Properties props = new Properties();
		try{
			props.load(new FileInputStream("defaultConfiguration.properties"));
            String nextHop = props.getProperty("nextHop");
    		try{
    			Inet4Address addr = (Inet4Address) InetAddress.getByName(nextHop);
    			return addr;
    		}catch(Exception e){
    			return null;
    		}
        }catch(IOException e){
			// FIXME: Meter mensaje de error
			return null;
		}
	}
}
