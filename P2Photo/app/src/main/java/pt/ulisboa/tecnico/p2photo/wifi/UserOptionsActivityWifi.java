package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import pt.ulisboa.tecnico.p2photo.R;

public class UserOptionsActivityWifi extends AppCompatActivity {

    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String TAG = "UserOptionsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_options);

        Button addAlbumBtn = (Button) findViewById(R.id.add_album_btn);
        addAlbumBtn.setOnClickListener(v -> startActivity(new Intent(UserOptionsActivityWifi.this, CreateFolderActivityWifi.class)));

        Button listAlbumsBtn = (Button) findViewById(R.id.get_album_btn);
        listAlbumsBtn.setOnClickListener(v -> startActivity(new Intent(UserOptionsActivityWifi.this, AlbumsListActivityWifi.class)));

        Button logOutBtn = (Button) findViewById(R.id.log_out_btn);
        logOutBtn.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            // delete data
            edit.clear();
            // Commit the changes
            edit.commit();
            Intent intent = new Intent(UserOptionsActivityWifi.this, MainActivityWifi.class);
            UserOptionsActivityWifi.this.finish();
            startActivity(intent);
        });

    }
}
