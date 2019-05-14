package pt.ulisboa.tecnico.p2photo.wifi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;

import pt.ulisboa.tecnico.p2photo.PersistentLogin;
import pt.ulisboa.tecnico.p2photo.R;

public class MainActivityWifi extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        String folder_main = "CMU";
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                f.mkdirs();
            }
        }

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        PersistentLogin persistentLogin = new PersistentLogin(pref, getApplicationContext());
//        if(persistentLogin.tryToLogin()) {
            Intent intent = new Intent(MainActivityWifi.this, UserOptionsActivityWifi.class);
            startActivity(intent);
//        }
    }

    public void logInActivity(View v) {
        Intent intent = new Intent(MainActivityWifi.this, LogInActivityWifi.class);
        startActivity(intent);
    }

    public void signUpActivity(View v) {
        Intent intent = new Intent(MainActivityWifi.this, SignUpActivityWifi.class);
        startActivity(intent);
    }

}
