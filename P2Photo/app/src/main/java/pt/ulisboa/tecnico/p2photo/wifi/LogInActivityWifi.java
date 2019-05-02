package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;

import java.io.IOException;

import pt.ulisboa.tecnico.p2photo.CommunicationUtilities;
import pt.ulisboa.tecnico.p2photo.PersistentLogin;
import pt.ulisboa.tecnico.p2photo.R;
import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

public class LogInActivityWifi extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";

    public final static String NAME = "pt.ulisboa.tecnico.p2photo.NAME";
    public final static String PASSWORD = "pt.ulisboa.tecnico.p2photo.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        PersistentLogin persistentLogin = new PersistentLogin(pref, getApplicationContext());
        //if(persistentLogin.tryToLogin()) {
            Intent intent = new Intent(LogInActivityWifi.this, UserOptionsActivityWifi.class);
            startActivity(intent);
        //}
    }

    public void logIn(View v) throws IOException, CommunicationsException, JSONException, InterruptedException {
        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();

        CommunicationUtilities communicationUtilities = new CommunicationUtilities(this.getApplicationContext());
        boolean state = communicationUtilities.sendLogin(name, password);
        proceedAccordingToState(communicationUtilities, state, name, password);
    }

    public void cancelLogIn(View v) {
        LogInActivityWifi.this.finish();
    }

    private void proceedAccordingToState(CommunicationUtilities communicationUtilities, boolean state, String username, String password) {
        if(state) {
            //get session key
            String sessionKey = (String) communicationUtilities.getContent();
            setSharedPreferences(sessionKey, username, password);

            Intent intent = new Intent(LogInActivityWifi.this, UserOptionsActivityWifi.class);
            LogInActivityWifi.this.finish();
            startActivity(intent);
        }
        else {}
    }

    private void setSharedPreferences(String sessionKey, String username, String password) {
        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        // Set/Store data
        edit.putString("session_key", sessionKey);
        edit.putString("username", username);
        edit.putString("password", password);
        Log.i("SESSION", sessionKey);
        // Commit the changes
        edit.commit();
    }
}
