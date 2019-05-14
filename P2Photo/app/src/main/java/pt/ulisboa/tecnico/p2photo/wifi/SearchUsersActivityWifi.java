package pt.ulisboa.tecnico.p2photo.wifi;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.p2photo.DataHolder;
import pt.ulisboa.tecnico.p2photo.R;

public class SearchUsersActivityWifi extends AppCompatActivity implements Handler.Callback, WifiP2pManager.ConnectionInfoListener {


    public static final String TAG = "UserListActivityWifi";
    private String name = "";

    private List<String> usersList = new ArrayList<>();
    private List<WiFiP2pService> usersListWiFiP2pService = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private ListView usersViewList;

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

    private WifiP2pInfo wifiP2pInfo;
    private boolean isGroupOwner;

    private CommunicationManager communicationManager;

    private Handler handler = new Handler(this);

    public Handler getHandler() {
        return handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users_wifi);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        name = pref.getString("username", null);

        getWifiUsers();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        startRegistrationAndDiscovery();
        ListView userList = findViewById(R.id.user_list);
        Button connectButton = findViewById(R.id.connect_btn);
        connectButton.setOnClickListener(v -> {
            connectP2p(usersListWiFiP2pService.get(userList.getCheckedItemPosition()));
        });
    }

    private void getWifiUsers() {
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, usersList);
        usersViewList = findViewById(R.id.user_list);
        usersViewList.setAdapter(arrayAdapter);
    }

    @Override
    protected void onRestart() {
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
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

    private void startRegistrationAndDiscovery2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    HashMap<String, String> record = new HashMap<>();
                    record.put("name", "Amos");
                    WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
                    manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            appendStatus("Added Local Service");
                            manager.setDnsSdResponseListeners(channel, (instanceName, registrationType, srcDevice) -> {
                                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                                    // update the UI and add the item the discovered
                                    // device.
                                    WiFiP2pService service = new WiFiP2pService();
                                    service.device = srcDevice;
                                    service.instanceName = instanceName;
                                    service.serviceRegistrationType = registrationType;

                                    arrayAdapter.add(service.device.deviceName + " - " + service.instanceName);
                                    arrayAdapter.notifyDataSetChanged();
                                    usersListWiFiP2pService.add(service);
                                }


                            }, (fullDomainName, txtRecordMap, srcDevice) -> {

                                appendStatus("User : " + record.get("USER") + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                                //Log.d(TAG, srcDevice.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                            });
                            manager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    manager.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
                                        @Override
                                        public void onSuccess() {
                                            appendStatus("Added service discovery request");
                                            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                                                @Override
                                                public void onSuccess() {
                                                    manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                                                        @Override
                                                        public void onSuccess() {
                                                            // this is my recursive discovery approach
                                                            appendStatus("Service discovery initiated");
                                                        }

                                                        @Override
                                                        public void onFailure(int code) {
                                                            appendStatus("Service discovery failed");
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(int code) {
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(int code) {
                                            appendStatus("Failed adding service discovery request");
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(int code) {
                                }
                            });
                        }

                        @Override
                        public void onFailure(int code) {
                            appendStatus("Failed to add a service");
                        }
                    });
                }

                @Override
                public void onFailure(int code) {
                }
            });
        }
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Map<String, String> record = new HashMap<String, String>();
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

                    arrayAdapter.add(service.device.deviceName + " - " + service.instanceName);
                    arrayAdapter.notifyDataSetChanged();
                    usersViewList.invalidateViews();


                    usersListWiFiP2pService.add(service);
                }


            }, new WifiP2pManager.DnsSdTxtRecordListener() {

                /**
                 * A new TXT record is available. Pick up the advertised
                 * buddy name.
                 */
                @Override
                public void onDnsSdTxtRecordAvailable(
                        String fullDomainName, Map<String, String> record,
                        WifiP2pDevice device) {
                    appendStatus("User : " + record.get("USER") + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                    Log.d(TAG, device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
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

                config.groupOwnerIntent = 15;
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
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread thread;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        isGroupOwner = p2pInfo.isGroupOwner;
        this.wifiP2pInfo = p2pInfo;

        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                thread = new GroupOwnerSocketHandler(getHandler(), name);
                thread.start();
            } catch (IOException e) {
                Log.d(TAG, "Failed to create a server thread - " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Connected as peer");
            thread = new ClientSocketHandler(getHandler(), p2pInfo.groupOwnerAddress, name);
            thread.start();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);
                Log.d(TAG, readMessage);
                appendStatus("MENSAGEM: " + readMessage);
                Runnable task = () -> {
                    communicationManager.write("SEND_PHOTOS");
                    communicationManager.write("PHOTOS");
                };
                task.run();
                break;

            case MY_HANDLE:
                Object obj = msg.obj;
                communicationManager = (CommunicationManager) obj;
        }
        return true;
    }


    public void appendStatus(String status) {
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
    }

}
