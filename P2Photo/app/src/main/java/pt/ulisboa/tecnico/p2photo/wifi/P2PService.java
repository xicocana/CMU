package pt.ulisboa.tecnico.p2photo.wifi;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class P2PService extends IntentService implements Handler.Callback {


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


    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "pt.ulisboa.tecnico.p2photo.wifi.action.FOO";
    private static final String ACTION_BAZ = "pt.ulisboa.tecnico.p2photo.wifi.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "pt.ulisboa.tecnico.p2photo.wifi.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "pt.ulisboa.tecnico.p2photo.wifi.extra.PARAM2";

    public P2PService() {
        super("P2PService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        Intent serviceIntent = new Intent(context, P2PService.class);
        serviceIntent.setAction(P2PService.ACTION_FOO);
        context.startService(serviceIntent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                handleActionFoo();
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    private void startRegistrationAndDiscovery() {
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

//                                    arrayAdapter.add(service.device.deviceName + " - " + service.instanceName);
//                                    arrayAdapter.notifyDataSetChanged();
//                                    usersListWiFiP2pService.add(service);
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


    public void appendStatus(String status) {
        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo() {
        startRegistrationAndDiscovery();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
