package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

public class LogInActivity extends AppCompatActivity {

    public final static String NAME = "pt.ulisboa.tecnico.p2photo.NAME";
    public final static String PASSWORD = "pt.ulisboa.tecnico.p2photo.PASSWORD";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }

    public void logIn(View v) throws IOException, CommunicationsException, JSONException, InterruptedException {
        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();

        ClientServerComms clientServerComms = new ClientServerComms(this.getApplicationContext());
        boolean state = clientServerComms.sendLogin(name, password);
        proceedAccordingToState(state);
    }

    public void cancelLogIn(View v) {
        LogInActivity.this.finish();
    }

    private void proceedAccordingToState(boolean state) {
        if(state) {
            Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);
            startActivity(intent);
        }
        else {}
    }
}
