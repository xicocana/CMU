package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by ist182069 on 19-04-2019.
 */

public class PersistentLogin {

    private static final String SESSION_KEY = "session_key";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    SharedPreferences sharedPreferences;
    Context context;

    public PersistentLogin(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public boolean tryToLogin() {
        String sessionKey = this.sharedPreferences.getString(SESSION_KEY, null);
        String username = this.sharedPreferences.getString(USERNAME, null);
        String password = this.sharedPreferences.getString(PASSWORD, null);

        System.out.println(username);
        System.out.println(password);
        if(username!=null && password!=null) {
            ClientServerComms clientServerComms = new ClientServerComms(context);
            boolean state = clientServerComms.sendGetToken(username, password);
            return proceedAccordingToState(clientServerComms, state);
        }

        return false;
    }

    private boolean proceedAccordingToState(ClientServerComms clientServerComms, boolean state) {
        if(state) {
            //get session key
            String sessionKey = (String) clientServerComms.getContent();
            if(sessionKey != null) {
                if (sessionKey.equals(sessionKey)) {
                    return true;
                    /*Intent intent = new Intent(MainActivity.this, UserOptionsActivity.class);
                    startActivity(intent);*/
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }
}
