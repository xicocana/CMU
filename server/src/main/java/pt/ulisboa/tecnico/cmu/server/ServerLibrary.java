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
	private static final String SHARED_ALBUMS = "shared_albums";

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
	
	private boolean isOwner(String jsonFileString, String user) {
		JSONObject jsonFile = new JSONObject(jsonFileString);
		String owner = (String) jsonFile.get("owner");
		if(owner.equals(user)) {
			return true;
		} else {
			return false;
		}
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
		    JSONArray attributesList = (JSONArray) obj.get(key);
		    String email = (String) attributesList.get(1);
		    
		    if(key.equals("admin")) {} 
		    else {
		    	JSONArray intermediateArray = new JSONArray();
			    intermediateArray.put(0, key);
			    intermediateArray.put(1, email);
			    array.put(intermediateArray);
		    }		    
		}
		
		return array;
	}
	
	private boolean signUpJSON(String user, String email, String password, String jsonString) throws ServerLibraryException {
    	JSONObject jsonFile = new JSONObject(jsonString);
    	
    	try {
	    	if(jsonFile.has(user)==true) {
	    		return false;
	    	} else {
	    		JSONArray userAtributes = new JSONArray();
	    		userAtributes.put(0, password);
	    		userAtributes.put(1, email);
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
	
	private JSONArray checkForDriveIds(JSONArray user_albums) {
		JSONArray clearedList = new JSONArray();
		Iterator iter = user_albums.iterator();
		
		while(iter.hasNext()) {
			String nome = iter.next().toString();
			System.out.println(nome);
			JSONArray intermediateArray = new JSONArray(nome);
			String condition = (String) intermediateArray.get(1);
			System.out.println(condition.isEmpty());
			if(condition.isEmpty()) {
			}
			else {
				clearedList.put(intermediateArray);
			}
		}

		return clearedList;
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
    
	private boolean addAlbumToUserAlbums(String user, JSONObject jsonFile, JSONArray album_info, JSONArray user_albums, String album, String driveId, String txtId) throws ServerLibraryException {
		album_info.put(0, album);
		album_info.put(1, driveId);
		album_info.put(2, txtId);
				
		if(albumExists(user_albums, album, driveId)) { 
			return false;
		} else {
			String removeString = "[" + "\"" + album + "\"," + "\"\"," + "\"\"" + "]";
			JSONArray removeArray = new JSONArray(removeString);
			user_albums = removeElementFromArray(user_albums, removeArray);
			user_albums.put(album_info);
			jsonFile.put(user, user_albums);
						
			try {
				Utils.atomicWriteJSONToFile(jsonFile, USERS_ALBUMS);
				return true;
			} catch (UtilsException ue) {
				throw new ServerLibraryException("addAlbumToUserAlbums(): Something went wrong while doing an atomic write...", ue, true);
			}		
		}
	}
	
	private JSONArray removeElementFromArray(JSONArray jsonArray, Object removeElement) {
		Iterator iter = jsonArray.iterator();
		int i = 0;
		while(iter.hasNext()) {
			Object object = iter.next();
			if(object.equals(removeElement)) {
				jsonArray.remove(i);
				break;
			}
			i++;
		}
		
		return jsonArray;
	}
	
	private JSONArray removeElementFromArray(JSONArray jsonArray, JSONArray jsonArray2) {
		Iterator iter = jsonArray.iterator();
		int i = 0;
		while(iter.hasNext()) {
			JSONArray compareArray = (JSONArray) iter.next();
			if(compareArray.toString().equals(jsonArray2.toString())) {
				jsonArray.remove(i);
				break;
			}
			i++;
		}
		
		return jsonArray;
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
			if(album_info.get(0).equals(albumName) && !album_info.get(1).equals("")) {
				return true;
			} else if(album_info.get(0).equals(albumName) && album_info.get(1).equals("")) {
				return false;
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
		JSONArray album_info = null;
		JSONArray user_albums = null;
		
		if(!hasUser(user, jsonString)) {
			user_albums = new JSONArray(); 
			if(createAlbumBranch(album_info, jsonFile, user, user_albums, album, driveId, txtId)) {
				return true;
			} else {
				return false;
			}
		} else {
			user_albums = jsonFile.getJSONArray(user);
			if(hasAlbum(user, album, jsonString)) {
				return false;
			} else {
				if(createAlbumBranch(album_info, jsonFile, user, user_albums, album, driveId, txtId)) {
					return true;
				} else {
					return false;
				}
			}
		}
	}
	
	private boolean createAlbumBranch(JSONArray album_info, JSONObject jsonFile, String user, JSONArray user_albums, String album, String driveId, String txtId) throws ServerLibraryException {
		album_info = new JSONArray();
		
		if(addAlbumToUserAlbums(user, jsonFile, album_info, user_albums, album, driveId, txtId)) {
			createSharedFile(user, album);
			return true;
		} else {
			return false;
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
	
	private void shareAlbumWithUser(String user, String share, String album) throws ServerLibraryException {
		try {
			
			String jsonFileString = Utils.readFile(SHARED_ALBUMS + File.separator + album + ".json");
			JSONObject sharedAlbum = new JSONObject(jsonFileString);
			JSONArray sharedList = (JSONArray) sharedAlbum.get("shared");
			if(!checkUserInJSONArray(sharedList, share)) {
				sharedList.put(share);
				
				sharedAlbum.remove("shared");
				sharedAlbum.put("shared", sharedList);
				
				Utils.atomicWriteJSONToFile(sharedAlbum, SHARED_ALBUMS + File.separator + album + ".json");
				
				jsonFileString = Utils.readFile(USERS_ALBUMS);
				createAlbum(share, album, "", "", jsonFileString);
			}
		} catch (ServerLibraryException sle) {
			System.out.println("shareAlbumWithUser(): something went wrong with the Utils function...");
		} catch (UtilsException ue) {
			throw new ServerLibraryException("shareAlbumWithUser(): something went wrong while atomically writing a JSON to the file..."); 
		}
	}
	
	private boolean checkUserInJSONArray(JSONArray jsonArray, String user) {
		boolean exists = false;
		for(int i = 0; i<jsonArray.length(); i++) {
			String currentUser = (String) jsonArray.get(i);
			if(currentUser.equals(user)) {
				exists = true;
			}
		}		
		return exists;
	}
	
	private boolean albumExists(JSONArray albumsList, String album) {
		Iterator iter = albumsList.iterator();
		while(iter.hasNext()) {
			JSONArray iterAlbum = (JSONArray) iter.next();
			if(iterAlbum.get(0).equals(album)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean albumExists(JSONArray albumsList, String album, String driveId) {
		Iterator iter = albumsList.iterator();
		while(iter.hasNext()) {
			JSONArray iterAlbum = (JSONArray) iter.next();
			if(iterAlbum.get(0).equals(album) && iterAlbum.get(1).equals(driveId)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean userExists(String user) {
		try {
			String registeredUsers = Utils.readFile(REGISTER_CLIENTS_FILE);
			JSONObject jsonFile = new JSONObject(registeredUsers);
			
	    	if(jsonFile.has(user)==true) {
	    		return true;
	    	} else {
	    		return false;
	    	}	    		    	
		} catch (ServerLibraryException sle) {
			System.out.println("userExists(): something went wrong with the Utils function...");
		}
		
		return false;
	}
	
	public void wipeOutSessionKeys(String registeredClientsFile) throws ServerLibraryException {
		String registeredClients = Utils.readFile(registeredClientsFile);
		JSONObject jsonClients = new JSONObject(registeredClients);
		
		loopWipeOut(jsonClients);
		
	}
	
	
	private void createSharedFile(String user, String album) {
		JSONObject sharedFile = new JSONObject();
		JSONArray sharedList = new JSONArray();
		sharedList.put(user);
		
		sharedFile.put("owner", user);
		sharedFile.put("shared", sharedList);
		
		File file = new File(SHARED_ALBUMS + File.separator + album + ".json");
		if(file.exists() && !file.isDirectory()) { 
		} else {
			try {
				Utils.atomicWriteJSONToFile(sharedFile, SHARED_ALBUMS + File.separator + album + ".json");
			} catch (UtilsException e) {
				System.out.println("dreateSharedFile(): something went wrong with the Utils class...");
			}
		}
	}
	
	private JSONArray getSharedAlbumsList(JSONArray userList, String currentUser, String album, JSONArray userAlbums) throws ServerLibraryException {
		for(int i = 0; i<userList.length(); i++) {
			String userName = (String) userList.get(i);
			if(userName.equals(currentUser)) {
			} else {
				String jsonFileString = Utils.readFile(USERS_ALBUMS);
				
				JSONArray otherUserAlbums = getJSONUsersAlbums(userName, jsonFileString);
				for(int j=0; j<otherUserAlbums.length(); j++) {
					JSONArray otherAlbum = (JSONArray) otherUserAlbums.get(j);
					String otherAlbumName = (String) otherAlbum.get(0);
					if(otherAlbumName.equals(album)) {
						userAlbums.put(otherAlbum);
					}
				}
				//ir a cada um dos albums e acrescentar a um json array que depois e appended a saida da funcao de baixo
			}
		}
		//possivelmente problematico
		return userAlbums;
	}
	//adicionar currentUser para a comparacao
	private JSONArray getSharedAlbums(JSONArray user_albums, String currentUser) throws ServerLibraryException {
		//deve ser usada depois de parsar os drive ids a "". Caso contario da erro
		JSONArray iterUserAlbums = new JSONArray(user_albums.toString());
		try {
			for(int i = 0; i<iterUserAlbums.length(); i++) {
				JSONArray album = (JSONArray) user_albums.get(i);
				String albumName = album.getString(0);
				String jsonFileString = Utils.readFile(SHARED_ALBUMS + File.separator + albumName + ".json");
				
				JSONObject shared_albums = new JSONObject(jsonFileString);
				JSONArray shared_users = (JSONArray) shared_albums.get("shared");
				//criar uma funcao que vai a procura do album partilhado com base no ficheiro que arranjamos acima
				user_albums = getSharedAlbumsList(shared_users, currentUser, albumName, user_albums);		
			}
		} catch(ServerLibraryException sle) {
			throw new ServerLibraryException("getSharedAlbums(): something went wrong with the Utils class...", sle, true);
		}
		return user_albums;	
	}
	
	private boolean isAlbumSharedWithUser(String user, String sharedAlbum, String users_albums) {
		JSONObject jsonObject = new JSONObject(users_albums);
		JSONArray jsonArray = (JSONArray) jsonObject.get(user);
		Iterator iter = jsonArray.iterator();
		while(iter.hasNext()) {
			JSONArray iterArray = (JSONArray) iter.next();
			String album = (String) iterArray.get(0);
			if(album.equals(sharedAlbum)) {
				return true;
			}
		}
		
		return false;
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
	        String email = (String) Utils.getObjectByJSONKey(receivedJSON, "email");
			String password = (String) Utils.getObjectByJSONKey(receivedJSON, "password");
	        
		    synchronized(this) {
		    	String jsonFileString = Utils.readFile(REGISTER_CLIENTS_FILE);			        			        
		    			        	
		    	if(signUpJSON(userName, email, password, jsonFileString)) {
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
				
		    	File file = new File(SHARED_ALBUMS + File.separator + albumName + ".json");
		    	if(file.exists() && !file.isDirectory() && !isAlbumSharedWithUser(userName, albumName, jsonFileString)) {
		    		message = albumName + " already exists in the system...";
		    		sendOkMessage(message);
		    	} else {
		    		if(createAlbum(userName, albumName, driveId, txtId, jsonFileString)) {
			    		message = "Client album was sucessfully created!";
			    		sendOkMessage(message);
			    	} else {
			    		message = "Could not create " + userName + "'s album...";
			    		sendNotOkMessage(message);
			    	}
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
					jsonObject.put("conclusion", NOT_OK_MESSAGE);
					jsonObject.put("message", message);
					jsonObject.put("album-list", userAlbums);
					String sendData = jsonObject.toString();
									
					Utils.sendMessage(communication, sendData);
				} else {					
					userAlbums = getSharedAlbums(userAlbums, userName);
					userAlbums = checkForDriveIds(userAlbums);
					jsonObject.put("conclusion", OK_MESSAGE);
					jsonObject.put("album-list", userAlbums);
					String sendData = jsonObject.toString();
					Utils.sendMessage(communication, sendData);
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
	
	public void addUserToAlbum() throws ServerLibraryException {
		try {
			String receivedData = Utils.receiveMessage(communication);
			
			JSONObject receivedJSON = Utils.getJSONFromString(receivedData);
			String userName = (String) Utils.getObjectByJSONKey(receivedJSON, "user-name");
			String shareName = (String) Utils.getObjectByJSONKey(receivedJSON, "share-name");
			String album = (String) Utils.getObjectByJSONKey(receivedJSON, "album");
			
			JSONObject jsonObject = new JSONObject();
			if(userName.equals(shareName)) {
				String message = "You cannot share an album with yourself..";
				jsonObject.put("conclusion", NOT_OK_MESSAGE);
				jsonObject.put("message", message);
				String sendData = jsonObject.toString();
								
				Utils.sendMessage(communication, sendData);
				return;
			}
			synchronized(this) {
				String sharedAlbumFile = Utils.readFile(SHARED_ALBUMS + File.separator + album + ".json");				
				if(isOwner(sharedAlbumFile, userName)) {					
				}
				else {
					String message = userName + " is not the owner of the album...";
					jsonObject.put("conclusion", NOT_OK_MESSAGE);
					jsonObject.put("message", message);
					String sendData = jsonObject.toString();
									
					Utils.sendMessage(communication, sendData);
					return;
				}
								
				if(userExists(shareName)) {
					String jsonFileString = Utils.readFile(USERS_ALBUMS);
					
					JSONArray userAlbums = getJSONUsersAlbums(userName, jsonFileString);							
					if(userAlbums==null || userAlbums.isEmpty()) {
						String message = "You do not have a single album on the system!";
						jsonObject.put("conclusion", NOT_OK_MESSAGE);
						jsonObject.put("message", message);
						String sendData = jsonObject.toString();
										
						Utils.sendMessage(communication, sendData);
					} else {
						if(albumExists(userAlbums, album)) { 
							String message = shareName + " sucessfully added to album!";
							shareAlbumWithUser(userName, shareName, album);
							
							sendOkMessage(message);
						} else {
							String message = album + " does not exist on the system...";
							jsonObject.put("conclusion", NOT_OK_MESSAGE);
							jsonObject.put("message", message);
							String sendData = jsonObject.toString();
											
							Utils.sendMessage(communication, sendData);
						}						
					}
				} else {
					String message = shareName + " does not exist in the system!";
					jsonObject.put("conclusion", NOT_OK_MESSAGE);
					jsonObject.put("message", message);
					String sendData = jsonObject.toString();
					
					Utils.sendMessage(communication, sendData);
				}				
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
