package pt.ulisboa.tecnico.p2photo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static com.google.android.gms.tasks.Tasks.await;

public class AlbumDisplayActivity extends AppCompatActivity {

    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int READ_REQUEST_CODE = 42;
    private Drive googleDriveService;
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

        requestSignIn();

        Button findUsersBtn = findViewById(R.id.button6);
        findUsersBtn.setOnClickListener(v -> startActivity(new Intent(AlbumDisplayActivity.this, UserListActivity.class).putExtra("album_name", album_name)));

        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {
            startActivityForResult(mDriveServiceHelper.createFilePickerIntent(), READ_REQUEST_CODE);

        });

        vista_imagens = findViewById(R.id.gridview);
        gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, bitmapList);
        vista_imagens.setAdapter(gridViewAdapter);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;

            case READ_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    //Uri uri = null;
                    if (resultData != null) {
                        new UploadFilesTask().execute(resultData.getData());
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

                    //TODO- para teste
                    //List<String> driveIdList = Arrays.asList("1BfEkcK_ZHCP7Abj-3YbGkm_lW4_RInGo","1BfEkcK_ZHCP7Abj-3YbGkm_lW4_RInGo","1BfEkcK_ZHCP7Abj-3YbGkm_lW4_RInGo");
                    mDriveServiceHelper.readFile(text_txt).addOnSuccessListener(stringStringPair -> {
                        Log.i("lista", "Content" + stringStringPair.second);

                        if (!stringStringPair.second.isEmpty()) {
                            List<String> driveIdList = Arrays.asList(stringStringPair.second.split(","));
                            new DownloadFilesTask().execute(driveIdList);
                        }

                    }).addOnFailureListener(e -> {

                    });


                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    private class UploadFilesTask extends AsyncTask<Uri, Integer, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {
            uri = uris[0];
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

            mDriveServiceHelper.uploadFile(metadata, content).addOnSuccessListener(googleDriveFileHolder -> {
                String message = "Image added to album successfully";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                mDriveServiceHelper.readFile(text_txt).addOnSuccessListener(stringStringPair -> {
                    Log.i("lista", "Content" + stringStringPair.second);
                    StringBuilder sb = new StringBuilder();
                    sb.append(",");
                    sb.append(googleDriveFileHolder.getId());
                    //TODO - no txt
                    mDriveServiceHelper.updateFile(text_txt, sb.toString(), getFilesDir() + album_name, album_name).addOnSuccessListener(file -> {
                        Log.i("lista", "sucesso txt updated");
                    }).addOnFailureListener(e -> {
                        Log.e("lista", "insucesso txt updated", e);
                    });

                }).addOnFailureListener(e -> {

                });


                try {
                    Bitmap bitmap2 = BitmapFactory.decodeStream(content.getInputStream());
                    bitmapList.add(bitmap2);
                    vista_imagens.invalidateViews();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).addOnFailureListener(e -> {
                String message = "Error adding image to drive";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            });

            return null;
        }
    }

    private class DownloadFilesTask extends AsyncTask<List<String>, Integer, Void> {


        @Override
        protected Void doInBackground(List<String>... lists) {

            for (String driveIdFile : lists[0]) {
                if (!driveIdFile.isEmpty()) {
                    Log.i("lista", "antes do down");
                    mDriveServiceHelper.getBitmapFromURL("https://drive.google.com/uc?export=download&id=" + driveIdFile)
                            .addOnSuccessListener(bitmap -> {
                                Log.i("lista", "sucesso Download");
                                bitmapList.add(bitmap);
                                vista_imagens.invalidateViews();
                                progressBar.setIndeterminate(false);
                                progressBar.setVisibility(View.INVISIBLE);

                            }).addOnFailureListener(e -> Log.e("lista", "insucesso Download", e));
                }
            }

            return null;
        }
    }


}

//EXEMPLO DE LOADING
//pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
//pDialog.dismiss();