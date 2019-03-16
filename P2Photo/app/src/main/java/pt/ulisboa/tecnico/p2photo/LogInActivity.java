package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LogInActivity extends AppCompatActivity {

    public final static String NAME = "pt.ulisboa.tecnico.p2photo.NAME";
    public final static String PASSWORD = "pt.ulisboa.tecnico.p2photo.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }

    public void logIn(View v) {
        //cria um intent para na proxima actividade que nao a UserOptionsActivity enviar ao servidor os dados
        Intent intent = new Intent(LogInActivity.this, UserOptionsActivity.class);

        //Vai buscar os nomes e a password
        EditText nameView = findViewById(R.id.editText);
        String name = nameView.getText().toString();

        EditText pwdView = findViewById(R.id.editText2);
        String password = pwdView.getText().toString();
        //TODO ifs para verificar se o nome esta num formato correcto temos de definir quais os formatos dos IDs

        intent.putExtra(NAME, name);
        intent.putExtra(PASSWORD, password);
        startActivity(intent);
    }

    public void cancelLogIn(View v) {
        LogInActivity.this.finish();
    }
}
