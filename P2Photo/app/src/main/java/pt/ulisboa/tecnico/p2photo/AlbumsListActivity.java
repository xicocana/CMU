package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.GoogleImageDownloadActivity;


public class AlbumsListActivity extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";
    private CommunicationTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String name = pref.getString("username", null);
        getUserAlbums(name);
    }

    private void getUserAlbums(String userName) {

        CommunicationUtilities communicationUtilities = new CommunicationUtilities(this.getApplicationContext());
        boolean state = communicationUtilities.sendGetAlbums(userName);

        if(state) {
            ArrayList<ArrayList<String>> albumsList = (ArrayList<ArrayList<String>>) communicationUtilities.getContent();

            ArrayList<String> albumsFromServer = new ArrayList<String>();
            for (ArrayList<String> iter: albumsList) {
                albumsFromServer.add(iter.get(0));
            }
            ListView albums_list = (ListView) findViewById(R.id.albums_list);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumsFromServer);
            albums_list.setAdapter(arrayAdapter);

            albums_list.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(AlbumsListActivity.this, AlbumDisplayActivity.class);

                intent.putExtra("album_name", albumsFromServer.get(position));
                intent.putExtra("album_id", albumsList.get(position).get(1));

                startActivity(intent);
            });
        }

        else {
            Intent intent = new Intent(AlbumsListActivity.this, UserOptionsActivity.class);
            AlbumsListActivity.this.finish();
            startActivity(intent);
        }
    }
}
