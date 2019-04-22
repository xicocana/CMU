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
		String command = "";
		JSONObject obj;
		String msg;
		String data;
		String driveId;
		String txtId;
		String sharee;
		String album;

        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Insert your username: ");
        String userName = (String) scanner.nextLine();
        
        Socket socket = new Socket(hostname, 8080);

        Communications communication = new Communications(socket);
        while(!command.equals("EXIT")) {
        	System.out.println("Insert command below: ");
        	command = (String) scanner.nextLine();
        	switch(command) {
        		case "GET-ALBUMS":
        	        communication.sendInChunks(command);
        	        obj = new JSONObject();
        	        obj.put("user-name", userName);
        	        msg = obj.toString();
        	        communication.sendInChunks(msg);
        	        
        	        data = (String) communication.receiveInChunks();
        	        System.out.println(data);
        	        break;
        		case "GET-USERS":
        	        communication.sendInChunks(command);
        	        
        	        data = (String) communication.receiveInChunks();
        	        System.out.println(data);
        	        break;
        		case "ADD-USER":
        			System.out.println("Insert user to share album: ");
        			sharee = (String) scanner.nextLine();
        			System.out.println("Add the album: ");
        			album = (String) scanner.nextLine();
        			communication.sendInChunks(command);
        	        obj = new JSONObject();
        	        
        	        obj.put("user-name", userName);
        	        obj.put("album", album);
        	        obj.put("share-name", sharee);
        	        
        	        msg = obj.toString();
        	        communication.sendInChunks(msg);
        	        
        	        data = (String) communication.receiveInChunks();
        	        System.out.println(data);
        	        break;
        		case "ADD-ALBUM":
        			System.out.println("Add the album: ");
        			album = (String) scanner.nextLine();
        			System.out.println("Add the drive Id: ");
        			driveId = (String) scanner.nextLine();
        			System.out.println("Add the txt Id: ");
        			txtId = (String) scanner.nextLine();   
        			
        			communication.sendInChunks(command);
        	        obj = new JSONObject();
        	        
        	        obj.put("user-name", userName);
        	        obj.put("album", album);
        	        obj.put("drive-id", driveId);
        	        obj.put("txt-id", txtId);
        	        
        	        msg = obj.toString();
        	        communication.sendInChunks(msg);
        	        
        	        data = (String) communication.receiveInChunks();
        	        System.out.println(data);
        	        break;
        		default:
        			System.out.println("That command does not exist!");
        	}
        }
        
        communication.sendInChunks("EXIT"); 
    }
}
