package pt.ulisboa.tecnico.p2photo.cloud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.io.File;

import pt.ulisboa.tecnico.p2photo.R;

public class UserOptionsActivity extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String TAG = "UserOptionsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_options);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }

        Button addAlbumBtn = (Button) findViewById(R.id.add_album_btn);
        addAlbumBtn.setOnClickListener(v -> startActivity(new Intent(UserOptionsActivity.this, CreateFolderActivity.class)));

        Button listAlbumsBtn = (Button) findViewById(R.id.get_album_btn);
        listAlbumsBtn.setOnClickListener(v -> startActivity(new Intent(UserOptionsActivity.this, AlbumsListActivity.class)));

        Button logOutBtn = (Button) findViewById(R.id.log_out_btn);
        logOutBtn.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            // delete data
            edit.clear();
            // Commit the changes
            edit.commit();
            Intent intent = new Intent(UserOptionsActivity.this, MainActivity.class);
            UserOptionsActivity.this.finish();
            startActivity(intent);
        });

    }
}
