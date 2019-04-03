package pt.ulisboa.tecnico.cmu.client;

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
	String command = "ADD-ALBUM";
        System.out.println("entra aqui");
        Socket socket = new Socket(hostname, 8080);
	System.out.println(socket.getInetAddress().getHostAddress());
        Communications communication = new Communications(socket);
        
        JSONObject obj = new JSONObject();
        obj.put("user-name", "foo");
        obj.put("drive-id", "bar");
        String data = obj.toString();
        communication.sendInChunks(command);
        communication.sendInChunks(data);
        communication.sendInChunks("EXIT");
        communication.end();        
    }
}
