package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
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

    public void logIn(View v) throws IOException, CommunicationsException, JSONException{

        //Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);

        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();



        //intent.putExtra(NAME, name);
        //intent.putExtra(PASSWORD, password);
        //startActivity(intent);

    }

    public void cancelLogIn(View v) {
        LogInActivity.this.finish();
    }
}
