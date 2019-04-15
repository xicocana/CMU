package pt.ulisboa.tecnico.cmu.server;

import org.json.JSONException;
import org.json.JSONObject;

import pt.ulisboa.tecnico.cmu.server.exceptions.UtilsException;
import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;

public class Utils {
	
	public Utils() {	
	}
	
	static void sendMessage(Communications communication, String message) throws UtilsException {
		try {
			communication.sendInChunks(message);
		} catch (CommunicationsException ce) {
            throw new UtilsException("sendMessage(): Communications module broke down.", ce);
		}
	}
	
	 static String receiveMessage(Communications communication) throws UtilsException {
	        String receivedString;
	        try {
	            receivedString = (String) communication.receiveInChunks();
	        } catch (CommunicationsException ce) {
	            throw new UtilsException("receiveMessage(): Communications module broke down...", ce);
	        }
	        return receivedString;
	}
	 
	static JSONObject getJSONFromString(String jsonString) throws UtilsException {		
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
		} catch(JSONException je) {
			throw new UtilsException("convertStringToJSON(): String '" + jsonString + "' is not a valid JSON string.", je);
		}
		
		return jsonObject;
	}
	
    private static Boolean jsonKeyExists(JSONObject jsonObject, String key) {
        return jsonObject.has(key);
}
    
	static Object getObjectByJSONKey(JSONObject jsonObject, String key) throws UtilsException {
        if (!jsonKeyExists(jsonObject, key)) {
            throw new UtilsException("jsonGetObjectByKey(): Key '" + key + "'  does not exist.");
        }
        return jsonObject.get(key);
	}

}
