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
			writeFile(USERS_ALBUMS, EMPTY);
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
	    		jsonFile.put(user, password);
		    	
		    	atomicWriteJSONToFile(jsonFile, REGISTER_CLIENTS_FILE);
				
				return true; 	
	    	}
    	} catch(JSONException jsone) {
    		throw new ServerLibraryException("signUpJSON(): Something went wrong while doing JSON operations...", jsone, false);
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
	
    static void writeFile(String filepath, String writeString) throws ServerLibraryException {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
            bw.write(writeString);
            bw.close();
        } catch (FileNotFoundException fnfe) {
            throw new ServerLibraryException("writeFile() exception: Could not find file '" + filepath + "'.", fnfe);
        } catch (IOException ioe) {
            throw new ServerLibraryException("writeFile() exception: Could not write to file '" + filepath + "'.", ioe);
        }
    }
    static String readFile(String filepath) throws ServerLibraryException {
        BufferedReader br;
        StringBuilder stringBuilder  = new StringBuilder();
        String line;
        String ls = System.lineSeparator();
        try {
            br = new BufferedReader(new FileReader(filepath));
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            // delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            br.close();
        } catch (FileNotFoundException fnfe) {
            throw new ServerLibraryException("readFile() exception: Could not find file '" + filepath + "'.", fnfe);
        } catch (IOException ioe) {
            throw new ServerLibraryException("readFile() exception: Could not read file '" + filepath + "'.", ioe);
        }
        String content = stringBuilder.toString();
        return content;
    }
    
    //a. atomicity
    private void atomicMoveFile(String originFilePath, String newFilePath) throws ServerLibraryException {
        Path originFilePathObject = FileSystems.getDefault().getPath(originFilePath);
        Path newFilePathObject = FileSystems.getDefault().getPath(newFilePath);
        try{
            Files.move(originFilePathObject, newFilePathObject, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ioe) {
            throw new ServerLibraryException("atomicMoveFile() exception: Couldn't move temporary database file into main database file.", ioe, true);
        }
    }
    private void atomicWriteJSONToFile(JSONObject updatedDatabaseJSON, String databaseFilePath) throws ServerLibraryException {
        // ATOMIC FUNCTION: CPU-ATOMIC AND FILE-WRITE-ATOMIC
        // CPU-ATOMICITY
        String updatedDatabaseString = updatedDatabaseJSON.toString();
        String databaseTempFilePath = databaseFilePath + ".tmp";

        // write updated database to our temporary file
        writeFile(databaseTempFilePath, updatedDatabaseString);

        // ATOMIC-WRITE to our main json file
        // based on move operation:
        // https://stackoverflow.com/questions/774098/atomicity-of-file-move
        // https://stackoverflow.com/questions/29923008/how-to-create-then-atomically-rename-file-in-java-on-windows?rq=1
        atomicMoveFile(databaseTempFilePath, databaseFilePath);        
    }
    
	private void addUserToAlbum(String user, JSONObject jsonFile, JSONArray album_info, JSONArray user_albums, String album, String driveId, String txtId) throws ServerLibraryException {
		album_info.put(0, album);
		album_info.put(1, driveId);
		album_info.put(2, txtId);
		
		user_albums.put(album_info);
		
		jsonFile.put(user, user_albums);
					
		atomicWriteJSONToFile(jsonFile, USERS_ALBUMS);
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
	
	public void login() throws ServerLibraryException {						
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
	        String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			String password = (String) Utils.getObjectByJSONKey(receivedJSON, "password");
	        
        	String registeredClients = readFile(REGISTER_CLIENTS_FILE);
        			        	
        	JSONObject clientsJSON = new JSONObject(registeredClients);
        	
        	if(clientsJSON.has(userName)) {
        		String registeredPassword = (String) Utils.getObjectByJSONKey(clientsJSON, userName);
	        	if(registeredPassword.equals(password)) {
	        		String message = "You were sucessfully logged into the system!";			        		
	        		String loginToken = generateLoginToken();			        		
	        		
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
	        
		        	
	    	String jsonFileString = readFile(REGISTER_CLIENTS_FILE);			        			        
	    			        	
	    	if(signUpJSON(userName, password, jsonFileString)) {
	    		String message = "You were sucessfully registered";
	    		sendOkMessage(message);
	    		System.out.println("Client was sucessfully registered!");
	    	} else {
	    		String message = "There was a user with that user name already on the system! Pick another one.";
	    		sendOkMessage(message);
	    		System.err.println(message);
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


			String jsonFileString = readFile(USERS_ALBUMS);	
	    	String message = null;
			
	    	if(createAlbum(userName, albumName, driveId, txtId, jsonFileString)) {
	    		message = "Client album was sucessfully created!";
	    		sendOkMessage(message);
	    	} else {
	    		message = "Could not create " + userName + "'s album...";
	    		sendOkMessage(message);
	    	}
		} catch (UtilsException ue) {
			throw new ServerLibraryException("addNewAlbum(): Something went wrong with the Utils function...", ue, true);
		}
		
		
	}
	
	public void getUsers() throws ServerLibraryException {
		try {
			JSONArray users_array = new JSONArray();

			String jsonFileString = readFile(REGISTER_CLIENTS_FILE);	
			
			JSONArray array = getJSONUsers(jsonFileString);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("user-list", array);
			jsonObject.put("conclusion", OK_MESSAGE);
			String sendData = jsonObject.toString();
			
			Utils.sendMessage(communication, sendData);
		} catch (UtilsException ue) {
			throw new ServerLibraryException("getUsers(): Something went wrong with the Utils function...", ue, false);
		}			
	}
	
	public void getUserAlbums() throws ServerLibraryException {		
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
			String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			
			String jsonFileString = readFile(USERS_ALBUMS);
			
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
		} catch (UtilsException ue) {
			throw new ServerLibraryException("getUserAlbums(): Something went wrong with the Utils function...", ue, false);
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
