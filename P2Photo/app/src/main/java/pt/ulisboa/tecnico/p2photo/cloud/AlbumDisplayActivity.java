package pt.ulisboa.tecnico.p2photo.cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.p2photo.CommunicationTask;
import pt.ulisboa.tecnico.p2photo.GoogleDriveFileHolder;
import pt.ulisboa.tecnico.p2photo.GridViewAdapter;
import pt.ulisboa.tecnico.p2photo.R;
import pt.ulisboa.tecnico.p2photo.googleUtils;

import static com.google.android.gms.tasks.Tasks.await;

public class AlbumDisplayActivity extends googleUtils {

    private static final String TAG = "AlbumDisplayActivity";

    private GridView vista_imagens;
    private GridViewAdapter gridViewAdapter;


    public static Uri uri = null;
    private ArrayList<Bitmap> bitmapList = new ArrayList<>();
    String album_name;
    String album_id;
    String text_txt;

    CircularProgressBar progressBar;
    List<String> albumInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        Intent intent = getIntent();
        album_name = intent.getStringExtra("album_name");
        album_id = intent.getStringExtra("album_id");
        text_txt = intent.getStringExtra("text_txt");
        albumInfo = intent.getStringArrayListExtra("albumInfo");

        requestSignIn();

        TextView title = findViewById(R.id.textView2);
        title.setText(album_name);


        //ADD USER BUTTON
        Button findUsersBtn = findViewById(R.id.add_user);
        findUsersBtn.setOnClickListener(v ->
                startActivity(new Intent(AlbumDisplayActivity.this, UserListActivity.class)
                        .putExtra("album_name", album_name)));

        //ADD IMAGE BUTTON
        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {
            startActivityForResult(mDriveServiceHelper.createFilePickerIntent(), READ_REQUEST_CODE);
        });

        //GRID IMAGES
        vista_imagens = findViewById(R.id.gridview);
        gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, bitmapList);
        vista_imagens.setAdapter(gridViewAdapter);

        //PROGRESS BAR
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);

       // InitializeDownload();
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
                        new AlbumDisplayActivity.UploadFilesTask().execute(resultData.getData());
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void InitializeDownload() {
        //TODO- para teste

        for (int i = 2; i <= albumInfo.size(); i += 3) {
            String txt = albumInfo.get(i);
            mDriveServiceHelper.getfileFromURL("https://drive.google.com/uc?export=download&id=" + txt).addOnSuccessListener(Content -> {
                if (Content != null){
                    Log.i("lista", "Content" + Content);

                    if (!Content.isEmpty()) {
                        List<String> driveIdList = Arrays.asList(Content.split(","));
                       // new DownloadFilesTask().execute(driveIdList);

                        for (String driveIdFile : driveIdList) {
                            if (!driveIdFile.isEmpty()) {
                                Glide.with(this)
                                        .asBitmap()
                                        .load("https://drive.google.com/uc?export=download&id=" + driveIdFile)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                bitmapList.add(resource);
                                                vista_imagens.invalidateViews();
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                            }
                                        });
                            }
                        }

                    }

                }else{
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "0 photos", Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(e -> {
                Log.i("lista", "erro");
            });
        }
    }

    @Override
    void doSomethingAfterSignin() {
        InitializeDownload();
    }

    private class UploadFilesTask extends AsyncTask<Uri, Integer, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {
            uri = uris[0];
            Log.i(TAG, "Uri: " + uri.toString());

            mDriveServiceHelper.searchFolder(album_name).addOnSuccessListener(fileHolder -> {

                if (fileHolder.getId() != null) {
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

                    uploadFunction(metadata, content);
                } else {

                    SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String name = pref.getString("username", null);

                    mDriveServiceHelper.createFolder(album_name, null).addOnSuccessListener(
                            folderHolder -> {
                                Toast.makeText(getApplicationContext(), "Album " + album_name + " created", Toast.LENGTH_SHORT).show();
                                mDriveServiceHelper.createTextFile(album_name, "", folderHolder.getId()).addOnSuccessListener(
                                        txtHolder -> {

                                            Toast.makeText(getApplicationContext(), "TXT  created", Toast.LENGTH_SHORT).show();
                                            //TODO - SERVER
                                            CommunicationTask task = new CommunicationTask("ADD-ALBUM");
                                            task.setFolderId(folderHolder.getId());
                                            task.setFileId(txtHolder.getId());
                                            task.setName(name);
                                            task.setAlbum(album_name);
                                            task.execute();
                                            //

                                            album_id = folderHolder.getId();
                                            text_txt = txtHolder.getId();

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

                                            uploadFunction(metadata, content);

                                        });

                                mDriveServiceHelper.setPermission(folderHolder.getId());

                            });
                }


            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "Error reading folder album in drive", Toast.LENGTH_SHORT).show();
            });


            return null;
        }

        private void uploadFunction(File metadata, AbstractInputStreamContent content) {
            mDriveServiceHelper.uploadFile(metadata, content).addOnSuccessListener(googleDriveFileHolder -> {
                String message = "Image added to album successfully";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                try {
                    Bitmap bitmap2 = BitmapFactory.decodeStream(content.getInputStream());
                    bitmapList.add(bitmap2);
                    vista_imagens.invalidateViews();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                readAndUpdateFile(googleDriveFileHolder);


            }).addOnFailureListener(e -> {
                String message = "Error adding image to drive";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            });
        }

        private void readAndUpdateFile(GoogleDriveFileHolder googleDriveFileHolder) {
            mDriveServiceHelper.readFile(text_txt).addOnSuccessListener(stringStringPair -> {
                Log.i("lista", "sucesso ler txt");
                Log.i("lista", "Content" + stringStringPair.second);

                StringBuilder sb = new StringBuilder();
                sb.append(",");
                sb.append(googleDriveFileHolder.getId());
                //TODO - no txt
                mDriveServiceHelper.updateFile(text_txt, sb.toString(), stringStringPair.second, getFilesDir() + album_name, album_name).addOnSuccessListener(file -> {
                    Log.i("lista", "sucesso txt updated");
                }).addOnFailureListener(e -> {
                    Log.e("lista", "insucesso txt updated", e);
                });

            }).addOnFailureListener(e -> {
                Log.e("lista", "insucesso ler txt", e);
            });
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

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                options.inSampleSize = 3;
                                BitmapFactory.decodeResource(getResources(),bitmap.getGenerationId(), options);

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
