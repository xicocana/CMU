
package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ServerCommunicationManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private String name;

    public ServerCommunicationManager(Socket socket, Handler handler, String name) {
        this.socket = socket;
        this.handler = handler;
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
            byte[] buffer2 = new byte[1024];
            int bytes;

            write("USER");
            write(name);

            bytes = iStream.read(buffer);
            String readMessage = new String((byte[]) buffer, 0, bytes);
            if (readMessage.equals("USER")) {
                Log.i(TAG, "recieve command user");
                bytes = iStream.read(buffer2);
                readMessage = new String((byte[]) buffer2, 0, bytes);
                Log.i(TAG, "recieve command user: "+readMessage);
            }

            /*handler.obtainMessage(SearchUsersActivityWifi.MY_HANDLE, this).sendToTarget();

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    String readMessage = new String((byte[]) buffer, 0, bytes);
                    if (readMessage.equals("USER")) {
                        Log.d(TAG, "RECEIVE COMMAND " + readMessage);

                        bytes = iStream.read(buffer);
                        if (bytes == -1) {
                            break;
                        }

                        readMessage = new String((byte[]) buffer, 0, bytes);
                        Log.d(TAG, "MSG " + readMessage);

                        //handler.obtainMessage(SearchUsersActivityWifi.MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                        write("SEND-PHOTO");
                        Log.d(TAG, "SEND-PHOTO abc");
                    }

                    if (readMessage.equals("SEND-PHOTO")) {
                        Log.d(TAG, "RECEIVE COMMAND " + readMessage);
                        bytes = iStream.read(buffer);

                        if (bytes == -1) {
                            break;
                        }
                        readMessage = new String((byte[]) buffer, 0, bytes);
                        Log.d(TAG, "MSG " + readMessage);

                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "FECHOU COMMUNICATION MANAGER");
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
