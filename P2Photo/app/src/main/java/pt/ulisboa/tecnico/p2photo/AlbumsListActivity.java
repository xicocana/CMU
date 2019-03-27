package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class AlbumsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_list);



        ListView albums_list = (ListView) findViewById(R.id.albums_list);

        ArrayList<String> albums_from_server = new ArrayList<String>();
        albums_from_server.add("album 1");
        albums_from_server.add("album 2");
        albums_from_server.add("album 3");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albums_from_server);
        albums_list.setAdapter(arrayAdapter);

        albums_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AlbumsListActivity.this, AlbumDisplayActivity.class);
                intent.putExtra("album_name", albums_from_server.get(position));
                startActivity(intent);
            }
        });
    }
}
