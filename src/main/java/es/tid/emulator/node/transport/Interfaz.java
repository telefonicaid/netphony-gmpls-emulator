/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.tid.emulator.node.transport;
/**
 * Interfaz Class will represent the information that the ROADM needs to know about its own interfaces
 * in order to be able of connecting itself with other nodes.
 * 
 * The main information to be stored about every Interfaz will be the following:
 * 
 * 	1.-	Interfaz ID:	FIXME: IP? No numerada? habr� que verlo
 * 	2.-	Grid:			The transmission bandwidth over a single channel FIXME: RFCs
 * 	3.- Channel Number:	Number of channels that can be used per interface	FIXME: RFCs
 * 	4.-	Channels:		The status of every channel (occupied, free, reserved... ) FIXME: Decidirlo
 * 		
 * @author fmn
 */
public class Interfaz {

    private int idInterfaz;
    private int grid;
    private int channelNumber;
    private int[] channels;

    /**
     *
     * Default constructor
     * Creates an Interfaz with id = 0, grid = 0, channelNumber = 0 and creates
     * an empty channel array.
     *
     */

    public Interfaz(){

        idInterfaz = 0;
        grid = 0;
        channelNumber = 0;

    }

    /**
     * Constructor with parameters to fill idInterfaz, grid and channel number
     * attributes. It also initalizes the channel array to channelNumber size
     * with all channels free (0 value)
     * @param idInterfaz
     * @param grid
     * @param channelNumber
     */

    public Interfaz(int idInterfaz, int grid, int channelNumber){

        this.idInterfaz = idInterfaz;
        this.grid = grid;
        this.channelNumber = channelNumber;
        this.channels = new int[channelNumber];

    }

    /**
     * @return idInterfaz parameter
     */

    public int getIdIntefaz(){

        return idInterfaz;

    }

    /**
     *
     * @return grid parameter
     */

    public int getGrid(){

        return grid;

    }

    /**
     * Method to extract channelNumber attribute
     * @return channelNumber
     */

    public int getChannelNumber(){

        return channelNumber;

    }

    /**
     * This method returns the channels status with the following legend:
     * 0 --> Free
     * 1 --> Used
     * 2 --> Down
     * 3 --> Admin Down
     * @return The array containing the channels status
     */

    public int[] getChannels(){

        return channels;

    }

    /**
     *
     * @param idInterfaz to fill idInterfaz attribute
     */

    public void setIdInterfaz(int idInterfaz){

        this.idInterfaz = idInterfaz;

    }

    /**
     *
     * @param grid to fill grid attribute
     */

    public void setGrid(int grid){

        this.grid = grid;

    }

    /**
     * Method to set the value of channelNumber attribute
     * @param channelNumber The number of channels to be set
     */

    public void setChannelNumber(int channelNumber){

        this.channelNumber = channelNumber;

    }

}
