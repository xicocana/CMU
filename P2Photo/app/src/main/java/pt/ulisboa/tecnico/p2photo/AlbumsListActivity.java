package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.GoogleImageDownloadActivity;


public class AlbumsListActivity extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";
    private SendDataToServerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String name = pref.getString("username", null);
        getUserAlbums(name);
    }

    private void getUserAlbums(String userName) {

        ClientServerComms clientServerComms = new ClientServerComms(this.getApplicationContext());
        clientServerComms.sendGetAlbums(userName);
        ArrayList<ArrayList<String>> albumsList = (ArrayList<ArrayList<String>>) clientServerComms.getContent();

        ArrayList<String> albumsFromServer = new ArrayList<String>();
        for (ArrayList<String> iter: albumsList) {
            albumsFromServer.add(iter.get(0));
        }
        ListView albums_list = (ListView) findViewById(R.id.albums_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumsFromServer);
        albums_list.setAdapter(arrayAdapter);

        albums_list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AlbumsListActivity.this, GoogleImageDownloadActivity.class);

            DataHolder dataHolder = DataHolder.getInstance();
            dataHolder.setTxtDriveID((String) albumsList.get(position).get(1));
            dataHolder.setTxtDriveID((String) albumsList.get(position).get(2));

            intent.putExtra("album_name", albumsFromServer.get(position));
            startActivity(intent);
        });
    }
}
