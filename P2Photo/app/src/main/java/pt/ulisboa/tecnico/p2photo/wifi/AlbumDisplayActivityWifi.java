package pt.ulisboa.tecnico.p2photo.wifi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import java.util.ArrayList;
import pt.ulisboa.tecnico.p2photo.GridViewAdapter;
import pt.ulisboa.tecnico.p2photo.R;

public class AlbumDisplayActivityWifi extends AppCompatActivity {


    private GridView vista_imagens;
    private GridViewAdapter gridViewAdapter;
    private ArrayList<java.io.File> imagens = new ArrayList<>();
    private static final String TAG = "AlbumDisplayActivity";
    public static Uri uri = null;
    private ArrayList<Bitmap> bitmapList = new ArrayList<>();
    ProgressDialog pDialog;
    String album_name;
    String album_id;
    String text_txt;

    CircularProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        TextView title = findViewById(R.id.textView2);
        Intent intent = getIntent();
        album_name = intent.getStringExtra("album_name");
        album_id = intent.getStringExtra("album_id");
        text_txt = intent.getStringExtra("text_txt");

        title.setText(album_name);


        Button findUsersBtn = findViewById(R.id.add_user);
        findUsersBtn.setOnClickListener(v -> startActivity(new Intent(AlbumDisplayActivityWifi.this, UserListActivityWifi.class).putExtra("album_name", album_name)));

        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {

        });

        vista_imagens = findViewById(R.id.gridview);
        gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, bitmapList);
        vista_imagens.setAdapter(gridViewAdapter);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);

    }
}

//EXEMPLO DE LOADING
//pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
//pDialog.dismiss();