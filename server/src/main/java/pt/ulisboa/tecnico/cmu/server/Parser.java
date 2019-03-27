package pt.ulisboa.tecnico.cmu.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Parser {
	
	private static final String OWNER_ID = "owner-id";
	private static final String STATE = "state";
	private String jsonString;
	private String user;
	private String password;
	
	public Parser(String jsonString, String user, String password) {
		this.jsonString = jsonString;
		this.user = user;
		this.password = password;
	}
	
	public void parseChanges() {		
		try {			
	    	JSONObject jsonFile = new JSONObject(this.jsonString);
	    	jsonFile.put(this.user, this.password);
	    	String replaceJSONFile = jsonFile.toString();
	    	System.out.println(replaceJSONFile);
			BufferedWriter bw;
			
			bw = new BufferedWriter(new FileWriter("register_clients.json"));
			bw.write(replaceJSONFile);
			bw.close();
		} catch (IOException ioe) {
			System.out.println("IOException...");
		}	
		
	}
}
