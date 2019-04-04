package pt.ulisboa.tecnico.cmu.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;

/**
 * Created by ist182069 on 27-03-2019.
 */

public class ClientTest {

    public static void main(String[] args) throws IOException, CommunicationsException, JSONException {
		String hostname = "localhost";
		String command = "GET-ALBUMS";
		
        Socket socket = new Socket(hostname, 8080);

        Communications communication = new Communications(socket);        
        
        communication.sendInChunks(command);
        JSONObject obj = new JSONObject();
        obj.put("user-name", "xicocana");
        String data = obj.toString();
        communication.sendInChunks(data);
        data = (String) communication.receiveInChunks();
        obj = new JSONObject(data);        
        JSONArray array = (JSONArray) obj.get("album-list");
        
        for (int i = 0; i < array.length(); i++) {
        	  System.out.println((String) array.get(i));
        }
        
        data = (String) communication.receiveInChunks();
        communication.sendInChunks("EXIT");
    }
}
