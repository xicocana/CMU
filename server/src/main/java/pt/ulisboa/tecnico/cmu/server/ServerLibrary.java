package pt.ulisboa.tecnico.cmu.server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;
import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;

public class ServerLibrary {
	
	//TODO 
	//TODO FAZER AS ESCRITAS E LEITURAS DO JSON ATOMICAS EU NAO FIZ ISSO AINDA 
	//TODO 

	Communications communication;
	
	public ServerLibrary(Communications communication) {
		this.communication = communication;
	}
	
	public void signUp() throws ServerLibraryException {
		 //TODO mudar esta excepcao de merda aquando a biblioteca
		try {
			String data = (String) communication.receiveInChunks();
	        JSONObject obj = new JSONObject(data);
	        String user = (String) obj.get("user-name");
	        String password = (String) obj.get("password");
	        
	        try {
	        	//TODO testar caso o gajo nao tenha nada la escrito
	        	BufferedReader br = new BufferedReader(new FileReader("register_clients.json"));
	        	String jsonFileString = br.readLine();
	        	
	        	System.out.println(jsonFileString);
	        	Parser parser = new Parser(jsonFileString, user, password);
	        	parser.parseChanges();
			} catch (IOException ioe) {
				System.out.println("Could not write in the register_clients.json file");
			}
	        
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		}	
	}
	
}
