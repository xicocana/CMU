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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;
import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;
import pt.ulisboa.tecnico.cmu.server.exceptions.UtilsException;

public class ServerLibrary {
	
	private static final String REGISTER_CLIENTS_FILE = "register_clients.json";
	private static final String OK_MESSAGE = "OK";
	private static final String NOT_OK_MESSAGE = "NOT-OK";
	private static final String USERS_ALBUMS = "users_albums.json";
	private static final String EMPTY = ""; 

	Communications communication;
	
	public ServerLibrary() {
	}
	
	public ServerLibrary(Communications communication) {
		this.communication = communication;
	}
	
	private void sendOkMessage(String message) throws ServerLibraryException {
		JSONObject conclusionJSON = new JSONObject();
		conclusionJSON.put("conclusion", OK_MESSAGE);
		conclusionJSON.put("message", message);
		String data = conclusionJSON.toString();
		try {
			Utils.sendMessage(communication, data);
		} catch (UtilsException ue) {
			throw new ServerLibraryException("SendOkMessage(): Failed to send message", ue, true);
		}
	}
	
	private void sendNotOkMessage(String message) throws ServerLibraryException {
		JSONObject conclusionJSON = new JSONObject();
		conclusionJSON.put("conclusion", NOT_OK_MESSAGE);
		conclusionJSON.put("message", message);
		String data = conclusionJSON.toString();
		try {
			Utils.sendMessage(communication, data);
		} catch (UtilsException ue) {
			throw new ServerLibraryException("SendOkMessage(): Failed to send message", ue, true);
		}
	}
	
    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }
    
	//Assumimos nao existir colisoes entre os numeros gerados
	private String generateLoginToken() {
		
		byte[] randomByteArray = new byte[8];
		new Random().nextBytes(randomByteArray);
		
		String hexByteArray = bytesToHex(randomByteArray);
		
		return hexByteArray;
	}
	
	private String getJSONFileString(String file) throws IOException, CommunicationsException, ServerLibraryException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String jsonFileString = br.readLine();
			return jsonFileString;
		} catch (FileNotFoundException e) {			
			try {
				Utils.writeFile(USERS_ALBUMS, EMPTY);
			} catch (UtilsException ue) {
				throw new ServerLibraryException("getJSONFileString(): something went wrong with the Utils function while writing to a file", ue, true);				
			}
        	String error = "Server faced a problem while processing your request. Try again later...";	        	
        	JSONObject obj = new JSONObject();
			obj.put("conclusion", NOT_OK_MESSAGE);
			obj.put("message", error);
			String data = obj.toString();
			communication.sendInChunks(data); 
			return null;
		}
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
	
	private boolean signUpJSON(String user, String password, String jsonString) throws ServerLibraryException {
    	JSONObject jsonFile = new JSONObject(jsonString);
    	
    	try {
	    	if(jsonFile.has(user)==true) {
	    		return false;
	    	} else {
	    		JSONArray userAtributes = new JSONArray();
	    		userAtributes.put(0, password);
	    		userAtributes.put(1, "default_email");
	    		userAtributes.put(2, "default_token");
	    		
	    		jsonFile.put(user, userAtributes);
		    	
		    	Utils.atomicWriteJSONToFile(jsonFile, REGISTER_CLIENTS_FILE);
				
				return true; 	
	    	}
    	} catch(JSONException jsone) {
    		throw new ServerLibraryException("signUpJSON(): Something went wrong while doing JSON operations...", jsone, false);
    	} catch (UtilsException ue) {
    		throw new ServerLibraryException("signUpJSON(): Something went wrong while doing an atomic write...", ue, true);
		}
	}
	
	private JSONArray getJSONUsersAlbums(String user, String content) {
		JSONObject obj = new JSONObject(content);
		JSONArray user_albums = new JSONArray();
		if(!obj.has(user)) {
			System.err.println("User: \"" + user + "\" does not have a single album on the system");
			return user_albums;
		}
		
		user_albums = obj.getJSONArray(user);		

		return user_albums;
	}
    
	private void addUserToAlbum(String user, JSONObject jsonFile, JSONArray album_info, JSONArray user_albums, String album, String driveId, String txtId) throws ServerLibraryException {
		album_info.put(0, album);
		album_info.put(1, driveId);
		album_info.put(2, txtId);
		
		user_albums.put(album_info);
		
		jsonFile.put(user, user_albums);
					
		try {
			Utils.atomicWriteJSONToFile(jsonFile, USERS_ALBUMS);
		} catch (UtilsException ue) {
			throw new ServerLibraryException("addUserToAlbum(): Something went wrong while doing an atomic write...", ue, true);
		}
	}
	
	private boolean hasUser(String user, String jsonString) {
		JSONObject jsonFile = new JSONObject(jsonString);
		
		if(jsonFile.has(user)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkForExistingAlbum(String albumName, JSONArray user_albums) {
		Iterator iter = user_albums.iterator();
		
		while(iter.hasNext()) {
			JSONArray album_info = (JSONArray) iter.next();
			if(album_info.get(0).equals(albumName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean hasAlbum(String user, String album, String jsonString) {
		JSONObject jsonFile = new JSONObject(jsonString);
		JSONArray user_albums = (JSONArray) jsonFile.get(user);
		
		
		if(checkForExistingAlbum(album, user_albums)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean createAlbum(String user, String album, String driveId, String txtId, String jsonString) throws ServerLibraryException {	
		JSONObject jsonFile = new JSONObject(jsonString);
		JSONArray album_info;
		JSONArray user_albums;
		
		if(!hasUser(user, jsonString)) {
			album_info = new JSONArray();
			user_albums = new JSONArray();			
			addUserToAlbum(user, jsonFile, album_info, user_albums, album, driveId, txtId);
			return true;
		} else {
			if(hasAlbum(user, album, jsonString)) {
				return false;
			} else {
				album_info = new JSONArray();
				user_albums = jsonFile.getJSONArray(user);
				addUserToAlbum(user, jsonFile, album_info, user_albums, album, driveId, txtId);
				return true;
			}
		}
	}
	
	private String getToken(String userName, String registeredClients) throws ServerLibraryException {
		JSONObject jsonClients;
		try {
			jsonClients = Utils.getJSONFromString(registeredClients);
			JSONArray user_attributes = (JSONArray) Utils.getObjectByJSONKey(jsonClients, userName);
			String token = (String) Utils.getObjectByJSONArrayAttribute(user_attributes, "token");
			
			return token;
		} catch (UtilsException ue) {
			throw new ServerLibraryException("getToken(): something went wrong with the Utils class...", ue, true);
		}

	}
	
	private void loopWipeOut(JSONObject jsonClientsList) throws ServerLibraryException {
		Iterator<String> iter = jsonClientsList.keys();		
		try {
			ArrayList<String> intermediateList = new ArrayList<String>();
			while(iter.hasNext()) {
				String key = iter.next();
				intermediateList.add(key);
			}
			
			Iterator<String> listIter = intermediateList.iterator();
			synchronized(this) {
				while(listIter.hasNext()) {
					String key = listIter.next();
					JSONArray userAttributes = (JSONArray) Utils.getObjectByJSONKey(jsonClientsList, key);
					userAttributes = Utils.changeJSONArrayAttributeByIndex(userAttributes, "token", "default_token");
					jsonClientsList = Utils.changeJSONObjectKeyAttribute(jsonClientsList, key, userAttributes);
				}
			}
		Utils.atomicWriteJSONToFile(jsonClientsList, REGISTER_CLIENTS_FILE);
		} catch(UtilsException ue) {
			throw new ServerLibraryException("loopWipeOut(): something went wrong with the Utils class...", ue, true);
		}
		
	}
	
	public void wipeOutSessionKeys(String registeredClientsFile) throws ServerLibraryException {
		String registeredClients = Utils.readFile(registeredClientsFile);
		JSONObject jsonClients = new JSONObject(registeredClients);
		
		loopWipeOut(jsonClients);
		
	}
	
	public void login() throws ServerLibraryException {						
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
	        String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			String password = (String) Utils.getObjectByJSONKey(receivedJSON, "password");
	        
			synchronized(this) {
	        	String registeredClients = Utils.readFile(REGISTER_CLIENTS_FILE);
	        			        	
	        	JSONObject clientsJSON = new JSONObject(registeredClients);
	        	
	        	if(clientsJSON.has(userName)) {
	        		JSONArray userAttributes = (JSONArray) Utils.getObjectByJSONKey(clientsJSON, userName);
	        		String registeredPassword = (String) Utils.getObjectByJSONArrayAttribute(userAttributes, "password");
	        		//String registeredPassword = (String) Utils.getObjectByJSONKey(clientsJSON, userName);
		        	if(registeredPassword.equals(password)) {
		        		String message = "You were sucessfully logged into the system!";			        		
		        		String loginToken = generateLoginToken();			        		
		        		
						userAttributes = Utils.changeJSONArrayAttributeByIndex(userAttributes, "token", loginToken);
						clientsJSON = Utils.changeJSONObjectKeyAttribute(clientsJSON, userName, userAttributes);
						Utils.atomicWriteJSONToFile(clientsJSON, REGISTER_CLIENTS_FILE);
		        		
		        		JSONObject tokenJSON = new JSONObject();
		                tokenJSON.put("conclusion", OK_MESSAGE);
		                tokenJSON.put("token", loginToken);
		                String sendData = tokenJSON.toString();
		        
		        		Utils.sendMessage(communication, sendData);
		        		System.out.println("Client: " + userName + "was sucessfully logged into the system!");			        		
		        	} else {
		        		String message = "Incorrect password. Try again!";
		        		sendNotOkMessage(message);
		        		System.out.println(message);
		        	}
	        	} else {
	        		String message = "You are not registered on the system...";
	        		sendNotOkMessage(message);
	        		System.out.println("Client: " + userName + " is not registered on the system...");
	        	}
			}
		} catch (UtilsException ue) {
			throw new ServerLibraryException("login(): Something went wrong with the Utils function...", ue, true);
		}
        
	}
	
	public void signUp() throws ServerLibraryException {				
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
	        String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			String password = (String) Utils.getObjectByJSONKey(receivedJSON, "password");
	        
		    synchronized(this) {
		    	String jsonFileString = Utils.readFile(REGISTER_CLIENTS_FILE);			        			        
		    			        	
		    	if(signUpJSON(userName, password, jsonFileString)) {
		    		String message = "You were sucessfully registered";
		    		sendOkMessage(message);
		    		System.out.println("Client was sucessfully registered!");
		    	} else {
		    		String message = "There was a user with that user name already on the system! Pick another one.";
		    		sendOkMessage(message);
		    		System.err.println(message);
		    	}
		    }
		} catch (UtilsException ue) {
			throw new ServerLibraryException("signUp(): Something went wrong with the Utils function...", ue, true);
		}
		

	}
	
	public void addNewAlbum() throws ServerLibraryException {			
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
			
			String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			String albumName = (String) Utils.getObjectByJSONKey(receivedJSON, "album");
			String driveId = (String) Utils.getObjectByJSONKey(receivedJSON, "drive-id");
			String txtId = (String) Utils.getObjectByJSONKey(receivedJSON, "txt-id");

			synchronized(this) {
				String jsonFileString = Utils.readFile(USERS_ALBUMS);	
		    	String message = null;
				
		    	if(createAlbum(userName, albumName, driveId, txtId, jsonFileString)) {
		    		message = "Client album was sucessfully created!";
		    		sendOkMessage(message);
		    	} else {
		    		message = "Could not create " + userName + "'s album...";
		    		sendOkMessage(message);
		    	}
			}
		} catch (UtilsException ue) {
			throw new ServerLibraryException("addNewAlbum(): Something went wrong with the Utils function...", ue, true);
		}
		
		
	}
	
	public void getUsers() throws ServerLibraryException {
		try {
			JSONArray users_array = new JSONArray();
			
			synchronized(this) {
				String jsonFileString = Utils.readFile(REGISTER_CLIENTS_FILE);	
				
				JSONArray array = getJSONUsers(jsonFileString);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("user-list", array);
				jsonObject.put("conclusion", OK_MESSAGE);
				String sendData = jsonObject.toString();
				
				Utils.sendMessage(communication, sendData);
			}
		} catch (UtilsException ue) {
			throw new ServerLibraryException("getUsers(): Something went wrong with the Utils function...", ue, false);
		}			
	}
	
	public void getUserAlbums() throws ServerLibraryException {		
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
			String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			
			synchronized(this) {
				String jsonFileString = Utils.readFile(USERS_ALBUMS);
				
				JSONArray userAlbums = getJSONUsersAlbums(userName, jsonFileString);
				
				JSONObject jsonObject = new JSONObject();
				if(userAlbums==null || userAlbums.isEmpty()) {
					String message = "You do not have a single album on the system!";
					jsonObject.put("album-list", userAlbums);
					String sendData = jsonObject.toString();
									
					Utils.sendMessage(communication, sendData);
					sendNotOkMessage(message);
				} else {
					jsonObject.put("album-list", userAlbums);
					String sendData = jsonObject.toString();
					Utils.sendMessage(communication, sendData);
					sendOkMessage(EMPTY);
				}
			}
		} catch (UtilsException ue) {
			throw new ServerLibraryException("getUserAlbums(): Something went wrong with the Utils function...", ue, false);
		}
										
	}
	
	public void getToken() throws ServerLibraryException {
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
			String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			String password = (String) Utils.getObjectByJSONKey(receivedJSON, "password");
			
			synchronized(this) {
				
				String jsonFileString = Utils.readFile(REGISTER_CLIENTS_FILE);
				JSONObject clientsJSON = new JSONObject(jsonFileString);
				
				if(clientsJSON.has(userName)) {
	        		JSONArray userAttributes = (JSONArray) Utils.getObjectByJSONKey(clientsJSON, userName);
	        		String registeredPassword = (String) Utils.getObjectByJSONArrayAttribute(userAttributes, "password");
	        		//String registeredPassword = (String) Utils.getObjectByJSONKey(clientsJSON, userName);
		        	if(registeredPassword.equals(password)) {
		        		String token = getToken(userName, jsonFileString);
						
						JSONObject jsonObject = new JSONObject();
						if(token.equals("default_token")) {
							String message = "Token has already expired!";
							sendNotOkMessage(message);
						} else {
							jsonObject.put("conclusion", OK_MESSAGE);
							jsonObject.put("token", token);
							String sendData = jsonObject.toString();
							Utils.sendMessage(communication, sendData);
						}
		        	} else {
		        		String message = "Your password was changed on the meantime...!";
		        		sendNotOkMessage(message);
		        		System.out.println(message);
		        	}
	        	} else {
	        		String message = "You are not registered on the system...";
	        		sendNotOkMessage(message);
	        		System.out.println("Client: " + userName + " is not registered on the system...");
	        	}

			}
		} catch (UtilsException ue) {
			throw new ServerLibraryException("getToken(): Something went wrong with the Utils function...", ue, false);
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
