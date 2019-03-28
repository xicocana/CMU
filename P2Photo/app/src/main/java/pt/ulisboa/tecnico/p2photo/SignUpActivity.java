package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SignUpActivity extends AppCompatActivity {

    public final static String PATTERN = "^[a-zA-Z0-9_.-]*$";
    public final static String NAME = "pt.ulisboa.tecnico.p2photo.NAME";
    public final static String PASSWORD = "pt.ulisboa.tecnico.p2photo.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
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

        //TODO ifs para verificar se o nome esta num formato correcto temos de definir quais os formatos dos IDs
        //checks basicos que vao ter de ser melhorados mas ja esta aqui a ideia
        if(name==null || name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must insert a valid name!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }


        else if(m.find()==false) {
            Toast.makeText(getApplicationContext(), "You are using invalid characters in your username", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            intent.putExtra(NAME, name);
            intent.putExtra(PASSWORD, password);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        else if(password==null || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must insert a password!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        else if(password.length()<4) {
            Toast.makeText(getApplicationContext(), "Password should at least have 4 characters!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        else if(repeatpwd==null || repeatpwd.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must repeat your password!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        else if(!password.equals(repeatpwd)) {
            Toast.makeText(getApplicationContext(), "Your password does not match.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            intent.putExtra(NAME, name);
            intent.putExtra(PASSWORD, password);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        else {
            //cria um intent para na proxima actividade que nao a UserOptionsActivity enviar ao servidor os dados
            Intent intent = new Intent(SignUpActivity.this, Loading.class);
            intent.putExtra(NAME, name);
            intent.putExtra(PASSWORD, password);
            startActivity(intent);
        }
    }

    //sera que o botao de cancelar deve acabar? ou simplesmente parar? repara que podemos cancelar
    //e fazer isto voltar atras
    public void cancelSignUp(View v) {
        SignUpActivity.this.finish();
    }
}
