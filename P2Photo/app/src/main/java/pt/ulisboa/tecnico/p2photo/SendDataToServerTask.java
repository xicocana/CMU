package pt.ulisboa.tecnico.p2photo;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
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

    private ArrayList<String> userNames = null;
    public ArrayList<ArrayList <String>> userAlbums = new ArrayList<ArrayList<String>>();

    public String folderID = "";
    public String fileID = "";
    public String album = "";

    public SendDataToServerTask(String command){
        this.command = command;
    }

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

    public void setUsers(ArrayList<String> userNames) { this.userNames = userNames; }

    public ArrayList<String> getUsers(){
        return this.userNames;
    }

    public void setUserAlbums(ArrayList<ArrayList <String>> userAlbums) { this.userAlbums = userAlbums; }

    public ArrayList<ArrayList <String>> getUserAlbums() { return this.userAlbums; }

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

                communication.sendInChunks(command);

                String data = (String) communication.receiveInChunks();
                JSONObject obj = new JSONObject(data);
                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");

                    JSONArray jsonArray = (JSONArray) obj.get("user-list");

                    ArrayList<String> userNames = new ArrayList<String>();
                    for(int i = 0; i<jsonArray.length(); i++) {
                        String userName = (String) jsonArray.get(i);
                        userNames.add(userName);
                    }

                    setUsers(userNames);
                } else {
                    this.setStateOfRequest("failure");
                    this.setMessage("Something went wrong with the server while processing your request...");
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

                if (obj.get("conclusion").equals("OK")) {
                    this.setStateOfRequest("sucess");
                    JSONArray jsonAlbumList = (JSONArray) obj.get("album-list");
                    UserAlbumsJSONArrayToArrayList(jsonAlbumList);
                } else {
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

        if(command.equals("GET-TOKEN")) {
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

    private void UserAlbumsJSONArrayToArrayList(JSONArray jsonAlbumList) throws JSONException {

        ArrayList<ArrayList<String>> albums = new ArrayList<ArrayList<String>>();
        for(int i = 0; i<jsonAlbumList.length(); i++) {
            JSONArray jsonAlbumAttributes = (JSONArray) jsonAlbumList.get(i);
            ArrayList<String> albumAttributes = new ArrayList<String>();
            for(int j = 0; j<jsonAlbumAttributes.length(); j++) {
                String attribute = (String) jsonAlbumAttributes.get(j);
                albumAttributes.add(attribute);
            }
            albums.add(albumAttributes);
        }

        setUserAlbums(albums);
    }

}