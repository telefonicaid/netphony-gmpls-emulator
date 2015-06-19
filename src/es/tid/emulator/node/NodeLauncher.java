/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.tid.emulator.node;

/**
 * MAIN ROADM: Launches the ROADM
 */

public class NodeLauncher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	NetworkNode r = new NetworkNode();
    	r.startNode();
	}
}
