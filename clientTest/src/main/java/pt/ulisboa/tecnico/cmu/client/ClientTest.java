package pt.ulisboa.tecnico.cmu.client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

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

        String command = "SIGN-UP";

        JSONObject obj = new JSONObject();
        obj.put("user-name", "xicocana");
        obj.put("password", "password");
        String data = obj.toString();
        communication.sendInChunks(command);
        communication.sendInChunks(data);

        communication.end();        
    }
}
