package pt.ulisboa.tecnico.p2photo.wifi;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pt.ulisboa.tecnico.p2photo.GridViewAdapter;
import pt.ulisboa.tecnico.p2photo.R;
import pt.ulisboa.tecnico.p2photo.cloud.AlbumDisplayActivity;

public class AlbumDisplayActivityWifi extends AppCompatActivity {
    public static final String TAG = "AlbumDisplayActivity";

    private GridView vista_imagens;
    private GridViewAdapter gridViewAdapter;
    private ArrayList<java.io.File> imagens = new ArrayList<>();
    private ArrayList<Bitmap> bitmapList = new ArrayList<>();
    ProgressDialog pDialog;
    String album_name;
    CircularProgressBar progressBar;
    private static final int READ_REQUEST_CODE = 42;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_display);

        //VERIFY INTERNET CONNECTION
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else {
            connected = false;
        }

        TextView title = findViewById(R.id.textView2);
        Intent intent = getIntent();
        album_name = intent.getStringExtra("album_name");

        String folder_main = "CMU-wifi-cache/"+album_name;
        java.io.File f = new java.io.File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                f.mkdirs();
            }
        }

        title.setText(album_name);

        Button findUsersBtn = findViewById(R.id.add_user);
        findUsersBtn.setOnClickListener(v -> startActivity(new Intent(AlbumDisplayActivityWifi.this, UserListActivityWifi.class).putExtra("album_name", album_name)));

        Button addImage = findViewById(R.id.add_image);
        addImage.setOnClickListener(v -> {
            // the file to be moved or copied
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/jpeg");
            startActivityForResult(i, READ_REQUEST_CODE);
        });

        vista_imagens = findViewById(R.id.gridview);
        gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, bitmapList);
        vista_imagens.setAdapter(gridViewAdapter);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);



        if(!connected){
            new AlbumDisplayActivityWifi.ImageShower2(this).execute();
        }else{
            new ImageShower(this).execute();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                new ImageCopy(this).execute(uri);
            }
        }
    }

    private class ImageShower extends AsyncTask<Void, Integer, Void> {

        private Context ctx;

        public ImageShower(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            vista_imagens.invalidateViews();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String ExternalStorageDirectoryPath = Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath();

                String targetPath = ExternalStorageDirectoryPath + "/CMU/" + album_name + "/";

                File targetDirector = new File(targetPath);

                File[] files = targetDirector.listFiles();
                for (File file : files) {
                    Uri uri = Uri.fromFile(file);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
                    bitmapList.add(bitmap);
                    try {
                        Random random = new Random();
                        int randomInt = random.nextInt(999) + 111;
                        String name = bitmap.getConfig().name() + randomInt;
                        java.io.File f = new java.io.File(Environment.getExternalStorageDirectory() + "/CMU-wifi-cache/" + album_name, name + ".jpg");
                        f.createNewFile();

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        //write the bytes in file
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ImageShower2 extends AsyncTask<Void, Integer, Void> {

        private Context ctx;

        public ImageShower2(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            vista_imagens.invalidateViews();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String ExternalStorageDirectoryPath = Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath();

                String targetPath = ExternalStorageDirectoryPath + "/CMU-wifi-cache/" + album_name + "/";

                File targetDirector = new File(targetPath);

                File[] files = targetDirector.listFiles();
                for (File file : files) {
                    Uri uri = Uri.fromFile(file);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
                    bitmapList.add(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;


        }
    }

    private class ImageCopy extends AsyncTask<Uri, Integer, Void> {

        private Context ctx;

        public ImageCopy(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uris[0]);
                //create a file to write bitmap data
                String name[] = uris[0].getPath().split("/");
                File f = new File(Environment.getExternalStorageDirectory() + "/CMU/" + album_name, name[name.length - 1] + ".jpg");
                f.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}










//    @Override
//    public boolean handleMessage(Message msg) {
//        switch (msg.what) {
//            case MESSAGE_READ:
//                byte[] readBuf = (byte[]) msg.obj;
//                // construct a string from the valid bytes in the buffer
//                String readMessage = new String(readBuf, 0, msg.arg1);
//                Log.d(TAG, readMessage);
//                (chatFragment).pushMessage("Buddy: " + readMessage);
//                break;
//
//            case MY_HANDLE:
//                Object obj = msg.obj;
//                (chatFragment).setChatManager((CommunicationManager) obj);
//
//        }
//        return true;
//    }





//EXEMPLO DE LOADING
//pDialog = ProgressDialog.show(this, "Loading Data", "Please Wait...", true);
//pDialog.dismiss();