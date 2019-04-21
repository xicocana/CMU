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

    private ArrayList<String> albums_from_server = new ArrayList<>();
    private SendDataToServerTask task;
    private ListView albums_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String name = pref.getString("username", null);


        /*task = new SendDataToServerTask(name, "GET-ALBUMS");
        try {
            task.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //List<String> albunsName = new ArrayList<>();

        /*task.getJSONuserAlbums();

        for (JSONArray jsonArray: task.getJSONuserAlbums()
              ) {
            try {
                albunsName.add((String)jsonArray.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/



        /*if(task.getStateOfRequest().equals("sucess")) {
            albums_from_server = (ArrayList<String>) albunsName;
        }*/

        albums_from_server.add("rest");

        albums_list = (ListView) findViewById(R.id.albums_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums_from_server);
        albums_list.setAdapter(arrayAdapter);

        albums_list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AlbumsListActivity.this, AlbumDisplayActivity.class);

            /*DataHolder dataHolder = DataHolder.getInstance();
            try {
                dataHolder.setTxtDriveID((String) task.getJSONuserAlbums().get(position).get(1));
                dataHolder.setTxtDriveID((String) task.getJSONuserAlbums().get(position).get(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            intent.putExtra("album_name", albums_from_server.get(position));
            startActivity(intent);

        });


    }


}
