package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.SharedPreferences;

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
            CommunicationUtilities communicationUtilities = new CommunicationUtilities(context);
            boolean state = communicationUtilities.sendGetToken(username, password);
            return proceedAccordingToState(communicationUtilities, state, sessionKey);
        }

        return false;
    }

    private boolean proceedAccordingToState(CommunicationUtilities communicationUtilities, boolean state, String sessionKey) {
        if(state) {
            //get session key
            String fetchedSessionKey = (String) communicationUtilities.getContent();
            if(sessionKey != null) {
                System.out.println("$$$");
                System.out.println(sessionKey);
                System.out.println(fetchedSessionKey);
                System.out.println("$$$");
                if (sessionKey.equals(fetchedSessionKey)) {
                    return true;
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
