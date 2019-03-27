package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.CreateFolderActivity;

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


        Button findUsersBtn = (Button) findViewById(R.id.find_user_btn);
        findUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserOptionsActivity.this, UserListActivity.class));
            }
        });
    }

}
