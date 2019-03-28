package pt.ulisboa.tecnico.p2photo.GoogleUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;

/**
 * An activity to illustrate how to create a new folder.
 */
public class GoogleCreateFolderActivity extends BaseGoogleActivity {
    private static final String TAG = "GoogleCreateFolder";
    String folder_name;

    @Override
    protected void onDriveClientReady() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null ){
            folder_name = extras.getString("foldername");
            createFolder();
        }

    }

    private void createFolder() {
        getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(folder_name)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(true)
                            .build();
                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(this,
                        driveFolder -> {
                            DataHolder dataHolder = DataHolder.getInstance();
                            dataHolder.setAlbum1DriveID(driveFolder.getDriveId().encodeToString());
                            showMessage(getString(R.string.file_created,
                                    driveFolder.getDriveId().encodeToString()));
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }



}