
package pt.ulisboa.tecnico.p2photo.wifi;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class PeerCommunicatioManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private boolean isGroupOwner;
    private String name;

    public PeerCommunicatioManager(Socket socket, Handler handler, String name) {
        this.socket = socket;
        this.handler = handler;
        this.isGroupOwner = isGroupOwner;
        this.name = name;
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";


    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            write("USER");
            write("TESTE_USER");

            handler.obtainMessage(SearchUsersActivityWifi.MY_HANDLE, this).sendToTarget();

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    String readMessage = new String((byte[]) buffer, 0, bytes);
                    if (readMessage.equals("USER")) {
                        Log.d(TAG, "RECEIVE COMMAND_USER:" + readMessage);
                        bytes = iStream.read(buffer);
                        if (bytes == -1) {
                            break;
                        }
                    }

                    // Send the obtained bytes to the UI Activity
                    Log.d(TAG, "Rec:" + String.valueOf(buffer));
                    handler.obtainMessage(SearchUsersActivityWifi.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "FECHOU PEERCOMM MANAGER");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String read() {
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            // Read from the InputStream
            bytes = iStream.read(buffer);

            // Send the obtained bytes to the UI Activity
            Log.d(TAG, "Rec:" + String.valueOf(buffer));

            handler.obtainMessage(SearchUsersActivityWifi.MESSAGE_READ,
                    bytes, -1, buffer).sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "disconnected", e);
        }
        return "";
    }

    public void write(String msg) {
        final byte[] buffer = msg.getBytes();
        Thread thread = new Thread() {
            public void run() {
                try {
                    oStream.write(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }
        };
        thread.start();
    }


//    @Override
//    public void run2() {
//        try {
//
//            iStream = socket.getInputStream();
//            oStream = socket.getOutputStream();
//            byte[] buffer = new byte[1024];
//            int bytes;
//            handler.obtainMessage(UserOptionsActivityWifi.MY_HANDLE, this)
//                    .sendToTarget();
//
//            while (true) {
//                try {
//                    // Read from the InputStream
//                    bytes = iStream.read(buffer);
//                    if (bytes == -1) {
//                        break;
//                    }
//
//                    // Send the obtained bytes to the UI Activity
//                    Log.d(TAG, "Rec:" + String.valueOf(buffer));
//                    handler.obtainMessage(UserOptionsActivityWifi.MESSAGE_READ,
//                            bytes, -1, buffer).sendToTarget();
//                } catch (IOException e) {
//                    Log.e(TAG, "disconnected", e);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }


}
