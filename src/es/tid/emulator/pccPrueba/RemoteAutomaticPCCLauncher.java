/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.tid.emulator.pccPrueba;

/**
 * MAIN ROADM: Launches the ROADM
 */

public class RemoteAutomaticPCCLauncher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	RemoteAutomaticPCC r = new RemoteAutomaticPCC();
    	r.startRemoteAutomaticPCC();
	}
}
