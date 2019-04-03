package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //public static final String MyPREFERENCES = "MyPrefs";
    //SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String myKey = pref.getString("session_key", null);

        if(myKey != null) {
            if (myKey.equals("sessionkey")) {
                Log.i("SESSION", "entreiii");
                Intent intent = new Intent(MainActivity.this, UserOptionsActivity.class);
                startActivity(intent);
            }
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
