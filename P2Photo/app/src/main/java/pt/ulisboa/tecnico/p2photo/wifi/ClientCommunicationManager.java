
package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import static pt.ulisboa.tecnico.p2photo.wifi.FileTransferService.copyFile;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ClientCommunicationManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private String name;
    private Context ctx;

    public ClientCommunicationManager(Socket socket, Handler handler, String name, Context ctx) {
        this.socket = socket;
        this.handler = handler;
        this.name = name;
        this.ctx = ctx;
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";

    private BufferedReader in;
    PrintWriter out;

    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //READ
            String read = in.readLine();
            Log.d(TAG, "Client read : " + read);

            read = in.readLine();
            Log.d(TAG, "Client read : " + read);

            //WRITE
            Log.i(TAG, "GOING TO SEND : USER");
            out.println("USER");

            Log.i(TAG, "GOING TO SEND : " + name);
            out.println(name);

            Log.d(TAG, "FROM SERVER");
            read = in.readLine();
            Log.d(TAG, "Client read : " + read);
            int sizeOfFiles = Integer.parseInt(read);

            for (int i = 0; i < sizeOfFiles; i++) {
                Log.d(TAG, "Entrou no FOR");
                readFile2();
            }

            Log.d(TAG, "TO SERVER");

            String ExternalStorageDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String targetPath = ExternalStorageDirectoryPath + "/CMU/teste/";
            File targetDirector = new File(targetPath);
            File[] files = targetDirector.listFiles();

            Log.i(TAG, "GOING TO SEND : " + files.length);
            out.println(Integer.toString(files.length));


            for (File file : files) {
                Log.d(TAG, "Entrou no FOR");
                writeFile2(file);
            }

            //TODO
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "EXCECAO _ CLIENTE" + e.toString());
        } finally {
            Log.d(TAG, "FECHOU COMMUNICATION MANAGER");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeFile2(File f) throws IOException {
        try {
            Log.d(TAG, "AQUI");
            OutputStream stream = socket.getOutputStream();
            ContentResolver cr = ctx.getContentResolver();
            InputStream is = null;
            Uri fileUri = Uri.fromFile(f);

            is = cr.openInputStream(fileUri);

            copyFile(is, stream);
            Log.d(TAG, "Client: Data written");
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void readFile2() throws IOException {
        try {
            Log.d(TAG, "Server: connection done");

            Random random = new Random();
            int randomInt = random.nextInt(999) + 111;
            String name = "abc" + randomInt;
            java.io.File f = new java.io.File(Environment.getExternalStorageDirectory() + "/CMU-wifi-cache/" + "teste", name + ".jpg");
            Log.d(TAG, "Entrou no AQUI");
            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();
            Log.d(TAG, "Entrou no AQUI2");
            Log.d(TAG, "server: copying files " + f.toString());
            InputStream inputstream = socket.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            Log.d("Chat", "Entrou no AQUI66");
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }
}
