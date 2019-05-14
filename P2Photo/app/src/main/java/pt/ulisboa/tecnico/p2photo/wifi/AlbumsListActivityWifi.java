package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.p2photo.CommunicationTask;
import pt.ulisboa.tecnico.p2photo.CommunicationUtilities;
import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;


public class AlbumsListActivityWifi extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";
    private CommunicationTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);

        SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String name = pref.getString("username", null);
        //getUserAlbums(name);
        getLocalAlbums();
    }

    private void getLocalAlbums(){
        List<String> albums = new ArrayList<>();
        File main_dir = Environment.getExternalStorageDirectory();
        File listFile[] = main_dir.listFiles();
        for(int i=0; i<listFile.length; i++){
            if(listFile[i].isDirectory() && listFile[i].getAbsolutePath().equals(Environment.getExternalStorageDirectory()+"/CMU")){
                File listFile2[] = listFile[i].listFiles();
                for (int j=0; j<listFile2.length; j++){
                    albums.add(listFile2[j].getName());
                }
                break;
            }
        }

        ListView albums_list = (ListView) findViewById(R.id.albums_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums);
        albums_list.setAdapter(arrayAdapter);

        albums_list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AlbumsListActivityWifi.this, AlbumDisplayActivityWifi.class);

            intent.putExtra("album_name", albums.get(position));
            //intent.putExtra("album_id", albumsList.get(position).get(1));
            //intent.putExtra("text_txt", albumsList.get(position).get(2));

            startActivity(intent);

        });
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
                Intent intent = new Intent(AlbumsListActivityWifi.this, AlbumDisplayActivityWifi.class);

                intent.putExtra("album_name", albumsFromServer.get(position));
                intent.putExtra("album_id", albumsList.get(position).get(1));
                intent.putExtra("text_txt", albumsList.get(position).get(2));

                startActivity(intent);
            });
        }

        else {
            Intent intent = new Intent(AlbumsListActivityWifi.this, UserOptionsActivityWifi.class);
            AlbumsListActivityWifi.this.finish();
            startActivity(intent);
        }
    }
}
