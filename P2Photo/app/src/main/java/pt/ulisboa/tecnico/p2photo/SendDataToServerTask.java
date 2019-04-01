package pt.ulisboa.tecnico.p2photo;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import pt.ulisboa.tecnico.p2photo.Communications;
import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

class SendDataToServerTask extends AsyncTask<Void, Void, Void> {

    private String name = "";
    private String pswd = "";
    private String command = "";
    private String state = "waiting";
    private String message = null;

    public SendDataToServerTask(String name, String pswd, String command){
        this.name = name;
        this.pswd = pswd;
        this.command = command;
    }

    public String getStateOfRequest() {
        return this.state;
    }

    public void setStateOfRequest(String state) {
        this.state = state;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            String hostname = "192.168.43.141";

            System.out.println("entra aqui");
            Socket socket = new Socket(hostname, 8080);
            System.out.println(socket.getInetAddress().getHostAddress());
            Communications communication = new Communications(socket);

            JSONObject obj = new JSONObject();
            obj.put("user-name", name);
            obj.put("password", pswd);

            String data = obj.toString();
            communication.sendInChunks(command);
            communication.sendInChunks(data);

            data = (String) communication.receiveInChunks();
            obj = new JSONObject(data);
            if(obj.get("conclusion").equals("OK")) {
                this.setStateOfRequest("sucess");
                this.setMessage((String) obj.get("message"));
            }
            else if(obj.get("conclusion").equals("NOT-OK")) {
                this.setStateOfRequest("failure");
                this.setMessage((String) obj.get("message"));
            }

            communication.sendInChunks("EXIT");
            communication.end();


        } catch(UnknownHostException uhe) {
            uhe.printStackTrace();
            System.out.println("Couldn't find the host.");
        } catch(IOException ioe) {
            ioe.printStackTrace();
            System.out.println("IOException");
        } catch(CommunicationsException ce) {
            ce.printStackTrace();
            System.out.println("CommunicationsException");
        } catch (JSONException je){
            je.printStackTrace();
            System.out.println("JsonException");
        }
        return null;}
}