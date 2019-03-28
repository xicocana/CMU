package pt.ulisboa.tecnico.cmu.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class Parser {
	
	private String jsonString;
	private String user;
	private String password;
	
	public Parser(String jsonString, String user, String password) {
		this.jsonString = jsonString;
		this.user = user;
		this.password = password;
	}
	
	public boolean parseChanges() throws IOException, JSONException {								
	    	JSONObject jsonFile = new JSONObject(this.jsonString);
	    	
	    	System.out.println(jsonFile.has(this.user));
	    	System.out.println(jsonFile);
	    	if(jsonFile.has(this.user)==true) {
	    		return false;
	    	} else {
	    		jsonFile.put(this.user, this.password);
		    	String replaceJSONFile = jsonFile.toString();
		    	System.out.println(replaceJSONFile);
				BufferedWriter bw;
				
				bw = new BufferedWriter(new FileWriter("register_clients.json"));
				bw.write(replaceJSONFile);
				bw.close();
				
				return true; 	
	    	}
	}
}