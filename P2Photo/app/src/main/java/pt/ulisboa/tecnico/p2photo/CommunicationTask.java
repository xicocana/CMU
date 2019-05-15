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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;
import pt.ulisboa.tecnico.p2photo.exceptions.TaskException;
import pt.ulisboa.tecnico.p2photo.exceptions.UtilsException;

public class CommunicationTask extends AsyncTask<Void, Void, Void> {

    private static final String OK = "OK";
    private static final String NOT_OK = "NOT-OK";

    private static final String LOGIN = "LOGIN";
    private static final String SIGN_UP = "SIGN-UP";
    private static final String ADD_ALBUM = "ADD-ALBUM";
    private static final String GET_ALBUMS = "GET-ALBUMS";
    private static final String GET_USERS = "GET-USERS";
    private static final String GET_TOKEN = "GET-TOKEN";
    private static final String EXIT = "EXIT";
    private static final String ADD_USER = "ADD-USER";

    private Socket socket = null;
    private String name = "";
    private String sharedUser = "";
    private String email = "";
    private String password = "";
    private String command = "";
    private String state = "waiting";
    private String message = null;
    private String hostname = "192.168.43.112";
    private String loginToken = "not_received";

    private int port = 8080;

    private ArrayList<String> usernames = null;
    private ArrayList<ArrayList<String>> userAlbums = new ArrayList<ArrayList<String>>();

    public String folderID = "";
    public String fileID = "";
    public String album = "";

    public ArrayList<String[]> getUsernamesAndEmails() {
        return usernamesAndEmails;
    }

    public void setUsernamesAndEmails(ArrayList<String[]> usernamesAndEmails) {
        this.usernamesAndEmails = usernamesAndEmails;
    }

    private ArrayList<String[]> usernamesAndEmails = null;

    public CommunicationTask(String command) {
        this.command = command;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private void UserAlbumsJSONArrayToArrayList(JSONArray jsonAlbumList) throws JSONException {

        Map<String,Integer> mapNames = new HashMap<>();
        String name = "";
        ArrayList<ArrayList<String>> albums = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < jsonAlbumList.length(); i++) {
            boolean alreadyExists = false;
            JSONArray jsonAlbumAttributes = (JSONArray) jsonAlbumList.get(i);
            ArrayList<String> albumAttributes = new ArrayList<String>();
            for (int j = 0; j < jsonAlbumAttributes.length(); j++) {
                if (j == 0){
                    name = (String) jsonAlbumAttributes.get(j);
                    if (!mapNames.containsKey(name)){
                        mapNames.put(name,i);
                    }else{
                        alreadyExists = true;
                    }
                }

                String attribute = (String) jsonAlbumAttributes.get(j);
                albumAttributes.add(attribute);
            }

            if (alreadyExists){
               int position =  mapNames.get(name);
               albums.get(position).addAll(albumAttributes);
            }else{
                albums.add(albumAttributes);
            }

        }

        setUserAlbums(albums);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSharedUser() {
        return this.sharedUser;
    }

    public void setSharedUser(String sharedUser) {
        this.sharedUser = sharedUser;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String getStateOfRequest() {
        return this.state;
    }

    public void setStateOfRequest(String state) {
        this.state = state;
    }

    public String getLoginToken() {
        return this.loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public ArrayList<String> getUsers() {
        return this.usernames;
    }

    public void setUsers(ArrayList<String> usernames) {
        this.usernames = usernames;
    }

    public ArrayList<ArrayList<String>> getUserAlbums() {
        return this.userAlbums;
    }

    public void setUserAlbums(ArrayList<ArrayList<String>> userAlbums) {
        this.userAlbums = userAlbums;
    }

    public String getFolderId() {
        return this.folderID;
    }

    public void setFolderId(String folderID) {
        this.folderID = folderID;
    }

    public String getFileId() {
        return this.fileID;
    }

    public void setFileId(String fileID) {
        this.fileID = fileID;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            this.socket = new Socket(hostname, port);
        } catch (IOException ioe) {
            System.err.println("doInBackground(): Could not open client socket!");
        }

        Communications communication = new Communications(socket);


        switch (command) {
            case LOGIN:
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user-name", name);
                    jsonObject.put("password", password);

                    String sendData = jsonObject.toString();
                    Utils.sendMessage(communication, command);
                    Utils.sendMessage(communication, sendData);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);
                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        String token = (String) recvJSONObject.get("token");
                        this.setLoginToken(token);
                        this.setStateOfRequest("success");
                    } else if (recvJSONObject.get("conclusion").equals(NOT_OK)) {
                        this.setStateOfRequest("failure");
                        this.setMessage((String) recvJSONObject.get("message"));
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to login...");
                        String message = "Something went terribly wrong while trying to login...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }

                    Utils.sendMessage(communication, EXIT);

                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to login...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }

            case SIGN_UP:
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user-name", name);
                    jsonObject.put("email", email);
                    jsonObject.put("password", password);

                    String sendData = jsonObject.toString();
                    Utils.sendMessage(communication, command);
                    Utils.sendMessage(communication, sendData);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);
                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("success");
                    } else if (recvJSONObject.get("conclusion").equals(NOT_OK)) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("failure");
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to sign up...");
                        String message = "Something went terribly wrong while trying to sign up...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }
                    Utils.sendMessage(communication, EXIT);

                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to sign up...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }

            case GET_USERS:
                try {
                    Utils.sendMessage(communication, command);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);
                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        JSONArray jsonArray = (JSONArray) recvJSONObject.get("user-list");
                        ArrayList<String[]> usersAndEmails = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONArray userX = (JSONArray) jsonArray.get(i);
                            usersAndEmails.add(new String[]{(String) userX.get(0), (String) userX.get(1)});
                        }
                        setUsernamesAndEmails(usersAndEmails);
                        // setUsers(usernames);
                        this.setStateOfRequest("success");
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to get users...");
                        String message = "Something went terribly wrong while trying to get users...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }
                    Utils.sendMessage(communication, EXIT);
                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to get users...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }

            case ADD_ALBUM:
                try {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user-name", name);
                    jsonObject.put("album", album);
                    jsonObject.put("drive-id", folderID);
                    jsonObject.put("txt-id", fileID);
                    String sendData = jsonObject.toString();

                    Utils.sendMessage(communication, command);
                    Utils.sendMessage(communication, sendData);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);
                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("success");
                    } else if (recvJSONObject.get("conclusion").equals(NOT_OK)) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("failure");
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to add album...");
                        String message = "Something went terribly wrong while trying to add album...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }
                    Utils.sendMessage(communication, EXIT);

                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to add album...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }

            case GET_ALBUMS:
                try {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user-name", name);

                    String sendData = jsonObject.toString();
                    Utils.sendMessage(communication, command);
                    Utils.sendMessage(communication, sendData);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);

                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        JSONArray jsonAlbumList = (JSONArray) recvJSONObject.get("album-list");
                        UserAlbumsJSONArrayToArrayList(jsonAlbumList);
                        this.setStateOfRequest("success");
                    } else if (recvJSONObject.get("conclusion").equals(NOT_OK)) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("failure");
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to get albums...");
                        String message = "Something went terribly wrong while trying to get albums...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }

                    Utils.sendMessage(communication, EXIT);

                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to get albums...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }


            case GET_TOKEN:
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user-name", name);
                    jsonObject.put("password", password);

                    String sendData = jsonObject.toString();
                    Utils.sendMessage(communication, command);
                    Utils.sendMessage(communication, sendData);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);

                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        String token = (String) recvJSONObject.get("token");
                        this.setLoginToken(token);
                        this.setStateOfRequest("success");
                    } else if (recvJSONObject.get("conclusion").equals("NOT-OK")) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("failure");
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to get token...");
                        String message = "Something went terribly wrong while trying to get token...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }

                    Utils.sendMessage(communication, EXIT);

                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to get token...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }

            case ADD_USER:
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user-name", name);
                    jsonObject.put("share-name", sharedUser);
                    jsonObject.put("album", album);

                    String sendData = jsonObject.toString();
                    Utils.sendMessage(communication, command);
                    Utils.sendMessage(communication, sendData);

                    String recvData = (String) Utils.receiveMessage(communication);
                    JSONObject recvJSONObject = new JSONObject(recvData);

                    if (recvJSONObject.get("conclusion").equals(OK)) {
                        String message = (String) recvJSONObject.get("message");
                        this.setMessage(message);
                        this.setStateOfRequest("success");
                    } else if (recvJSONObject.get("conclusion").equals("NOT-OK")) {
                        this.setMessage((String) recvJSONObject.get("message"));
                        this.setStateOfRequest("failure");
                    } else {
                        System.err.println("doInBackground(): Something went terribly wrong while trying to add user to album...");
                        String message = "Something went terribly wrong while trying to add user to album...";
                        this.setMessage(message);
                        this.setStateOfRequest("failure");
                    }

                    Utils.sendMessage(communication, EXIT);

                } catch (JSONException jsone) {
                    System.err.println("doInBackground(): Got a JSON error while trying to add user to album...");
                } catch (UtilsException ui) {
                    System.err.println("doInBackground(): Utils class broke down...");
                }


        }


        try {
            communication.end();
        } catch (CommunicationsException ce) {
            System.err.println("doInBackground(): Something went wrong while trying to end the connection with client...");
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

}