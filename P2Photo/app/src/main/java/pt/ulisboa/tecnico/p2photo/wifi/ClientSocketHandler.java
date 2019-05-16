
package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private ClientCommunicationManager peerCommunicatioManager;
    private InetAddress mAddress;
    private String name;
    private Context ctx;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, String name,Context ctx) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
        this.name = name;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(), SearchUsersActivityWifi.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            peerCommunicatioManager = new ClientCommunicationManager(socket, handler,name,ctx);
            new Thread(peerCommunicatioManager).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }
}
