package tid.emulator.node.management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import tid.emulator.node.NetworkNode;
import tid.emulator.node.tedb.SimpleLocalTEDB;
import tid.emulator.node.transport.LSPCreationException;
import tid.emulator.node.transport.defineLocalTEDB;
import tid.emulator.node.transport.lsp.LSPCreationErrorTypes;
import tid.emulator.node.transport.lsp.te.TechnologyParameters;
import tid.emulator.node.transport.ospf.OSPFController;
import tid.pce.client.ClientRequestManager;
import tid.pce.client.PCCPCEPSession;
import tid.pce.client.PCEPClient;
import tid.pce.computingEngine.RequestDispatcher;
import tid.pce.pcep.objects.ObjectParameters;
import tid.pce.pcepsession.PCEPSessionsInformation;
import tid.pce.server.wson.ReservationManager;
import tid.pce.tedb.DomainTEDB;
import tid.pce.tedb.IntraDomainEdge;
import tid.pce.tedb.SimpleTEDB;

/**
 * Session to manage the PCE
 * @author ogondio
 *
 */
public class NodeManagementSession extends Thread {
	
	/**
	 * The socket of the management session
	 */
	private Socket socket;
	
	private int technology;
	/**
	 * Logger
	 */
	private Logger log;

	/**
	 * The request Dispatcher
	 */
	private RequestDispatcher requestDispatcher;
	
	/**
	 * Output Stream of the managament session, to write the answers.
	 */
	private PrintStream out;
	
	/**
	 * The TEDB 
	 */
	private DomainTEDB tedb;
	
	/**
	 * The reservation manager. 
	 
	private ReservationManager reservationManager;*/
	
	/**
	 * STRONGEST: Collaborative PCEs
	 */
	private NetworkNode node;
	private final int TURNOFF_STATE = 100;
	private final int INITIAL_STATE = 0;
	private final int CONFIGURATION_STATE = 1;
	private final int LSPMANAGEMENT_STATE = 2;
	private final int DEFAULTCONFIGURATION_STATE = 4;
	private final int MANUALLYCONFIGURATION_STATE = 5;
	private final int CONSOLE_INTERFACE_TYPE = 1;
	private final int WEB_INTERFACE_TYPE = 2;
	private final int NODE_MANAGEMENT_STATE = 3;
	private final int NODE_SHOW_TOPOLOGY = 6;
	
	//OSPF
	private OSPFController ospfController;
	
	private int state;
	private String command;
	private BufferedReader br;
	
	public NodeManagementSession(Socket s, NetworkNode node){
		this.socket=s;
		log=Logger.getLogger("PCEServer");
		state = INITIAL_STATE;
		this.node=node;
	}
	
	public void run(){
		log.info("Starting Management session");
		boolean running=true;
		try {
			out=new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			log.warning("Management session cancelled: "+e.getMessage());
			return;
		}
		out.print("***********************************************");
		out.print("******** ROADM CONSOLE USER INTERFACE *********");
		out.print("***********************************************\n");
		
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (running) {
				switch (state) {
				
					case INITIAL_STATE:
						
						out.print("\nROADM Main Menu:\n");
						out.print("\n\t1) Configure ROADM (WSON)\n");
						out.print("\t2) Configure ROADM (Flexigrid)\n");
						out.print("\t3) Turn off the ROADM\n");
						out.print("\t4) LSPs Management NODE\n");
						out.print("\t5) Show Topology NODE\n");
						out.print("\nPlease, choose an option\n");
						out.print("ROADM:>");
						
						command = null;
						command = getCommand();
						
						if(command.equals("1")){
							state = CONFIGURATION_STATE;
							technology = TechnologyParameters.WSON;
						
						}else if(command.equals("2")){
							technology = TechnologyParameters.SSON;
							state = CONFIGURATION_STATE;
												
						}else if(command.equals("3")){
							
							state = TURNOFF_STATE;
												
						}else if(command.equals("4")){
							
							state = NODE_MANAGEMENT_STATE;
												
						}else if(command.equals("5")){
							state = NODE_SHOW_TOPOLOGY;							
						}else{
							out.print("ERROR: Your command was incorrect\n");
							state = INITIAL_STATE;
						}
						break;
						
					case NODE_MANAGEMENT_STATE:
						out.print("\nNode Management Main Menu:\n\n");
						out.print("Available commands:\r\n\n");
						out.print("1)show LSPs\r\n");
						out.print("2)set LSP\r\n");
						out.print("3)teardown LSP\r\n");
						out.print("4)help\r\n");
						out.print("5)set traces on\r\n");
						out.print("6)set traces off\r\n");
						out.print("7)back\r\n\n");
						out.print("8)print eros\r\n\n");
						out.print("9)quit\r\n\n");
						out.print("NODE:>");
						command = getCommand();
						
						if (command.equals("quit") || command.equals("9")) {
							log.info("Ending Management Session");
							out.println("bye!");
							try {
								out.close();						
							} catch (Exception e){
								e.printStackTrace();
							}
							try {
								br.close();						
							} catch (Exception e){
								e.printStackTrace();
							}					
							return;
						}else if (command.equals("back")|| command.equals("7")) {
										
							state = INITIAL_STATE;
						}else if (command.equals("show LSPs")|| command.equals("1")){
							node.getManagerLSP().showLSPList(out);
						}else if (command.equals("set LSP")|| command.equals("2")){
							addLSP();
						}else if (command.equals("teardown LSP")|| command.equals("3")){
							killLSP();
						}
						else if (command.equals("help")|| command.equals("4")){
							out.print("\nNode Management Main Menu:");
							out.print("Available commands:\r\n");
							out.print("show LSPs\r\n");
							out.print("set LSP\r\n");
							out.print("teardown LSP\r\n");
							out.print("help\r\n");
							out.print("back to main menu\r\n");
							out.print("quit\r\n");						
						}
						else if (command.equals("set traces on")|| command.equals("5")) {
							log.setLevel(Level.ALL);		
							Logger log2=Logger.getLogger("PCEPParser");
							log2.setLevel(Level.ALL);
							Logger log3= Logger.getLogger("OSPFParser");
							log3.setLevel(Level.ALL);
							out.print("traces on!\r\n");
						} 
						else if (command.equals("set traces off")|| command.equals("6")) {
							log.setLevel(Level.SEVERE);		
							Logger log2=Logger.getLogger("PCEPParser");
							log2.setLevel(Level.SEVERE);
							Logger log3= Logger.getLogger("OSPFParser");
							log3.setLevel(Level.SEVERE);
							out.print("traces off!\r\n");
						}else if (command.equals("print eros")|| command.equals("8")){
							out.print("\nInsert the name of the file: ");
							String fileName = getCommand();
							FileWriter fichero = null;
								
							PrintWriter pw = null;
								
							try {
								fichero = new FileWriter (fileName);
								pw = new PrintWriter(fichero);
						        // A partir del objeto File creamos el fichero físicamente
						       
						        out.print("File Correctly Created!\n");
						        pw.println(node.getManagerLSP().printEroList());
						    } catch (IOException ioe) {
						    	ioe.printStackTrace();
						    }catch (Exception e) {
					            e.printStackTrace();
					        }finally {
					        	try {
					        		// Nuevamente aprovechamos el finally para 
					        		// asegurarnos que se cierra el fichero.
					        		if (null != fichero)
					        			fichero.close();
					        	} catch (Exception e2) {
					        		e2.printStackTrace();
					        	}
					        }
					    }else if (command.equals("back to main menu")) {
							state = INITIAL_STATE;
						}
						else if (command.equals("show interDomain links")){
							out.print(tedb.printInterDomainLinks());
						}else if (command.equals("stats")){
							out.println("procTime "+requestDispatcher.getThreads()[0].getProcTime().result());
							out.println("maxTime "+requestDispatcher.getThreads()[0].getMaxProcTime());
							out.println("idleTime "+requestDispatcher.getThreads()[0].getIdleTime().result());
						}
						else{
							out.print("invalid command\n");	
							out.print("\n");
							state = NODE_MANAGEMENT_STATE;
						}
						break;
						
					case CONFIGURATION_STATE:
						
						log.finer("Configuration");
						out.print("\nYou chose CONFIGURATION");
						out.print("\nThe following options are available:\n");
						out.print("\n\t1) Use the last ROADM configuration");
						out.print("\t2) Configure the ROADM manually");
						out.print("\t3) Read the configuration from a file");
						out.print("\t4) Back to Main Menu");
						out.print("\n\nPlease, choose an option");
						
						command = getCommand();
						
						if(command.equals("1")){
							
							// FIXME: Implementar m�todo que use la ultima configuracion
							state = DEFAULTCONFIGURATION_STATE;
							defaultROADMConfiguration();
							
						}else if(command.equals("2")){
							
							// FIXME: Implementar m�todo que configure manualmente
							state = MANUALLYCONFIGURATION_STATE;
							manuallyConfigureROADM();
													
						}else if(command.equals("3")){
							
							// FIXME: Implementar m�todo que lea de un archivo la configuracion
													
						}else if(command.equals("4")){
							state = INITIAL_STATE;
							
						}else{
							out.print("ERROR: Your command was incorrect\n");
						}
						
						break;
														
					case TURNOFF_STATE:
						
						log.info("Turn Off");
						
						node.getManagerLSP().killAllLSP();
						
						if(node.getPCC().getPceSession() != null){
							node.getPCC().getPceSession().close(ObjectParameters.REASON_NOEXPLANATION);
						}
						out.print("\nYou chose Turn Off");
						out.print("\nShutting Down...");

						// FIXME: Cerrar todos los threads
						running = false;
						System.exit(1);
						break;
					
					case NODE_SHOW_TOPOLOGY:
						log.finer("show topology");
						out.print(node.getTed().printTopology());
						//No enseñamos la topología multidominio
						state = INITIAL_STATE;
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	public String getCommand(){
		try {
			command = null;
			command = br.readLine();
		} catch (IOException ioe) {
			out.print("IO error trying to read your command");
			System.exit(1);
		}
		return command;
	}
	public void manuallyConfigureROADM(){
		
		while(state == MANUALLYCONFIGURATION_STATE){
			log.finer("Manually ROADM Configuration");
			out.print("\nYou chose MANUALLY CONFIGURATION");
			out.print("\nThe following options are available:");
			out.print("\n\t1) Set Node Id");
			out.print("\t2) Add Interface");
			out.print("\t3) Add Client");
			out.print("\t4) Add PCE");
			out.print("\t5) Back to Main Menu");
			out.print("\n\nPlease, choose an option");
			
			command = getCommand();
			if(command.equals("1")){  // Set Node Id
				
				try{
					node.getNodeInformation().setId((Inet4Address)InetAddress.getByName(getCommand()));
				}catch(UnknownHostException e){
					log.severe("Unknown Host exception when defining LSP destination address");
				}
			}else if(command.equals("2")){
				// FIXME: Implementar metodo para anadir interfaz
										
			}else if(command.equals("3")){
				// FIXME: Implementar metodo para anadir cliente
										
			}else if(command.equals("4")){
				
				// FIXME: Implementar anadir sesion PCE
				addPCE(true,null,null);
										
			}else if(command.equals("5")){
				
				state = INITIAL_STATE;
				break;
				
			}else{
				
				out.print("ERROR: Your command was incorrect\n");
			}
		}
	}
	public void defaultROADMConfiguration(){
		while(state == DEFAULTCONFIGURATION_STATE){
			
			log.finer("Default ROADM Configuration");
			Properties props = new Properties();
			// FIXME: Mirar esto del código OF si es necesario
			int OF = 10;
			try{
				props.load(new FileInputStream("/usr/local/nodeConfig/defaultConfiguration.properties"));
	            String nodeId = props.getProperty("nodeId");
	            String pceAddress = props.getProperty("PCEAddress");
	            String pcepPort = props.getProperty("PCEPPort");
	            String topologyName = null;
	            if (technology == TechnologyParameters.WSON){
	            	topologyName = props.getProperty("networkDescriptionFile_WSON");
	            	OF = 1001;
	            	
	            }
	            else if (technology == TechnologyParameters.SSON){
	            	topologyName = props.getProperty("networkDescriptionFile_SSON");
	            	OF = 1002;
	            	
	            }
                try{
					node.getNodeInformation().setId((Inet4Address)InetAddress.getByName(nodeId));
										
			        // Inicializacion del cliente PCE
					node.getPCC().setClientPCE(new PCEPClient());
			        // Inicialización
					node.getPCC().setCrm(new ClientRequestManager());
					//Añadimos el PCE y creamos la sesión PCEP
					addPCE(false,pceAddress,pcepPort);

					log.finer("Default ROADM Configuration Accomplished");
					
					/** Leer la topología y crear la TEDB Local del nodo
					 * 
					 * 1) Leer el mismo archivo .xml para todos los nodos.
					 * 2) Podar los enlaces y nodos que no sean adyacentes al nodo en el que estamos.
					 */
					
					//The Traffic Engineering Database
					DomainTEDB ted;
					ted=new SimpleLocalTEDB();
					((SimpleLocalTEDB)ted).initializeFromFile(topologyName, null, false, 0, Integer.MAX_VALUE);
					//TEDB CREADA --> recorrer grafo y podar
					SimpleDirectedWeightedGraph<Object, IntraDomainEdge> LocalGraph = defineLocalTEDB.podateGraph(((SimpleTEDB)ted).getNetworkGraph(), node.getNodeInformation().getId());
					((SimpleLocalTEDB)ted).setNetworkGraph(LocalGraph);
					node.setTed(((SimpleLocalTEDB)ted));
						
					//OSPF
					ospfController = new OSPFController();
					ospfController.initialize();
				
					state = INITIAL_STATE;
				}catch(UnknownHostException e){
					log.severe("Unknown Host exception when defining LSP destination address");
				}
				
			}catch(IOException e){
				e.printStackTrace();
				// FIXME: Meter mensaje de error
			}
		}
	}
	public void addPCE(boolean manually, String pceAddress, String pcepPort){
		log.finer("Adding PCE");
		
		if(manually){
			out.print("\nYou chose ADD PCE");
			out.print("\nInsert the PCE IP Please");
			String ip = getCommand();
			out.print("\nInsert the port where the PCE is listening");
			
			int port = Integer.valueOf(getCommand()).intValue();
			PCCPCEPSession PCEsession = new PCCPCEPSession(ip, port,false, new PCEPSessionsInformation());
			node.getPCC().setPceSession(PCEsession);
			node.getPCC().setCrm(PCEsession.crm);
			PCEsession.start();
		}else{
			int pcepport = Integer.valueOf(pcepPort).intValue();
			PCCPCEPSession PCEsession = new PCCPCEPSession(pceAddress, pcepport,false, new PCEPSessionsInformation());
			node.getPCC().setPceSession(PCEsession);
			node.getPCC().setCrm(PCEsession.crm);
			PCEsession.start();
		}			
	}
	public void killLSP(){
		log.finer("Killing LSP");
		
		out.print("\nYou chose Kill LSP");
		out.print("\nInsert the LSP identifier Please");
		out.print("\nInsert the Source of the LSP you want to kill: ");
		
		try{
			Inet4Address source = (Inet4Address) InetAddress.getByName(getCommand());
			
			out.print("\nInsert the LSP id Please: ");
			String s_id = getCommand();
			int id = Integer.parseInt(s_id);
			
			node.getManagerLSP().deleteLSP(source, id);
			
		}catch(UnknownHostException e){
			log.severe("Unknown Host exception when defining LSP source address");
		}
	}
	public void addLSP(){
		
		log.finer("Adding LSP");
		//request a destination
		out.print("\nYou chose ADD LSP");
		out.print("\nInsert the Destination Node ID Please: ");
		boolean bidirectional = false;
		int OFcode = 0;				
		try{
			Inet4Address destinationId = (Inet4Address) InetAddress.getByName(getCommand());
			out.print("\nInsert the bandwidth Please: ");
			String s_bw = getCommand();
			out.print("\nInsert Bidirectionality Please (yes/no): ");
			String bidirect = getCommand();
			if (bidirect.equals("yes"))
				bidirectional=true;
			else if (bidirect.equals("no"))
				bidirectional=false;
			float bw = Float.parseFloat(s_bw);
			
			if (node.getNodeInformation().getNodeTechnology() == TechnologyParameters.SSON){
				OFcode = 1002;
			}else if (node.getNodeInformation().getNodeTechnology() == TechnologyParameters.WSON){
				OFcode = 1001;
			}else if (node.getNodeInformation().getNodeTechnology() == TechnologyParameters.MPLS){
				OFcode = 1000;
			}
			
			//request the PCE for a route from source to destination
			try {
				node.getManagerLSP().addnewLSP(destinationId, bw, bidirectional, OFcode);
			} catch (LSPCreationException e) {
				log.info("Error when adding new LSP");
				switch(e.getErrorType()){
				case(LSPCreationErrorTypes.ERROR_REQUEST):{
					log.info("ERROR in Req");	    				
					break;
				}
				case(LSPCreationErrorTypes.NO_PATH):{
					log.info("ERROR in Path");	    				
					break;
				}
				case(LSPCreationErrorTypes.NO_RESOURCES):{
					log.info("ERROR in Resources");	    				
					break;
				}
				case(LSPCreationErrorTypes.NO_RESPONSE):{
					log.info("ERROR in Response");	    				
					break;
				}
				default:{
					log.info("ERROR!!!!!!!!");
					System.exit(-1);
					break;
				}
				}
			}
		}catch(UnknownHostException e){
			log.severe("Unknown Host exception when defining LSP destination address");
		}
		out.print("\nLSP being established");
	}
}