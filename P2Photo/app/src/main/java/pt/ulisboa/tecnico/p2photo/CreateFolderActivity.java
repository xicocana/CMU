package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.GoogleCreateFolderActivity;

public class CreateFolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_folder);

        Button create_btn = (Button) findViewById(R.id.button3);
        EditText folder_name = (EditText) findViewById(R.id.editText4);

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateFolderActivity.this, GoogleCreateFolderActivity.class);
                intent.putExtra("foldername", folder_name.getText());
                startActivity(intent);
            }
        });
    }
}
