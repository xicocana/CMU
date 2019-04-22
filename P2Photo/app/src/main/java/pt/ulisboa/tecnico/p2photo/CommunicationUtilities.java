package pt.ulisboa.tecnico.p2photo;

import android.widget.Toast;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by ist182069 on 19-04-2019.
 */

public class CommunicationUtilities {

    private final static String LOGIN = "LOGIN";
    private final static String SIGN_UP = "SIGN-UP";
    private final static String TOKEN = "GET-TOKEN";
    private final static String GET_USERS = "GET-USERS";
    private final static String GET_ALBUMS = "GET-ALBUMS";
    private final static String ADD_USER = "ADD-USER";

    private Object content;

    private Context context;

    public CommunicationUtilities(Context context) {
        this.context = context;
    }

    private void setContent(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return this.content;
    }

    private void displayMessage(CommunicationTask task) {
        String message = task.getMessage();
        //Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }

    private boolean getPublisherState(CommunicationTask task) {
        for(int i = 1; i<=10; i++) {
            try {
                //numero de segundos que fica a espera: millis*10
                Thread.sleep(650);
                if(task.getStateOfRequest().equals("success")) {
                    displayMessage(task);
                    return true;
                } else if(task.getStateOfRequest().equals("failure")) {
                    displayMessage(task);
                    return false;
                }
            } catch(InterruptedException ie) {
                System.err.println("Could not properly put the thread to sleep...");
                Toast.makeText(this.context, "Critical error!", Toast.LENGTH_SHORT).show();
                return false;

            }
        }
        Toast.makeText(this.context, "Could not obtain an answer back from the server!", Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean sendSignUp(String name,String email, String password) {
        CommunicationTask task = new CommunicationTask(SIGN_UP);
        task.setName(name);
        task.setPassword(password);
        task.setEmail(email);
        task.execute();
        return getPublisherState(task);
    }

    public boolean sendLogin(String name, String password) {
        CommunicationTask task = new CommunicationTask(LOGIN);
        task.setName(name);
        task.setPassword(password);
        task.execute();
        if(getPublisherState(task)) {
            String loginToken = task.getLoginToken();
            setContent(loginToken);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendGetToken(String name, String password) {
        CommunicationTask task = new CommunicationTask(TOKEN);
        task.setName(name);
        task.setPassword(password);
        task.execute();
        if(getPublisherState(task)) {
            String loginToken = task.getLoginToken();
            setContent(loginToken);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendGetUsers() {
        CommunicationTask task = new CommunicationTask(GET_USERS);
        task.execute();
        if(getPublisherState(task)) {
            ArrayList<String[]> usersAndEmails = task.getUsernamesAndEmails();
            setContent(usersAndEmails);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendGetAlbums(String name) {
        CommunicationTask task = new CommunicationTask(GET_ALBUMS);
        task.setName(name);
        task.execute();
        if(getPublisherState(task)) {
            ArrayList<ArrayList <String>> userAlbums = task.getUserAlbums();
            setContent(userAlbums);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendAddUser(String name, String share, String album) {
        CommunicationTask task = new CommunicationTask(ADD_USER);
        task.setName(name);
        task.setSharedUser(share);
        task.setAlbum(album);
        if(getPublisherState(task)) {
            String message = task.getMessage();
            setContent(message);
            return true;
        } else {
            return false;
        }
    }
}
