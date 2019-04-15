package pt.ulisboa.tecnico.p2photo.GoogleUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.p2photo.AlbumDisplayActivity;
import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;
import pt.ulisboa.tecnico.p2photo.UserListActivity;

/**
 * Activity to illustrate how to retrieve and read file contents.
 */
public class GoogleImageDownloadActivity extends BaseGoogleActivity {
    private static final String TAG = "RetrieveContents";

    /**
     * Text view for file contents
     */
    ImageView imageView;

    private GridView imageGrid;
    private ArrayList<Bitmap> bitmapList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        this.bitmapList = new ArrayList<>();
        TextView title = findViewById(R.id.textView2);
        Intent intent = getIntent();
        String album_name = intent.getStringExtra("album_name");
        title.setText(album_name);

        Button findUsersBtn = findViewById(R.id.button6);
        findUsersBtn.setOnClickListener(v -> startActivity(new Intent(GoogleImageDownloadActivity.this, UserListActivity.class)));

        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {
            Intent intent1 = new Intent(GoogleImageDownloadActivity.this, GoogleAddImageActivity.class);
            intent1.putExtra("album_name",album_name);
            startActivity(intent1);
        });

        imageView = findViewById(R.id.image_view);


        this.imageGrid = findViewById(R.id.gridview);

        imageGrid.setAdapter(new ImageAdapter(this, bitmapList));
    }

    @Override
    protected void onDriveClientReady() {


    }

    @Override
    protected void onResume() {
        super.onResume();
        DataHolder dataHolder = DataHolder.getInstance();
        DriveId txtDriveId = DriveId.decodeFromString(dataHolder.getTxtDriveID());
        retrieveContents(txtDriveId.asDriveFile());
    }

    private void retrieveContents(DriveFile file) {

        Task<DriveContents> openFileTask =
                getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);

        List<String> listOfDriveIds = new ArrayList<>();

        openFileTask.continueWithTask(task -> {
            DriveContents contents = task.getResult();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(contents.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listOfDriveIds.add(line);
                }
                showMessage(getString(R.string.content_loaded));
            }


            Task<Void> discardTask = getDriveResourceClient().discardContents(contents);
            return discardTask;
        }).addOnSuccessListener(aVoid -> {

            for (String driveIdString : listOfDriveIds) {
                //getImage();
                if (!driveIdString.isEmpty()) {
                   DriveId driveId = DriveId.decodeFromString(driveIdString);
                    getImage(driveId.asDriveFile());
                }
            }

        }).addOnFailureListener(e -> {
            // Handle failure
            // ...
        });


    }

    private void getImage(DriveFile file) {
        file.open(getDriveResourceClient().asGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(

                        result -> {
                            if (!result.getStatus().isSuccess()) {
                                // Handle an error
                            }
                            DriveContents driveContents = result.getDriveContents();
                            InputStream is = driveContents.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            // Do something with the bitmap

                            // Don't forget to close the InputStream
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            bitmapList.add(bitmap);
                            imageGrid.invalidateViews();
                            //imageView.setImageBitmap(bitmap);

                        });
    }

}

