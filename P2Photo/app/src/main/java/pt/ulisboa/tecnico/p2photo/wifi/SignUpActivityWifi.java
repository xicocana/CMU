package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import pt.ulisboa.tecnico.p2photo.CommunicationUtilities;
import pt.ulisboa.tecnico.p2photo.R;

public class SignUpActivityWifi extends AppCompatActivity {

    private final static String PATTERN = "^[a-zA-Z0-9_.-]*$";
    private final static String NAME = "pt.ulisboa.tecnico.p2photo.NAME";
    private final static String PASSWORD = "pt.ulisboa.tecnico.p2photo.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    public void register(View v) {
        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText3);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();

        EditText rptpwdView = findViewById(R.id.editText5);
        String repeatpwd = rptpwdView.getText().toString();

        EditText emailView= findViewById(R.id.email_edit_text);
        String email = emailView.getText().toString();

        signUp(name, email, password, repeatpwd);
    }

    public void cancelSignUp(View v) {
        SignUpActivityWifi.this.finish();
    }

    private void signUp(String name, String email, String password, String repeatpwd) {

        Pattern pattern = Pattern.compile(PATTERN);
        Matcher m = pattern.matcher(name);

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
        else if(email == null || email.isEmpty()){
            Toast.makeText(getApplicationContext(),"You must insert a email!",Toast.LENGTH_SHORT).show();
        }

        else {
            CommunicationUtilities communicationUtilities = new CommunicationUtilities(this.getApplicationContext());
            boolean state = communicationUtilities.sendSignUp(name, email, password);
            proceedAccordingToState(state);
        }

    }

    private void proceedAccordingToState(boolean state) {
        if(state) {
            Intent intent = new Intent(SignUpActivityWifi.this, MainActivityWifi.class);
            SignUpActivityWifi.this.finish();
            startActivity(intent);
        } else {}
    }

}

