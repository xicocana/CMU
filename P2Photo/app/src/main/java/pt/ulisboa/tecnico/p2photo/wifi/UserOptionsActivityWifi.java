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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.p2photo.CommunicationTask;
import pt.ulisboa.tecnico.p2photo.R;

public class UserOptionsActivityWifi extends AppCompatActivity implements  Handler.Callback, WifiP2pManager.ConnectionInfoListener {

    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String TAG = "UserOptionsActivity";

    //TODO WIFI
    // TXT RECORD properties
    static final int SERVER_PORT = 4545;

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifip2photo";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;

    private WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler(this);

    private List<WiFiP2pService> WiFiP2pService = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_options);

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
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            channel = manager.initialize(this, getMainLooper(), null);

            startRegistrationAndDiscovery();

        });

        Button connectButton = findViewById(R.id.connect);
        connectButton.setOnClickListener(v -> {
            connectP2p(WiFiP2pService.get(0));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }

            });
        }
        super.onStop();
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Map<String, String> record = new HashMap<>();
            SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String name = pref.getString("username", null);
            record.put(TXTRECORD_PROP_AVAILABLE, "visible");
            record.put("USER", name);


            WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
            manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    appendStatus("Added Local Service");
                }

                @Override
                public void onFailure(int error) {
                    appendStatus("Failed to add a service");
                }
            });

            discoverService();
        }
    }

    private void discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.setDnsSdResponseListeners(channel, (instanceName, registrationType, srcDevice) -> {

                // A service has been discovered. Is this our app?
                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                    // update the UI and add the item the discovered
                    // device.

                    WiFiP2pService service = new WiFiP2pService();
                    service.device = srcDevice;
                    service.instanceName = instanceName;
                    service.serviceRegistrationType = registrationType;

                    Log.d(TAG, "onBonjourServiceAvailable " + instanceName);

                    Button connectButton = findViewById(R.id.connect);
                    connectButton.setVisibility(View.VISIBLE);


                   // connectP2p(service);
                }


            }, new WifiP2pManager.DnsSdTxtRecordListener() {

                /**
                 * A new TXT record is available. Pick up the advertised
                 * buddy name.
                 */
                @Override
                public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record,
                                                      WifiP2pDevice device) {
                    Log.d(TAG, device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                    appendStatus(record.get("USER"));
                }

            });

            // After attaching listeners, create a service request and initiate
            // discovery.
            serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
            manager.addServiceRequest(channel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            appendStatus("Added service discovery request");
                        }

                        @Override
                        public void onFailure(int arg0) {
                            appendStatus("Failed adding service discovery request");
                        }
                    });

            manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    appendStatus("Service discovery initiated");
                }

                @Override
                public void onFailure(int arg0) {
                    appendStatus("Service discovery failed");

                }
            });
        }
    }


    public void connectP2p(WiFiP2pService service) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                manager.removeServiceRequest(channel, serviceRequest,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(int arg0) {
                            }
                        });

                manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        appendStatus("Connecting to service");
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        appendStatus("Failed connecting to service");
                    }
                });
            }
    }

    private boolean isGroupOwner;

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread thread;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        isGroupOwner = p2pInfo.isGroupOwner;
        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                thread = new GroupOwnerSocketHandler(getHandler());
                thread.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Connected as peer");
            thread = new ClientSocketHandler(getHandler(),p2pInfo.groupOwnerAddress);
            thread.start();
        }

    }

    public void appendStatus(String status) {
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
    }

    private CommunicationManager communicationManager;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer



                if (isGroupOwner){
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, readMessage);
                    appendStatus("Mensage Recebida : " + msg);

                    communicationManager.write("ack");
                    Log.d("WIFI", "teste_album");
                }


                break;

            case MY_HANDLE:
                Object obj = msg.obj;
                communicationManager = (CommunicationManager) obj;

                if(!isGroupOwner){
                    SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String name = pref.getString("username", null);

                    Log.d("WIFI", "TESTE_NAME : " + name);

                    communicationManager.write("SEND_USER");
                    communicationManager.write(name);
                }


        }
        return true;
    }
}
