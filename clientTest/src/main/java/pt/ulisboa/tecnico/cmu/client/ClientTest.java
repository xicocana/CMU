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

        System.out.println("entra aqui");
        Socket socket = new Socket("localhost", 5111);
        System.out.println(socket.getInetAddress().getHostAddress());
        Communications communication = new Communications(socket);
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("just insert user name. This is just a silly test class");
        String input = scanner.nextLine();
        
        System.out.println("Insert the password");
        String password = scanner.nextLine();
        //String command = "SIGN-UP";
        String command = "LOGIN";

        JSONObject obj = new JSONObject();
        obj.put("user-name", input);
        obj.put("password", password);
        String data = obj.toString();
        communication.sendInChunks(command);
        communication.sendInChunks(data);
        
        data = (String) communication.receiveInChunks();
        obj = new JSONObject(data);
        if(obj.get("conclusion").equals("OK")) {
        	System.out.println(obj.get("message"));
        }
        else if(obj.get("conclusion").equals("NOT-OK")) {
        	System.out.println(obj.get("message"));
        }
        
        communication.end();        
    }
}
