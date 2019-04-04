package pt.ulisboa.tecnico.cmu.server;

import org.json.JSONArray;
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
import java.util.Iterator;
import java.util.Random;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;
import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;

public class ServerLibrary {
	
	private static final String REGISTER_CLIENTS_FILE = "register_clients.json";
	private static final String OK_MESSAGE = "OK";
	private static final String NOT_OK_MESSAGE = "NOT-OK";
	private static final String USERS_ALBUMS = "users_albums.json";
	private static final String EMPTY = "";
	
	private String exceptionFile = null;
	//TODO 
	//TODO FAZER AS ESCRITAS E LEITURAS DO JSON ATOMICAS EU NAO FIZ ISSO AINDA 
	//TODO 

	Communications communication;
	
	public ServerLibrary(Communications communication) {
		this.communication = communication;
	}
	
	private void sendOkMessage(String message) throws CommunicationsException {
		JSONObject conclusionJSON = new JSONObject();
		conclusionJSON.put("conclusion", OK_MESSAGE);
		conclusionJSON.put("message", message);
		String data = conclusionJSON.toString();
		communication.sendInChunks(data);
		System.out.println(message);
	}
	
	private void sendNotOkMessage(String message) throws CommunicationsException {
		JSONObject conclusionJSON = new JSONObject();
		conclusionJSON.put("conclusion", NOT_OK_MESSAGE);
		conclusionJSON.put("message", message);
		String data = conclusionJSON.toString();
		communication.sendInChunks(data);
		System.out.println(message);
	}
	
	private String getJSONFileString(String file) throws IOException, CommunicationsException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String jsonFileString = br.readLine();
			return jsonFileString;
		} catch (FileNotFoundException e) {			
        	new FileWriter("users_albums.json");
        	String error = "Server faced a problem while processing your request. Try again later...";	        	
        	JSONObject obj = new JSONObject();
			obj.put("conclusion", NOT_OK_MESSAGE);
			obj.put("message", error);
			String data = obj.toString();
			communication.sendInChunks(data); 
			return null;
		}
	}
	
	private void initializeClientList(String file) throws IOException {
		byte[] bytes = new byte[96];
		new Random().nextBytes(bytes);
		
		String adminPassword = Base64.getEncoder().withoutPadding().encodeToString(bytes);
		exceptionFile = "write";
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		JSONObject obj = new JSONObject();
		obj.put("admin", adminPassword);
		String firstTimeUser = obj.toString();
		String jsonFileString = firstTimeUser;
		bw.write(firstTimeUser);
		bw.close();
	}
	
	private void initializeAlbum(String file) throws IOException {
		exceptionFile = "write";
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));	        		
		
		JSONObject drive_album = new JSONObject();
		drive_album.put("default_album", "drive_id");		        		
		
		JSONObject user_albums = new JSONObject();		
		user_albums.put("admin", drive_album);
		
		String firstTimeUser = user_albums.toString();
		bw.write(firstTimeUser);
		bw.close();
	}
	
	private JSONArray putJSONIntoArray(JSONObject obj, JSONArray array) {
		Iterator<String> keys = obj.keys();
		
		while(keys.hasNext()) {
		    String key = keys.next();
		    array.put(key);    
		}
		
		return array;
	}
	
	private JSONArray getJSONUsers(String content) {
		JSONObject obj = new JSONObject(content.trim());
		Iterator<String> keys = obj.keys();
		JSONArray array = new JSONArray();
		
		while(keys.hasNext()) {
		    String key = keys.next();
		    if(key.equals("admin")) {} 
		    else {array.put(key);}		    
		}
		
		return array;
	}
	
	private JSONArray getJSONUsersAlbums(String user, String content) throws CommunicationsException {
		JSONObject obj = new JSONObject(content);
		JSONArray array = new JSONArray();
		if(!obj.has(user)) {
			System.err.println("User: \"" + user + "\" does not have a single album on the system");
			return array;
		}
		
		JSONObject albums = obj.getJSONObject(user);
		array = putJSONIntoArray(albums, array);
		
		return array;
	}
	
	public void login() throws ServerLibraryException {
		try {
			try {
				
				String data = (String) communication.receiveInChunks();
		        JSONObject obj = new JSONObject(data);
		        String user = (String) obj.get("user-name");
		        String password = (String) obj.get("password");
		        
		        try {
		        	exceptionFile = "read";
		        	BufferedReader br = new BufferedReader(new FileReader(REGISTER_CLIENTS_FILE));
		        	String jsonFileString = br.readLine();
		        	if(jsonFileString==null || jsonFileString.equals("")) {
		        		byte[] bytes = new byte[96];
		        		new Random().nextBytes(bytes);
		        		
		        		String adminPassword = Base64.getEncoder().withoutPadding().encodeToString(bytes);
		        		exceptionFile = "write";
		        		BufferedWriter bw = new BufferedWriter(new FileWriter(REGISTER_CLIENTS_FILE));
		        		obj = new JSONObject();
		        		obj.put("admin", adminPassword);
		        		String firstTimeUser = obj.toString();
		        		jsonFileString = firstTimeUser;
						bw.write(firstTimeUser);
						bw.close();
		        	}
		        			        	
		        	obj = new JSONObject(jsonFileString);
		        	if(obj.has(user)) {
		        		String registeredPassword = (String) obj.get(user);
			        	if(registeredPassword.equals(password)) {
			        		String message = "You were sucessfully logged into the system!";
			        		obj.put("conclusion", OK_MESSAGE);
			        		obj.put("message", message);
			        		data = obj.toString();
			        		communication.sendInChunks(data);
			        		System.out.println("Client: " + user + "was sucessfully logged into the system!");
			        	} else {
			        		String message = "Incorrect password. Try again!";
			        		obj.put("conclusion", NOT_OK_MESSAGE);
			        		obj.put("message", message);
			        		data = obj.toString();
			        		communication.sendInChunks(data);
			        		System.out.println(message);
			        	}
		        	} else {
		        		String message = "You are not registered on the system...";
		        		obj.put("conclusion", NOT_OK_MESSAGE);
		        		obj.put("message", message);
		        		data = obj.toString();
		        		communication.sendInChunks(data);
		        		System.out.println("Client: " + user + " is not registered on the system...");
		        	}
		        } catch (FileNotFoundException fnfe) {
		        	new FileWriter(REGISTER_CLIENTS_FILE);
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
				throw new ServerLibraryException("Could not " + exceptionFile + " in the register_clients.json file. Aborting...", true);
			} 
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		}	
	}
	
	public void signUp() throws ServerLibraryException {
		try {
			try {
				
				String data = (String) communication.receiveInChunks();
		        JSONObject obj = new JSONObject(data);
		        String user = (String) obj.get("user-name");
		        String password = (String) obj.get("password");
		        
		        try {
		        	exceptionFile = "read";
		        	BufferedReader br = new BufferedReader(new FileReader(REGISTER_CLIENTS_FILE));
		        	String jsonFileString = br.readLine();
		        	if(jsonFileString==null || jsonFileString.equals("")) {
		        		byte[] bytes = new byte[96];
		        		new Random().nextBytes(bytes);
		        		
		        		String adminPassword = Base64.getEncoder().withoutPadding().encodeToString(bytes);
		        		exceptionFile = "write";
		        		BufferedWriter bw = new BufferedWriter(new FileWriter(REGISTER_CLIENTS_FILE));
		        		obj = new JSONObject();
		        		obj.put("admin", adminPassword);
		        		String firstTimeUser = obj.toString();
		        		jsonFileString = firstTimeUser;
						bw.write(firstTimeUser);
						bw.close();
		        	}
		        	
		        	exceptionFile = "write";
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
		        	exceptionFile = "write";
		        	new FileWriter(REGISTER_CLIENTS_FILE);
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
				throw new ServerLibraryException("Could not " + exceptionFile + " in the register_clients.json file. Aborting...", true);
			} 
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		}	
	}
	
	public void addNewAlbum() throws ServerLibraryException {
		try {
			String data = (String) communication.receiveInChunks();
			JSONObject obj = new JSONObject(data);
			String user = (String) obj.get("user-name");
			String album = (String) obj.get("album");
			String driveId = (String) obj.get("drive-id");
			try {
				try {
					exceptionFile = "read";
					BufferedReader br = new BufferedReader(new FileReader(USERS_ALBUMS));
					String jsonFileString = br.readLine();
		        	if(jsonFileString==null || jsonFileString.equals("")) {
		        		initializeAlbum(USERS_ALBUMS);
		        	}				
					
		        	exceptionFile = "write";
		        	Parser parser = new Parser(jsonFileString, user);		        			        	
		        	String message;
		        	
		        	if(parser.parseAlbum(album, driveId)) {
		        		message = "Client album was sucessfully created!";
		        		sendOkMessage(message);
		        	} else {
		        		message = "Could not create " + user + "'s album...";
		        		sendOkMessage(message);
		        	}
		        	
				} catch (FileNotFoundException fnfe) {
					exceptionFile = "write";
		        	new FileWriter("users_albums.json");
		        	String error = "Server faced a problem while processing your request. Try again later...";	        	
		        	obj = new JSONObject();
					obj.put("conclusion", NOT_OK_MESSAGE);
					obj.put("message", error);
					data = obj.toString();
					communication.sendInChunks(data);
		        	throw new ServerLibraryException("Could not find the \"users_albums.json\" Aborting...", true);
				}
			}
			catch (IOException e) {
				throw new ServerLibraryException("Could not read the \"users_albums.json\" file...");
			}
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		} 
		
	}
	
	public void getUsers() throws ServerLibraryException {
		JSONArray users_array = new JSONArray();
		
		try {
			try {
				exceptionFile = "read";
				String jsonFileString = getJSONFileString(REGISTER_CLIENTS_FILE);
				if(jsonFileString==null || jsonFileString.isEmpty()) {
					initializeClientList(REGISTER_CLIENTS_FILE);
				}
				
				JSONArray array = getJSONUsers(jsonFileString);
				JSONObject obj = new JSONObject();
				obj.put("user-list", array);
				obj.put("conclusion", OK_MESSAGE);
				String data = obj.toString();
				communication.sendInChunks(data);
				sendOkMessage("");
				
			} catch (IOException ioe) {
				String message = "The server faced an internal problem!";
				sendNotOkMessage(message);
				throw new ServerLibraryException("Could not " + exceptionFile + REGISTER_CLIENTS_FILE + " file. Aborting...");
			} 
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		} 
	}
	
	public void getUserAlbms() throws ServerLibraryException {
		String data;
		try {
			try {
				data = (String) communication.receiveInChunks();
				JSONObject obj = new JSONObject(data);
				String user = obj.getString("user-name");
				
				exceptionFile = "read";
				String jsonFileString = getJSONFileString(USERS_ALBUMS);
				if(jsonFileString==null || jsonFileString.isEmpty()) {
					initializeClientList(USERS_ALBUMS);
				}
				
				JSONArray array = getJSONUsersAlbums(user, jsonFileString);
				obj = new JSONObject();
				if(array==null || array.isEmpty()) {
					String message = "You do not have a single album on the system!";
					obj.put("album-list", array);
					data = obj.toString();
					communication.sendInChunks(data);
					sendNotOkMessage(message);
				} else {
					obj.put("album-list", array);
					data = obj.toString();
					communication.sendInChunks(data);
					sendOkMessage(EMPTY);
				}								
			} catch(IOException ioe) {
				String message = "The server faced an internal problem!";
				sendNotOkMessage(message);
				throw new ServerLibraryException("Could not " + exceptionFile + USERS_ALBUMS + " file. Aborting...");
			}
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Communications module broke down...", ce, true);
		}				
	}
	
	public void exit() throws ServerLibraryException {
		try {
			communication.end();
		} catch (CommunicationsException ce) {
			throw new ServerLibraryException("Could properly close the connection", ce, true);
		}
	}
	
}
