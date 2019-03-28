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
import java.util.Base64;
import java.util.Random;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;
import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;

public class ServerLibrary {
	
	private static final String REGISTER_CLIENTS_FILE = "register_clients.json";
	private static final String OK_MESSAGE = "OK";
	private static final String NOT_OK_MESSAGE = "NOT-OK";
	//TODO 
	//TODO FAZER AS ESCRITAS E LEITURAS DO JSON ATOMICAS EU NAO FIZ ISSO AINDA 
	//TODO 

	Communications communication;
	
	public ServerLibrary(Communications communication) {
		this.communication = communication;
	}
	
	public void signUp() throws ServerLibraryException {
		try {
			try {
				
				String data = (String) communication.receiveInChunks();
		        JSONObject obj = new JSONObject(data);
		        String user = (String) obj.get("user-name");
		        String password = (String) obj.get("password");
		        
		        try {
		        	BufferedReader br = new BufferedReader(new FileReader("register_clients.json"));
		        	String jsonFileString = br.readLine();
		        	if(jsonFileString==null || jsonFileString.equals("")) {
		        		byte[] bytes = new byte[96];
		        		new Random().nextBytes(bytes);
		        		
		        		String adminPassword = Base64.getEncoder().withoutPadding().encodeToString(bytes);
		        		BufferedWriter bw = new BufferedWriter(new FileWriter("register_clients.json"));
		        		obj = new JSONObject();
		        		obj.put("admin", adminPassword);
		        		String firstTimeUser = obj.toString();
		        		jsonFileString = firstTimeUser;
						bw.write(firstTimeUser);
						bw.close();
		        	}
		        	
		        	Parser parser = new Parser(jsonFileString, user, password);
		        	
		        	obj = new JSONObject();
		        	if(parser.parseChanges()==true) {
		        		String message = "You were sucessfully registered";
		        		obj.put("conclusion", OK_MESSAGE);
		        		obj.put("message", message);
		        		data = obj.toString();
		        		communication.sendInChunks(data);
		        		System.out.println("Client was sucessfully registered!");
		        	} else {
		        		String message = "There was a user with that user name already on the system! Pick another one.";
		        		obj.put("conclusion", NOT_OK_MESSAGE);
		        		obj.put("message", message);
		        		data = obj.toString();
		        		communication.sendInChunks(data);
		        		System.out.println(message);
		        	}
		        } catch (FileNotFoundException fnfe) {
		        	new FileWriter("register_clients.json");
		        	String error = "Server faced a problem while processing your request. Try again later...";	        	
		        	obj = new JSONObject();
					obj.put("conclusion", NOT_OK_MESSAGE);
					obj.put("message", error);
					data = obj.toString();
					communication.sendInChunks(data);
		        	throw new ServerLibraryException("Could not find the register_clients.json file. Aborting...", true);
				} catch (JSONException jsone) {
					String error = "Server faced a problem while processing your request. Try again later...";	        	
		        	obj = new JSONObject();
					obj.put("conclusion", NOT_OK_MESSAGE);
					obj.put("message", error);
					data = obj.toString();
					communication.sendInChunks(data);
					throw new ServerLibraryException("Server crashed while doing JSON Operations. Aborting...", true);
				}
			} catch (IOException ioe) {
				String error = "Server faced a problem while processing your request. Try again later...";	        	
	        	JSONObject obj = new JSONObject();
				obj.put("conclusion", NOT_OK_MESSAGE);
				obj.put("message", error);
				String data = obj.toString();
				communication.sendInChunks(data);
				throw new ServerLibraryException("Could not either read or write in the register_clients.json file. Aborting...", true);
			} 
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		}	
	}
	
}
