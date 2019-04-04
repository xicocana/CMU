package pt.ulisboa.tecnico.p2photo.GoogleUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;

public class GoogleAddImageActivity extends BaseGoogleActivity {
    private static final String TAG = "GoogleAddImageActivity";

    private static final int REQUEST_GET_SINGLE_FILE = 5;
    private Bitmap mBitmapToSave;
    private String albumName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_add_image);
        albumName = getIntent().getStringExtra("album_name");
    }

    @Override
    protected void onDriveClientReady() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_GET_SINGLE_FILE:
                Log.i(TAG, "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image captured successfully.");
                    Uri imageUri = data.getData();

                    try {
                        mBitmapToSave = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    saveFileToDrive();
                }
                break;

        }
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        new ImportTask(mBitmapToSave).execute();
        finish();
    }


    class ImportTask extends AsyncTask<Void, Void, Void> {
        private Bitmap image = mBitmapToSave;

        public ImportTask(Bitmap image) {
            this.image = image;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            saveFileToDrive();
            return null;
        }

        private void saveFileToDrive() {
            // [START drive_android_create_file]
            final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

            createContentsTask.continueWithTask(createContentsTaskk -> {
                DriveId driveId = DriveId.decodeFromString(DataHolder.getInstance().getAlbum1DriveID());
                DriveFolder parent = driveId.asDriveFolder();
                DriveContents contents = createContentsTask.getResult();

                OutputStream outputStream = contents.getOutputStream();
                // Write the bitmap data from it.
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);

                try {
                    outputStream.write(bitmapStream.toByteArray());
                } catch (IOException e) {
                    Log.w(TAG, "Unable to write file contents.", e);
                }

                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet changeSet =
                        new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg")
                                .setTitle("Android Photo.png")
                                .build();


                return getDriveResourceClient().createFile(parent, changeSet, contents);
            }).addOnSuccessListener(driveFile -> {

                DataHolder dataHolder = DataHolder.getInstance();
                dataHolder.setFileDriveID(driveFile.getDriveId().encodeToString());
                Log.i(TAG, "DriveID da Imagem: " + driveFile.getDriveId().encodeToString());

                DriveId txtDriveId = DriveId.decodeFromString(dataHolder.getTxtDriveID());
                appendContents(txtDriveId.asDriveFile(),driveFile.getDriveId().encodeToString());

            });
        }

        private void appendContents(DriveFile file, String driveIdToWrite) {
            // [START drive_android_open_for_write]
            Task<DriveContents> openTask =
                    getDriveResourceClient().openFile(file, DriveFile.MODE_READ_WRITE);
            // [END drive_android_open_for_write]
            // [START drive_android_rewrite_contents]
            openTask.continueWithTask(task -> {
                DriveContents driveContents = task.getResult();
                ParcelFileDescriptor pfd = driveContents.getParcelFileDescriptor();
                long bytesToSkip = pfd.getStatSize();
                try (InputStream in = new FileInputStream(pfd.getFileDescriptor())) {
                    // Skip to end of file
                    while (bytesToSkip > 0) {
                        long skipped = in.skip(bytesToSkip);
                        bytesToSkip -= skipped;
                    }
                }
                try (OutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                    String stringToWrite = "\n"+driveIdToWrite;
                    out.write(stringToWrite.getBytes());
                }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setStarred(true)
                        .setLastViewedByMeDate(new Date())
                        .build();
                Task<Void> commitTask =
                        getDriveResourceClient().commitContents(driveContents, changeSet);
                return commitTask;

            })
                    .addOnSuccessListener(aVoid -> {
                        showMessage(getString(R.string.content_updated));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Unable to update contents", e);
                        showMessage(getString(R.string.content_update_failed));
                        finish();
                    });

        }
    }

}
