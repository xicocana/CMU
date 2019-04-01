package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

public class SignUpActivity extends AppCompatActivity {

    private final static String PATTERN = "^[a-zA-Z0-9_.-]*$";
    private final static String NAME = "pt.ulisboa.tecnico.p2photo.NAME";
    private final static String PASSWORD = "pt.ulisboa.tecnico.p2photo.PASSWORD";
    private final static String SIGN_UP = "SIGN-UP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void register(View v) {
        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();

        EditText rptpwdView = findViewById(R.id.editText3);
        String repeatpwd = rptpwdView.getText().toString();

        Pattern pattern = Pattern.compile(PATTERN);

        // Now create matcher object.
        Matcher m = pattern.matcher(name);

        //checks basicos que vao ter de ser melhorados mas ja esta aqui a ideia
        if(name==null || name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must insert a valid name!", Toast.LENGTH_SHORT).show();
        }


        else if(m.find()==false) {
            Toast.makeText(getApplicationContext(), "You are using invalid characters in your username", Toast.LENGTH_SHORT).show();
        }

        else if(password==null || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must insert a password!", Toast.LENGTH_SHORT).show();
        }

        else if(password.length()<4) {
            Toast.makeText(getApplicationContext(), "Password should at least have 4 characters!", Toast.LENGTH_SHORT).show();
        }

        else if(repeatpwd==null || repeatpwd.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must repeat your password!", Toast.LENGTH_SHORT).show();
        }

        else if(!password.equals(repeatpwd)) {
            Toast.makeText(getApplicationContext(), "Your password does not match.", Toast.LENGTH_SHORT).show();
        }

        else {
            SendDataToServerTask task = new SendDataToServerTask(name, password, SIGN_UP);
            task.execute();

            if(task.getStateOfRequest().equals("sucess")) {
                String message = task.getMessage();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                SignUpActivity.this.finish();
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
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            SignUpActivity.this.finish();
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
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        SignUpActivity.this.finish();
                        startActivity(intent);
                    }
                    Toast.makeText(getApplicationContext(), "Could not obtain an answer back from the server!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    SignUpActivity.this.finish();
                    startActivity(intent);
                }
            }
        }
    }

    public void cancelSignUp(View v) {
        SignUpActivity.this.finish();
    }

}

