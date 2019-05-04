package pt.ulisboa.tecnico.p2photo.cloud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import pt.ulisboa.tecnico.p2photo.PersistentLogin;
import pt.ulisboa.tecnico.p2photo.R;

public class MainActivity extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        PersistentLogin persistentLogin = new PersistentLogin(pref, getApplicationContext());
        if(persistentLogin.tryToLogin()) {
            Intent intent = new Intent(MainActivity.this, UserOptionsActivity.class);
            startActivity(intent);
        }
    }

    public void logInActivity(View v) {
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(intent);
    }

    public void signUpActivity(View v) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

}
