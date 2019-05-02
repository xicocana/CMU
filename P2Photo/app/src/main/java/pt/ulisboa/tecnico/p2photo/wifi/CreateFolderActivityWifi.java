package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import java.io.File;

import pt.ulisboa.tecnico.p2photo.CommunicationTask;
import pt.ulisboa.tecnico.p2photo.R;

public class CreateFolderActivityWifi extends AppCompatActivity {

    private static final String TAG = "CreateFolderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_folder);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = pref.getString("username", null);

        Button create_btn = (Button) findViewById(R.id.button3);
        EditText folder_name = (EditText) findViewById(R.id.editText4);

        create_btn.setOnClickListener(v -> {
            Intent intent = new Intent(CreateFolderActivityWifi.this, UserOptionsActivityWifi.class);

            //create local folder
            String folder_main = folder_name.getText().toString();
            File folder = getFilesDir();
            File f= new File(folder, "testeeeeee");
            f.mkdir();
            //enviar nome do album ao server
            CommunicationTask task = new CommunicationTask("ADD-ALBUM");
            task.setFileId("localID");
            task.setFolderId("localID");
            task.setName(name);
            task.setAlbum(folder_main);
            task.execute();

            startActivity(intent);

        });
    }
}
