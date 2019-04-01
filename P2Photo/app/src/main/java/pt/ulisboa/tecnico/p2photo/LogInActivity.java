package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

    class myTask extends AsyncTask<Void, Void, Void> {

        private String name = "";
        private String pswd = "";
        private String command = "LOGIN";

        public myTask(String name, String pswd, String command) {
            this.name = name;
            this.pswd = pswd;
            this.command = command;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String hostname = "192.168.43.80";

                System.out.println("entra aqui");
                Socket socket = new Socket(hostname, 8080);
                System.out.println(socket.getInetAddress().getHostAddress());
                Communications communication = new Communications(socket);

                JSONObject obj = new JSONObject();
                obj.put("user-name", name);
                obj.put("password", pswd);

                String data = obj.toString();
                communication.sendInChunks(command);
                communication.sendInChunks(data);

                data = (String) communication.receiveInChunks();
                obj = new JSONObject(data);
                if (obj.get("conclusion").equals("OK")) {
                    //System.out.println(obj.get("message"));
                    Log.i("SIGNUP", "OK");
                } else if (obj.get("conclusion").equals("NOT-OK")) {
                    //System.out.println(obj.get("message"));
                    Log.i("SIGNUP", "NOT-OK");
                }

                communication.sendInChunks("EXIT");
                communication.end();


            } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch (CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            } catch (JSONException je) {
                je.printStackTrace();
                System.out.println("JsonException");
            }
            return null;
        }
    }
}
