package pt.ulisboa.tecnico.cmu.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;

/**
 * Created by ist182069 on 27-03-2019.
 */

public class ClientTest {

    public static void main(String[] args) throws IOException, CommunicationsException, JSONException {
		String hostname = "localhost";
		String command = "GET-TOKEN";
		
        Socket socket = new Socket(hostname, 8080);

        Communications communication = new Communications(socket);        
        
        communication.sendInChunks(command);
        JSONObject obj = new JSONObject();
        obj.put("user-name", "xicocana");
        obj.put("password", "2435");
        String msg = obj.toString();
        communication.sendInChunks(msg);
        
        String data = (String) communication.receiveInChunks();
        System.out.println(data);
        
        /*
        communication.sendInChunks("SIGN-UP");
        JSONObject obj = new JSONObject();
        obj.put("user-name", "xicocana");
        obj.put("password", "lalelilolu");
        String msg = obj.toString();
        communication.sendInChunks(msg);
        
        String data = (String) communication.receiveInChunks();
        System.out.println(data);
        
        /*communication.sendInChunks("ADD-ALBUM");
        
		JSONObject obj = new JSONObject();
		obj.put("user-name", "dc");
		obj.put("album", "montanha");
		obj.put("drive-id", "4ffe80ac");
		obj.put("txt-id", "33fabc01");
		String getAlbums = obj.toString();
		communication.sendInChunks(getAlbums);*/
        /*
        communication.sendInChunks("GET-ALBUMS");
        
		JSONObject obj = new JSONObject();
		obj.put("user-name", "dc");
		String getAlbums = obj.toString();
		communication.sendInChunks(getAlbums);
        
        String data = (String) communication.receiveInChunks();
        System.out.println(data);*/
        /*JSONObject obj = new JSONObject();
        obj.put("user-name", "xicocana");
        String data = obj.toString();
        communication.sendInChunks(data);
        data = (String) communication.receiveInChunks();
        System.out.println("############### ###############");
        System.out.println(data);
        System.out.println("############### ###############");
        obj = new JSONObject(data);        
        obj = obj.getJSONObject("album-list");
        
        Iterator<String> keys = obj.keys();
        while(keys.hasNext()) {
        	System.out.println(keys.next());
        }
        
        
        data = (String) communication.receiveInChunks();*/
        communication.sendInChunks("EXIT"); 
    }
}
