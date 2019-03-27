package pt.ulisboa.tecnico.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AlbumDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        TextView title = (TextView) findViewById(R.id.textView2);
        Intent intent = getIntent();
        String album_name = intent.getStringExtra("album_name");
        title.setText(album_name);

        Button findUsersBtn = (Button) findViewById(R.id.button6);
        findUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AlbumDisplayActivity.this, UserListActivity.class));
            }
        });

    }
}
