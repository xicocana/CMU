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
    public final static String LOGIN = "LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }

    public void logIn(View v) throws IOException, CommunicationsException, JSONException, InterruptedException {

        //Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);

        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();

        //Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);


        SendDataToServerTask task = new SendDataToServerTask(name, password, LOGIN);
        task.execute();


        if(task.getStateOfRequest().equals("sucess")) {
            String message = task.getMessage();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            //get session key
            String myKey = "sessionkey";
            SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            // Set/Store data
            edit.putString("session_key", myKey);
            edit.putString("username", name);
            Log.i("SESSION", myKey);
            // Commit the changes
            edit.commit();

            Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);
            LogInActivity.this.finish();
            startActivity(intent);
        } else if(task.getStateOfRequest().equals("failure")) {
            String message = task.getMessage();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            for(int i = 1; i<10; i++) {
                try {
                    Thread.sleep(500);
                    if(task.getStateOfRequest().equals("sucess")) {
                        String message = task.getMessage();
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        //get session key
                        //TM DE SER O SERVER A MANDAR A CHAVE DE SESSAO
                        String myKey = "sessionkey";
                        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        // Set/Store data
                        edit.putString("session_key", myKey);
                        Log.i("SESSION", myKey);
                        edit.putString("username", name);
                        // Commit the changes
                        edit.commit();

                        Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);
                        LogInActivity.this.finish();
                        startActivity(intent);
                        break;
                    } else if(task.getStateOfRequest().equals("failure")) {
                        String message = task.getMessage();
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        break;
                    }
                } catch(InterruptedException ie) {
                    System.err.println("Could not properly put the thread to sleep...");
                    Toast.makeText(getApplicationContext(), "Critical error!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    LogInActivity.this.finish();
                    startActivity(intent);
                }
                Toast.makeText(getApplicationContext(), "Could not obtain an answer back from the server!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                LogInActivity.this.finish();
                startActivity(intent);
            }
        }
    }

    public void cancelLogIn(View v) {
        LogInActivity.this.finish();
    }
}
