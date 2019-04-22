package pt.ulisboa.tecnico.p2photo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import pt.ulisboa.tecnico.p2photo.GoogleUtils.ImageAdapter;

import static com.google.android.gms.tasks.Tasks.await;

public class AlbumDisplayActivity extends AppCompatActivity{

    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int READ_REQUEST_CODE = 42;
    private Drive googleDriveService;
    private GridView vista_imagens;
    private ArrayList<java.io.File> imagens = new ArrayList<>();
    private static final String TAG = "AlbumDisplayActivity";
    public static Uri uri = null;
    private ArrayList<Bitmap> bitmapList = new ArrayList<>();
    ProgressDialog pDialog;
    String album_name;
    String album_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        TextView title = findViewById(R.id.textView2);
        Intent intent = getIntent();
        album_name = intent.getStringExtra("album_name");
        album_id = intent.getStringExtra("album_id");
        title.setText(album_name);

        requestSignIn();

        Button findUsersBtn = findViewById(R.id.button6);
        findUsersBtn.setOnClickListener(v -> startActivity(new Intent(AlbumDisplayActivity.this, UserListActivity.class).putExtra("album_name",album_name)));

        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {

            startActivityForResult( mDriveServiceHelper.createFilePickerIntent(), READ_REQUEST_CODE );

        });

        vista_imagens = findViewById(R.id.gridview);
        vista_imagens.setAdapter(new GridViewAdapter(this, R.layout.grid_item_layout, bitmapList));
        Log.i("lista", bitmapList.size()+"::" );
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;

            case  READ_REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK) {
                    //Uri uri = null;
                    if (resultData != null) {
                        uri = resultData.getData();
                        Log.i(TAG, "Uri: " + uri.toString());

                        File metadata = new File()
                                .setParents(Collections.singletonList(album_id))
                                .setMimeType("image/jpeg")
                                .setStarred(false)
                                .setName("teste.jpg");

                        AbstractInputStreamContent content = new AbstractInputStreamContent(null) {
                            @Override
                            public InputStream getInputStream() throws IOException {
                                return getContentResolver().openInputStream(uri);
                            }

                            @Override
                            public long getLength() throws IOException {
                                return getInputStream().available();
                            }

                            @Override
                            public boolean retrySupported() {
                                return false;
                            }
                        };

                        if(mDriveServiceHelper.uploadFile(metadata, content) != null){
                            String message = "Image added to album successfully";
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            try {
                                Bitmap bitmap2 = BitmapFactory.decodeStream( content.getInputStream());
                                bitmapList.add(bitmap2);
                                vista_imagens.invalidateViews();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            String message = "Error adding image to drive";
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                    //updateImages();
                    //pDialog = ProgressDialog.show( this, "Loading Data", "Please Wait...", true);
                    new DownloadFilesTask().execute();

                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    private class DownloadFilesTask extends AsyncTask<Object, Integer, Void> {

        @Override
        protected Void doInBackground(Object[] objects) {

            mDriveServiceHelper.queryAllFiles().onSuccessTask(fileList -> {

                fileList.getFiles();
                System.out.print(2345);
                return null;

            }).addOnFailureListener(e -> {
                e.printStackTrace();
            });

            mDriveServiceHelper.searchFolder(album_name).onSuccessTask(task -> {
                mDriveServiceHelper.searchFileInFolder(task.getId(), "image/jpeg").onSuccessTask(task1 -> {
                    int i=0;

                    if(task1.size() == 0 ){
                        //pDialog.dismiss();
                    }

                    for (GoogleDriveFileHolder f: task1) {
                        java.io.File file = new java.io.File(getFilesDir() + "/fileName"+i);
                        i++;
                        Log.i("lista", f.getId());
                        mDriveServiceHelper.downloadFile(file, f.getId()).onSuccessTask(command -> {
                            Bitmap bitmap2 = BitmapFactory.decodeFile(file.getPath());
                            bitmapList.add(bitmap2);
                            Log.i("lista", bitmapList.size() + ":::::::");
                            Log.i("lista", "antesinvalll");
                            vista_imagens.invalidateViews();
                            if(f.getId().equals(task1.get(task1.size()-1).getId())) {
                                //pDialog.dismiss();
                            }
                            return null;
                        });
                    }
                    return null;
                });
                return null;
            });

            return null;
        }
    }


}

//EXEMPLO DE LOADING
//pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
//pDialog.dismiss();