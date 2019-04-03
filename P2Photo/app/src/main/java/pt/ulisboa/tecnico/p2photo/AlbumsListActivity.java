package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import pt.ulisboa.tecnico.p2photo.teste.downloadTest;

public class AlbumsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);

        ListView albums_list = (ListView) findViewById(R.id.albums_list);
        ArrayList<String> albums_from_server = new ArrayList<String>();

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = pref.getString("username", null);

        SendDataToServerTask task = new SendDataToServerTask(name, "GET-USERS");
        task.execute();

        if(task.getStateOfRequest().equals("sucess")) {
            String message = task.getMessage();

            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(message);
                int length = jsonArray.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        albums_from_server.add(jsonArray.getString(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums_from_server);
        albums_list.setAdapter(arrayAdapter);

        albums_list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AlbumsListActivity.this, AlbumDisplayActivity.class);
            intent.putExtra("album_name", albums_from_server.get(position));
            startActivity(intent);
        });


    }
}
