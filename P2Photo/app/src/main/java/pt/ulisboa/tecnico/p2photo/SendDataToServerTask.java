package pt.ulisboa.tecnico.p2photo;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

public class SendDataToServerTask extends AsyncTask<Void, Void, Void> {

    private String name = "";
    private String pswd = "";
    private String command = "";
    private String state = "waiting";
    private String message = null;
    private String hostname = "192.168.43.141";

    private String loginToken = "not_received";

    private int port = 8080;
    private JSONArray users = null;
    public ArrayList<String> userlist = new ArrayList<String>();
    public ArrayList<JSONArray> JSONuserAlbums = new ArrayList<JSONArray>();

    public String folderID = "";
    public String fileID = "";
    public String album = "";

    public SendDataToServerTask(String name, String pswd, String command){
        this.name = name;
        this.pswd = pswd;
        this.command = command;
    }

    public SendDataToServerTask(String name, String command){
        this.name = name;
        this.command = command;
    }

    public SendDataToServerTask(String name, String command, String folderId, String fileId, String album){
        this.name = name;
        this.command = command;
        this.folderID = folderId;
        this.fileID = fileId;
        this.album = album;
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

    public String getLoginToken() {
        return this.loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public void setUsers(JSONArray users) { this.users = users; }

    public JSONArray getUsers(){
        return this.users;
    }

    public ArrayList<String> getUserList(){
        return this.userlist;
    }

    public ArrayList<JSONArray> getJSONuserAlbums() { return this.JSONuserAlbums; }

    @Override
    protected Void doInBackground(Void... params) {
        if(command == "LOGIN") {
            try {
                Socket socket = new Socket(hostname, port);
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
                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");
                    this.setMessage((String) obj.get("message"));
                    String token = (String) obj.get("token");
                    this.setLoginToken(token);
                } else if (obj.get("conclusion").equals("NOT-OK")) {
                    this.setStateOfRequest("failure");
                    this.setMessage((String) obj.get("message"));
                }

                communication.sendInChunks("EXIT");
                communication.end();

            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
        }

        if(command == "SIGN-UP") {
            try {
                Socket socket = new Socket(hostname, port);
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
                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");
                    this.setMessage((String) obj.get("message"));
                } else if (obj.get("conclusion").equals("NOT-OK")) {
                    this.setStateOfRequest("failure");
                    this.setMessage((String) obj.get("message"));
                }

                communication.sendInChunks("EXIT");
                communication.end();

            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
        }

        if(command == "GET-USERS") {
            try {
                Socket socket = new Socket(hostname, port);
                System.out.println(socket.getInetAddress().getHostAddress());
                Communications communication = new Communications(socket);

                JSONObject obj = new JSONObject();
                obj.put("user-name", name);

                String data = obj.toString();
                communication.sendInChunks(command);
                communication.sendInChunks(data);

                data = (String) communication.receiveInChunks();
                obj = new JSONObject(data);
                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");
                    //this.setMessage((String) obj.get("message"));
                    JSONArray jsonArray = (JSONArray) obj.get("user-list");
                    int length = jsonArray.length();
                    if (length > 0) {
                        for (int i = 0; i < length; i++) {
                            userlist.add(jsonArray.getString(i));
                        }
                    }
                } else if (obj.get("conclusion").equals("NOT-OK")) {
                    this.setStateOfRequest("failure");
                    this.setMessage((String) obj.get("message"));
                }

                communication.sendInChunks("EXIT");
                communication.end();

            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
        }
        if(command == "ADD-ALBUM"){
            try {
                Socket socket = new Socket(hostname, port);
                System.out.println(socket.getInetAddress().getHostAddress());
                Communications communication = new Communications(socket);

                JSONObject obj = new JSONObject();
                obj.put("user-name", name);
                obj.put("album", album);
                obj.put("drive-id", folderID);
                obj.put("txt-id", fileID);

                String data = obj.toString();
                communication.sendInChunks(command);
                communication.sendInChunks(data);

                data = (String) communication.receiveInChunks();
                obj = new JSONObject(data);
                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");

                } else if (obj.get("conclusion").equals("NOT-OK")) {
                    this.setStateOfRequest("failure");
                    this.setMessage((String) obj.get("message"));
                }

                communication.sendInChunks("EXIT");
                communication.end();

            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
        }
        if(command == "GET-ALBUMS"){
            try {
                Socket socket = new Socket(hostname, port);
                System.out.println(socket.getInetAddress().getHostAddress());
                Communications communication = new Communications(socket);

                JSONObject obj = new JSONObject();
                obj.put("user-name", name);

                String data = obj.toString();
                communication.sendInChunks(command);
                communication.sendInChunks(data);

                data = (String) communication.receiveInChunks();
                obj = new JSONObject(data);

                this.setStateOfRequest("sucess");
                //this.setMessage((String) obj.get("message"));
                JSONArray jsonArray = new JSONArray();
                jsonArray = obj.getJSONArray("album-list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray jsonArray2 =  jsonArray.getJSONArray(i);
                    JSONuserAlbums.add(jsonArray2);
                }

                communication.sendInChunks("EXIT");
                communication.end();

            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
        }

        if(command.equals("GET-TOKEN")) {
            try {
                Socket socket = new Socket(hostname, port);
                System.out.println(socket.getInetAddress().getHostAddress());
                Communications communication = new Communications(socket);

                JSONObject obj = new JSONObject();
                obj.put("user-name", name);

                String data = obj.toString();
                communication.sendInChunks(command);
                communication.sendInChunks(data);

                data = (String) communication.receiveInChunks();
                obj = new JSONObject(data);

                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");
                    this.setMessage((String) obj.get("message"));
                    String token = (String) obj.get("token");
                    this.setLoginToken(token);
                } else if (obj.get("conclusion").equals("NOT-OK")) {
                    this.setStateOfRequest("failure");
                    this.setMessage((String) obj.get("message"));
                }

                communication.sendInChunks("EXIT");
                communication.end();

            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
        }
        return null;}

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

}