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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.p2photo.R;

public class SearchUsersActivityWifi extends AppCompatActivity implements Handler.Callback, WifiP2pManager.ConnectionInfoListener {


    public static final String TAG = "UserListActivityWifi";
    private String name = "";

    private List<String> usersList = new ArrayList<>();
    private List<WiFiP2pService> usersListWiFiP2pService = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

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
    private CommunicationManager communicationManager;
    private boolean isGroupOwner;

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
        arrayAdapter.clear();
        startRegistrationAndDiscovery();
        ListView userList = findViewById(R.id.user_list);
        Button connectButton = findViewById(R.id.connect_btn);
        connectButton.setOnClickListener(v -> {
            connectP2p(usersListWiFiP2pService.get(userList.getCheckedItemPosition()));
        });
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
            manager.setDnsSdResponseListeners(channel, new WifiP2pManager.DnsSdServiceResponseListener() {

                @Override
                public void onDnsSdServiceAvailable(String instanceName,
                                                    String registrationType, WifiP2pDevice srcDevice) {

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


                        usersListWiFiP2pService.add(service);
                    }


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

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                appendStatus("MENSAGEM: " + readMessage);

                if (!isGroupOwner){
                    // Allow user to pick an image from Gallery or other
                    // registered apps
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                }else{
                    new FileServerAsyncTask(this)
                            .execute();
                }

                break;

            case MY_HANDLE:
                Object obj = msg.obj;
                communicationManager = (CommunicationManager) obj;
                communicationManager.write("TESTE");

        }
        return true;
    }

    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        Log.d(TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(this, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, wifiP2pInfo.groupOwnerAddress);
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        this.startService(serviceIntent);
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

    private WifiP2pInfo wifiP2pInfo;
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
                thread = new GroupOwnerSocketHandler(getHandler(),name);
                thread.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Connected as peer");

            thread = new ClientSocketHandler(getHandler(), p2pInfo.groupOwnerAddress, name);
            thread.start();

        }

    }

    public void appendStatus(String status) {
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
    }

    private void getWifiUsers() {
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, usersList);
        final ListView usersViewList = (ListView) findViewById(R.id.user_list);
        usersViewList.setAdapter(arrayAdapter);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;

        /**
         * @param context
         */
        public FileServerAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server: connection done");
                final File f = new File(context.getExternalFilesDir("received"),
                        "wifip2pshared-" + System.currentTimeMillis()
                                + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                SearchUsersActivityWifi searchUsersActivityWifi = (SearchUsersActivityWifi )context;
                searchUsersActivityWifi.appendStatus("File copied - " + result);

                File recvFile = new File(result);
                Uri fileUri = FileProvider.getUriForFile(
                        context,
                        "com.example.android.wifidirect.fileprovider",
                        recvFile);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "image/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            SearchUsersActivityWifi searchUsersActivityWifi = (SearchUsersActivityWifi )context;
            searchUsersActivityWifi.appendStatus("Opening a server socket");
        }

        public static boolean copyFile(InputStream inputStream, OutputStream out) {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);

                }
                out.close();
                inputStream.close();
            } catch (IOException e) {
                Log.d(SearchUsersActivityWifi.TAG, e.toString());
                return false;
            }
            return true;
        }
    }
}
