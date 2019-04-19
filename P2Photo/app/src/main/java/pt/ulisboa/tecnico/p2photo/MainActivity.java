package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String SESSION_KEY = "session_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String sessionKey = pref.getString(SESSION_KEY, null);

        ClientServerComms clientServerComms = new ClientServerComms(this.getApplicationContext());
        boolean state = clientServerComms.sendGetToken("xicocana");
        proceedAccordingToState(clientServerComms, state);
    }

    public void logInActivity(View v) {
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(intent);
    }

    public void signUpActivity(View v) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void proceedAccordingToState(ClientServerComms clientServerComms, boolean state) {
        if(state) {
            //get session key
            String sessionKey = (String) clientServerComms.getContent();

            if(sessionKey != null) {
                if (sessionKey.equals(sessionKey)) {
                    Log.i("SESSION", "entreiii");
                    Intent intent = new Intent(MainActivity.this, UserOptionsActivity.class);
                    startActivity(intent);
                }
            }
        }
        else {}
    }
}
