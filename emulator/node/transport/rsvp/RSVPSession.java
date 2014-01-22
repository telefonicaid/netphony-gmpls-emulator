/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tid.emulator.node.transport.rsvp;

import java.util.Timer;


/**
 *
 * @author fmn
 */
public class RSVPSession extends Thread {

    private RSVPListener listener;
    private RSVPSender sender;
    private boolean no_delay=false;
    private String ID_dest;
    private int port;
    private int deadTimerLocal = 0; 
    private Timer timer;
    private int keepAliveLocal = 0;
    private boolean running = true;
    
    public RSVPSession(String ip_destination, int port, boolean no_delay){
    	this.no_delay=no_delay;
    	this.ID_dest=ip_destination;
    	this.port=port;
    	this.keepAliveLocal=30;
		this.deadTimerLocal=120;
		this.timer = new Timer();
    }

	public void run(){
    	running=true;
        /*System.out.println("Nueva Sesion RSVP");
        System.out.println("Opening new RSVP Session with host "+ ID_dest + " on port " + port);*/
        
        

    }

}
