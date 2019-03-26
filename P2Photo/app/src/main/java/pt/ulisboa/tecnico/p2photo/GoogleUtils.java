package pt.ulisboa.tecnico.p2photo;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;

public class GoogleUtils {

    private static final String TAG = "google_utils";

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_ADD_ALBUM = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 66;
    private static final int REQUEST_CODE_CREATOR = 77;
    private static final int REQUEST_GET_SINGLE_FILE = 88;
    private DataHolder dataHolder;
    private Bitmap mBitmapToSave;


    public GoogleUtils() {
        this.dataHolder = new DataHolder();
    }

    public void createFolder() {
        dataHolder.getmDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("New folder")
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(true)
                            .build();
                    return  dataHolder.getmDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener((Executor) this,
                        driveFolder -> {
                            Log.i(TAG, "Sucess to create file");

                        })
                .addOnFailureListener((Executor) this, e -> {
                    Log.e(TAG, "Unable to create file", e);

                });
    }


}
