package pt.ulisboa.tecnico.cmu.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import pt.ulisboa.tecnico.cmu.server.exceptions.ParserException;

public class Parser {
	
	private static final String USER_ALBUMS = "users_albums.json";
	private static final String REGISTER_CLIENTS = "register_clients.json"; 
	
	private String jsonString;
	private String user;
	private String password;
	
	public Parser(String jsonString, String user, String password) {
		this.jsonString = jsonString;
		this.user = user;
		this.password = password;
	}
	
	public Parser(String jsonString, String user) {
		this.jsonString = jsonString;
		this.user = user;
	}
	
	private void addUserToAlbum(JSONObject jsonFile, JSONObject user_albums, String album, String driveId) throws IOException {
		user_albums.put(album, driveId);
		jsonFile.put(this.user, user_albums);
		
		String fileString = jsonFile.toString();
		BufferedWriter bw = new BufferedWriter(new FileWriter(USER_ALBUMS));
		bw.write(fileString);
		bw.close();		
	}
	
	private boolean hasUser() {
		JSONObject jsonFile = new JSONObject(this.jsonString);
		
		if(jsonFile.has(this.user)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean hasAlbum(String album) {
		JSONObject jsonFile = new JSONObject(this.jsonString);
		JSONObject user_albums = (JSONObject) jsonFile.get(this.user);
		
		if(user_albums.has(album)==true) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean parseAlbum(String album, String driveId) throws IOException {		
		JSONObject jsonFile = new JSONObject(this.jsonString);
		JSONObject user_albums;
		
		if(!hasUser()) {
			user_albums = new JSONObject();
			addUserToAlbum(jsonFile, user_albums, album, driveId);
			return true;
		} else {
			if(hasAlbum(album)) {
				return false;
			} else {
				user_albums = jsonFile.getJSONObject(user);
				addUserToAlbum(jsonFile, user_albums, album, driveId);
				return true;
			}
		}
	}
	
	public boolean parseChanges() throws IOException, JSONException {								
	    	JSONObject jsonFile = new JSONObject(this.jsonString);
	    	
	    	if(jsonFile.has(this.user)==true) {
	    		return false;
	    	} else {
	    		jsonFile.put(this.user, this.password);
		    	String replaceJSONFile = jsonFile.toString();
		    	System.out.println(replaceJSONFile);
				BufferedWriter bw;
				
				bw = new BufferedWriter(new FileWriter(REGISTER_CLIENTS));
				bw.write(replaceJSONFile);
				bw.close();
				
				return true; 	
	    	}
	}
	
}