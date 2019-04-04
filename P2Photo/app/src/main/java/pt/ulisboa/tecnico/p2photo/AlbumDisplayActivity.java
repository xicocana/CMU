package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.GoogleAddImageActivity;

public class AlbumDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        TextView title = findViewById(R.id.textView2);
        Intent intent = getIntent();
        String album_name = intent.getStringExtra("album_name");
        title.setText(album_name);

        Button findUsersBtn = findViewById(R.id.button6);
        findUsersBtn.setOnClickListener(v -> startActivity(new Intent(AlbumDisplayActivity.this, UserListActivity.class)));

        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {
            Intent intent1 = new Intent(AlbumDisplayActivity.this, GoogleAddImageActivity.class);
            intent1.putExtra("album_name",album_name);
            startActivity(intent1);
        });

    }
}
