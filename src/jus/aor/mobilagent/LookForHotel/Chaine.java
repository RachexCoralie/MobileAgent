package jus.aor.mobilagent.LookForHotel;

import jus.aor.mobilagent.LookForHotel.Hotel;
import jus.aor.mobilagent.LookForHotel._Chaine;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jus.aor.mobilagent.kernel._Service;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Chaine implements _Service {
	
	private static final long serialVersionUID = 1L;
	private List<Hotel> listhotels = new LinkedList<Hotel>();
	
	
	/**
	 * Construit la liste des hotels à partir du fichier XML
	 * @param chemin vers fichier XML 
	 * @return la liste des hotels
	 * @throws RemoteException 
	 */	
	protected Chaine(String path) {	
		
		System.out.println(">Initialisation de la chaine à partir du fichier "+ path + "...");
		
		/* Récupération des hôtels de la chaîne dans le fichier xml passé en 1er argument */
		DocumentBuilder docBuilder = null;
		Document doc=null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); // récupération d'informations de configuration
			doc = docBuilder.parse(new File(path)); // récupération d'informations de configuration
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		String name, localisation;
		NodeList list = doc.getElementsByTagName("Hotel"); //On récupère les arguments pour la construction de Chaine
		NamedNodeMap attrs;
		
		/* Acquisition de toutes les entrées de la base d'hôtels */
		for(int i =0; i<list.getLength();i++) {
			attrs = list.item(i).getAttributes();
			name=attrs.getNamedItem("name").getNodeValue();
			localisation=attrs.getNamedItem("localisation").getNodeValue();
			listhotels.add(new Hotel(name,localisation));
		}

	}

	
	/**
	 * Restitue la liste des hotels situés dans la localisation.
	 * @param localisation le lieu où l'on recherche des hotels
	 * @return la liste des hotels trouvés
	 * @throws RemoteException 
	 */
	public List<Hotel> get(String localisation){
		// On parcourt la liste d'hotel et en ne retourne que les hotels ayant la bonne localisation
		List<Hotel> result = new ArrayList<Hotel>();
        for (Hotel h : listhotels) {
            if (h.localisation.equals(localisation)) {
                result.add(h);
            }
        }
        return result;
	}


	/**
	 * Appel générique du service : recherche les hotels à la localisation donnée
	 * @param localisation 
	 * @return la liste d'hotels correspondant à la localisation
	 * @throws IllegalArgumentException 
	 */
	public List<Hotel> call(Object... params) throws IllegalArgumentException {
		String localisation = (String) params[0];
		Iterator<Hotel> iterator = listhotels.iterator();		
		List<Hotel> hotels = new ArrayList<Hotel>();

		while(iterator.hasNext()) {
		    Hotel element = iterator.next(); 
		    if((element.localisation).equals(localisation)){
				hotels.add(element);
			}
		} 
		System.out.println("Il y a " + hotels.size() + " Hotels dans "+ localisation +".");
		return hotels;
	}		

}
