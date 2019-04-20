package pt.ulisboa.tecnico.p2photo.GoogleUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;

import pt.ulisboa.tecnico.p2photo.CommunicationTask;
import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;
import pt.ulisboa.tecnico.p2photo.UserOptionsActivity;

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
        if (extras != null) {
            folder_name = extras.getString("foldername");
            createFolder();
        }
        Intent ret = new Intent(GoogleCreateFolderActivity.this, UserOptionsActivity.class);
        startActivity(ret);
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

                            createTxt(driveFolder);
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
                    finish();
                });
    }

    private void createTxt(DriveFolder parent) {
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

        createContentsTask.continueWithTask(task -> {
                    DriveContents contents = createContentsTask.getResult();

                    // Create the initial metadata - MIME type and title.
                    // Note that the user will be able to change the title later.
                    MetadataChangeSet changeSet =
                            new MetadataChangeSet.Builder()
                                    .setMimeType("text/plain")
                                    .setTitle(folder_name + "_Files_Id.txt ")
                                    .build();

                    return getDriveResourceClient().createFile(parent, changeSet, contents);

                }
        ).onSuccessTask(driveFile -> {
            DataHolder dataHolder = DataHolder.getInstance();
            dataHolder.setTxtDriveID(driveFile.getDriveId().encodeToString());


            SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String name = pref.getString("username", null);

            //CHAMAR FUNCAO DO SERVER PARA CRIAR FOLDER E TXT
            CommunicationTask task = new CommunicationTask("ADD-ALBUM");
            task.setName(name);
            task.setFolderId(dataHolder.getAlbum1DriveID());
            task.setFileId(dataHolder.getTxtDriveID());
            task.setAlbum(folder_name);
            task.execute();
            return null;
        });
    }
}