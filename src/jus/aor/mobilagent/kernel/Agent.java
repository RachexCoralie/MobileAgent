package jus.aor.mobilagent.kernel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

//** Client mobile qui se déplace de serveur en serveur en emportant avec lui son contexte **/

public class Agent implements _Agent{

	private static final long serialVersionUID = 1L;
	
	public transient AgentServer server;
	private transient String serverName;
	private Route route;
	private URI firstServer;
	private transient BAMAgentClassLoader bamAgent;
	// Pour l'OBJ4
	protected long debut;
	

	@Override
	// Initialise l'agent lors de son déploiement initial dans le bus à agents mobiles.
	// > Au départ n'a pas de BAMAgentClassLoader 
	public void init(BAMAgentClassLoader bamAgent, AgentServer agentServer, String serverName) {
		// Lors de l'initialisation d'un agent, on doit renseigner son Serveur de départ
		this.server = agentServer;
		this.serverName = serverName;
		this.bamAgent = bamAgent;
		
		try {
			this.firstServer = new URI(serverName);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		//Obj4
		debut = System.currentTimeMillis();
		// Construction d'une feuille de route pour l'agent
		try {
			route = new Route(new Etape(this.server.site(),_Action.NIHIL));
		} //catch (URISyntaxException e) 
		catch (Exception e){
			e.printStackTrace();
		}
	}

	
	@Override
	// ReInitialise l'agent lors de son déploiement sur un des serveurs du bus.
	// > doit transiter avec son BAMAgentClassLoader !
	public void reInit(BAMAgentClassLoader bamAgent, AgentServer agentServer, String serverName) {
		// On doit renseigner le serveur courant et le classloader
		this.server = agentServer;
		this.serverName = serverName;
		this.bamAgent = bamAgent;
		System.out.println("Reinit");
	}

	
	@Override
	public void addEtape(Etape etape) {
		// Ajout de l'étape à la feuille de route
		this.route.add(etape);
	}
	
	
	@Override
	/** Methode appelée lorsqu'un agent arrive sur un serveur **/
	public void run() {
		System.out.println("L'agent est sur le serveur " + serverName + "\n");
		if (this.route.hasNext()){
			Etape NextStep = this.route.next();
			NextStep.action.execute();
			
			if(this.route.hasNext()){
				this.move();
			}else {
				System.out.println("L'agent à fini.");
			}
		} else {
			System.out.println("L'agent avait déjà fini.");
		}
		
	}
	
	private void move(){
		try {
			System.out.println("Déplacement de l'agent sur le serveur :" + route.get().server.getPort() + "\n");
			// Creation du socket agent
			Socket ConnectServer;
			ConnectServer = new Socket(route.get().server.getHost(),route.get().server.getPort());
			// Recupération du Jar qui doit transiter avec l'agent
			Jar myjar = bamAgent.getJar() ;
			// Preparation de l'envoie
			OutputStream os = ConnectServer.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			// Envoi du jar au serveur par serialization
			oos.writeObject(myjar);
			// Envoie de l'agent au serveur par serialization
			oos.writeObject(this);
			// Fermeture
			os.close();
			oos.close();
			ConnectServer.close();
		} catch (NoSuchElementException | IOException e) {
			System.out.println("Le serveur n'est pas joignable, on passe au suivant \n");
			route.next();
			new Thread(this).run();
		}
	}
		
	
	/** Utilisé par Hello pour récupérer le nom du serveur sur lequel l'agent passe*/
	public String getServerName() {
		return this.serverName;
	}
	
	public URI getFirstServer() {
		return this.firstServer;
	}
	
	public Route getRoute(){
		return this.route;
	}
	
	
	public _Action retour = new _Action() {
		private static final long serialVersionUID = 1L;
		public void execute() {
			System.out.println("End!");
		}
	};
	
	
}
