package pt.ulisboa.tecnico.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.GoogleAddImageActivity;
import pt.ulisboa.tecnico.p2photo.GoogleUtils.GoogleCreateFolderActivity;

public class UserOptionsActivity extends AppCompatActivity {
    private static final String TAG = "UserOptionsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_options);

        Button addAlbumBtn = (Button) findViewById(R.id.add_album_btn);
        addAlbumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserOptionsActivity.this, CreateFolderActivity.class));

            }
        });

        Button listAlbumsBtn = (Button) findViewById(R.id.button7);
        listAlbumsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserOptionsActivity.this, AlbumsListActivity.class));
            }
        });

        Button logOutBtn = (Button) findViewById(R.id.log_out_btn);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                // delete data
                edit.clear();
                // Commit the changes
                edit.commit();
                Intent intent = new Intent(UserOptionsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
