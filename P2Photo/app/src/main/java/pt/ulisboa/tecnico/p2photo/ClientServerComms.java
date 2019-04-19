package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.widget.Toast;
import android.content.Context;

import org.json.JSONObject;

/**
 * Created by ist182069 on 19-04-2019.
 */

public class ClientServerComms {

    private final static String LOGIN = "LOGIN";
    private final static String SIGN_UP = "SIGN-UP";
    private final static String TOKEN = "GET-TOKEN";

    private Object content;

    private Context context;

    public ClientServerComms(Context context) {
        this.context = context;
    }

    private void setContent(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return this.content;
    }

    private void displayMessage(SendDataToServerTask task) {
        String message = task.getMessage();
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    }

    private boolean getPublisherState(SendDataToServerTask task) {
        for(int i = 1; i<=10; i++) {
            try {
                //numero de segundos que fica a espera: millis*10
                Thread.sleep(650);
                if(task.getStateOfRequest().equals("sucess")) {
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

    public boolean sendSignUp(String name, String password) {
        SendDataToServerTask task = new SendDataToServerTask(name, password, SIGN_UP);
        task.execute();
        return getPublisherState(task);
    }

    public boolean sendLogin(String name, String password) {
        SendDataToServerTask task = new SendDataToServerTask(name, password, LOGIN);
        task.execute();
        if(getPublisherState(task)) {
            String loginToken = task.getLoginToken();
            setContent(loginToken);
            return true;
        } else {
            return false;
        }
    }

    //TODO terei de passar a password tambem
    public boolean sendGetToken(String name, String password) {
        SendDataToServerTask task = new SendDataToServerTask(name, password, TOKEN);
        task.execute();
        if(getPublisherState(task)) {
            String loginToken = task.getLoginToken();
            setContent(loginToken);
            return true;
        } else {
            return false;
        }
    }

}
