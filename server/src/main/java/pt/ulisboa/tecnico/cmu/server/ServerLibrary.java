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
	
	private void initializeClientList(String file) throws IOException, ServerLibraryException {
		byte[] bytes = new byte[96];
		new Random().nextBytes(bytes);
		
		String adminPassword = Base64.getEncoder().withoutPadding().encodeToString(bytes);
		exceptionFile = "write";

		JSONObject obj = new JSONObject();
		obj.put("admin", adminPassword);
		
		atomicWriteJSONToFile(obj, file);
	}
	
	private void initializeAlbum(String file) throws IOException, ServerLibraryException {
		exceptionFile = "write";        		
		
		JSONObject drive_album = new JSONObject();
		drive_album.put("default_album", "drive_id");		        		
		
		JSONObject user_albums = new JSONObject();		
		user_albums.put("admin", drive_album);
		
		atomicWriteJSONToFile(user_albums, file);
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
	
	private boolean signUpJSON(String user, String password, String jsonString) throws IOException, ServerLibraryException {
    	JSONObject jsonFile = new JSONObject(jsonString);
    	
    	if(jsonFile.has(user)==true) {
    		return false;
    	} else {
    		jsonFile.put(user, password);
	    	
	    	atomicWriteJSONToFile(jsonFile, REGISTER_CLIENTS_FILE);
			
			return true; 	
    	}
	}
	
	private JSONObject getJSONUsersAlbums(String user, String content) throws CommunicationsException {
		JSONObject obj = new JSONObject(content);
		JSONObject albums_mapping = new JSONObject();
		if(!obj.has(user)) {
			System.err.println("User: \"" + user + "\" does not have a single album on the system");
			return albums_mapping;
		}
		
		albums_mapping = obj.getJSONObject(user);		

		return albums_mapping;
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
        synchronized (this){
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
    }
    
	private void addUserToAlbum(String user, JSONObject jsonFile, JSONObject user_albums, String album, String driveId) throws IOException, ServerLibraryException {
		user_albums.put(album, driveId);
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
	
	private boolean hasAlbum(String user, String album, String jsonString) {
		JSONObject jsonFile = new JSONObject(jsonString);
		JSONObject user_albums = (JSONObject) jsonFile.get(user);
		
		if(user_albums.has(album)==true) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean createAlbum(String user, String album, String driveId, String jsonString) throws IOException, ServerLibraryException {		
		JSONObject jsonFile = new JSONObject(jsonString);
		JSONObject user_albums;
		
		if(!hasUser(user, jsonString)) {
			user_albums = new JSONObject();
			addUserToAlbum(user, jsonFile, user_albums, album, driveId);
			return true;
		} else {
			if(hasAlbum(user, album, jsonString)) {
				return false;
			} else {
				user_albums = jsonFile.getJSONObject(user);
				addUserToAlbum(user, jsonFile, user_albums, album, driveId);
				return true;
			}
		}
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
		        	String jsonFileString = readFile(REGISTER_CLIENTS_FILE);
		        	if(jsonFileString==null || jsonFileString.equals("")) {
		        		initializeClientList(REGISTER_CLIENTS_FILE);
		        	}
		        			        	
		        	obj = new JSONObject(jsonFileString);
		        	if(obj.has(user)) {
		        		String registeredPassword = (String) obj.get(user);
			        	if(registeredPassword.equals(password)) {
			        		String message = "You were sucessfully logged into the system!";
			        		
			        		String loginToken = generateLoginToken();
			        		
			        		obj= new JSONObject();
			                obj.put("status", OK_MESSAGE);
			                obj.put("token", loginToken);
			                data = obj.toString();
			        
			        		communication.sendInChunks(data);
			        		System.out.println("Client: " + user + "was sucessfully logged into the system!");			        		
			        	} else {
			        		String message = "Incorrect password. Try again!";
			        		sendNotOkMessage(message);
			        		System.out.println(message);
			        	}
		        	} else {
		        		String message = "You are not registered on the system...";
		        		sendNotOkMessage(message);
		        		System.out.println("Client: " + user + " is not registered on the system...");
		        	}
		        } catch (FileNotFoundException fnfe) {
		        	exceptionFile = "write";
		        	writeFile(REGISTER_CLIENTS_FILE, EMPTY);
		        	String error = "Server faced a problem while processing your request. Try again later...";	        	
		        	sendNotOkMessage(error);
		        	throw new ServerLibraryException("Could not find: \"" + REGISTER_CLIENTS_FILE + "\". Aborting...", true);
				} catch (JSONException jsone) {
					String error = "Server faced a problem while processing your request. Try again later...";	        	
					sendNotOkMessage(error);
					throw new ServerLibraryException("Server crashed while doing JSON Operations. Aborting...", true);
				}
			} catch (IOException ioe) {
				String error = "Server faced a problem while processing your request. Try again later...";	        	
	        	sendNotOkMessage(error);
				throw new ServerLibraryException("Could not " + exceptionFile + "\"" + REGISTER_CLIENTS_FILE + "\". Aborting...", true);
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
		        
		        synchronized(this) {
			        try {
			        	exceptionFile = "read";
			        	String jsonFileString = readFile(REGISTER_CLIENTS_FILE);
			        	if(jsonFileString==null || jsonFileString.equals("")) {
			        		initializeClientList(REGISTER_CLIENTS_FILE);
			        	}
			        	
			        	exceptionFile = "write";		        	
			        	obj = new JSONObject();
			        			        	
			        	if(signUpJSON(user, password, jsonFileString)) {
			        		String message = "You were sucessfully registered";
			        		sendOkMessage(message);
			        		System.out.println("Client was sucessfully registered!");
			        	} else {
			        		String message = "There was a user with that user name already on the system! Pick another one.";
			        		sendOkMessage(message);
			        		System.out.println(message);
			        	}
			        } catch (FileNotFoundException fnfe) {
			        	exceptionFile = "write";
			        	writeFile(REGISTER_CLIENTS_FILE, EMPTY);
			        	String error = "Server faced a problem while processing your request. Try again later...";	        	
			        	sendNotOkMessage(error);
			        	throw new ServerLibraryException("Could not find the register_clients.json file. Aborting...", true);
					} catch (JSONException jsone) {
						String error = "Server faced a problem while processing your request. Try again later...";	        	
						sendNotOkMessage(error);
						throw new ServerLibraryException("Server crashed while doing JSON Operations. Aborting...", true);
					}
		        }
			} catch (IOException ioe) {
				String error = "Server faced a problem while processing your request. Try again later...";	        	
				sendNotOkMessage(error);
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
				synchronized(this) {
					try {
						exceptionFile = "read";
						BufferedReader br = new BufferedReader(new FileReader(USERS_ALBUMS));
						String jsonFileString = br.readLine();
			        	if(jsonFileString==null || jsonFileString.equals("")) {
			        		initializeAlbum(USERS_ALBUMS);
			        	}				
						
			        	exceptionFile = "write";		        			        	
			        	String message;
			        	
			        	if(createAlbum(user, album, driveId, jsonFileString)) {
			        		message = "Client album was sucessfully created!";
			        		sendOkMessage(message);
			        	} else {
			        		message = "Could not create " + user + "'s album...";
			        		sendOkMessage(message);
			        	}
			        	
					} catch (FileNotFoundException fnfe) {
						exceptionFile = "write";
						writeFile(REGISTER_CLIENTS_FILE, EMPTY);
			        	String error = "Server faced a problem while processing your request. Try again later...";
			        	sendNotOkMessage(error);
			        	throw new ServerLibraryException("Could not find the \"users_albums.json\" Aborting...", true);
					}
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
				
				JSONObject albums_mapping = getJSONUsersAlbums(user, jsonFileString);
				obj = new JSONObject();
				if(albums_mapping==null || albums_mapping.isEmpty()) {
					String message = "You do not have a single album on the system!";
					obj.put("album-list", albums_mapping);
					data = obj.toString();
					communication.sendInChunks(data);
					sendNotOkMessage(message);
				} else {
					obj.put("album-list", albums_mapping);
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
