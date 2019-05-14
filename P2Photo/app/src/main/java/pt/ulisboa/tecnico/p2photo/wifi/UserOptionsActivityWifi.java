package pt.ulisboa.tecnico.p2photo.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.p2photo.R;

public class UserOptionsActivityWifi extends AppCompatActivity  {

    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String TAG = "UserOptionsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_options_wifi);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }

        Button addAlbumBtn = findViewById(R.id.add_album_btn);
        addAlbumBtn.setOnClickListener(v -> startActivity(new Intent(UserOptionsActivityWifi.this, CreateFolderActivityWifi.class)));

        Button listAlbumsBtn = findViewById(R.id.get_album_btn);
        listAlbumsBtn.setOnClickListener(v -> startActivity(new Intent(UserOptionsActivityWifi.this, AlbumsListActivityWifi.class)));

        Button logOutBtn = findViewById(R.id.log_out_btn);
        logOutBtn.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = pref.edit();
            // delete data
            edit.clear();
            // Commit the changes
            edit.apply();
            Intent intent = new Intent(UserOptionsActivityWifi.this, MainActivityWifi.class);
            UserOptionsActivityWifi.this.finish();
            startActivity(intent);
        });


        Button searchWIFIButton = findViewById(R.id.search_wifi);
        searchWIFIButton.setOnClickListener(v -> {
            //TODO WIFI
            /*intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            channel = manager.initialize(this, getMainLooper(), null);

            startRegistrationAndDiscovery();
            */
            Intent intent = new Intent(UserOptionsActivityWifi.this, SearchUsersActivityWifi.class);
            startActivity(intent);
        });

    }









}
