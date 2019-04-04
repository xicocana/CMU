package pt.ulisboa.tecnico.p2photo.GoogleUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;

/**
 * Activity to illustrate how to retrieve and read file contents.
 */
public class GoogleImageDownloadActivity extends BaseGoogleActivity {
    private static final String TAG = "RetrieveContents";

    /**
     * Text view for file contents
     */
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_image_download);
        imageView = (ImageView) findViewById(R.id.image_view);
    }

    @Override
    protected void onDriveClientReady() {
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
                if (!driveIdString.isEmpty()){

                }
            }

            DriveId driveId = DriveId.decodeFromString(listOfDriveIds.get(1));
            getImage(driveId.asDriveFile());

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


                            imageView.setImageBitmap(bitmap);

                        });
    }

}