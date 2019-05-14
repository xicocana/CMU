
package pt.ulisboa.tecnico.p2photo.wifi;

import android.os.Handler;
import android.util.Log;

import com.google.api.services.drive.model.User;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private CommunicationManager peerCommunicatioManager;
    private InetAddress mAddress;
    private String name;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, String name) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
        this.name = name;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(), SearchUsersActivityWifi.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            peerCommunicatioManager = new CommunicationManager(socket, handler,"TESTE_CLIENT");
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
