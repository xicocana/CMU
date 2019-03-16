package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

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

        //TODO ifs para verificar se o nome esta num formato correcto temos de definir quais os formatos dos IDs

        //checks basicos que vao ter de ser melhorados mas ja esta aqui a ideia
        if(name==null || name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must insert a name!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        if(password==null || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must insert a password!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        if(repeatpwd==null || repeatpwd.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must repeat your password!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            SignUpActivity.this.finish();
            startActivity(intent);
        }

        if(!password.equals(repeatpwd)) {
            //isto tem de ser melhorado. Para uma coisa que nao acaba a actividade e volta a comeca-la
            //tem de ser algo que detecte imediatamente que a password esta ma e poe uma daquelas advertencias
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
