
package pt.ulisboa.tecnico.p2photo.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.load.data.BufferedOutputStream;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedInputStream;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ServerCommunicationManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private String name;
    private Context ctx;

    public ServerCommunicationManager(Socket socket, Handler handler, String name, Context ctx) {
        this.socket = socket;
        this.handler = handler;
        this.name = name;
        this.ctx = ctx;
    }

    PrintWriter out = null;
    BufferedReader in = null;
    private InputStream iStream;
    private OutputStream oStream;


    private static final String TAG = "ChatHandler";

    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(oStream)), true);
            in = new BufferedReader(new InputStreamReader(iStream));

            //WRITE
            Log.i(TAG, "GOING TO SEND : USER");
            out.println("USER");

            Log.i(TAG, "GOING TO SEND : " + name);
            out.println(name);

            //READ
            String read = in.readLine();
            Log.i(TAG, "Client read : " + read);

            read = in.readLine();
            Log.i(TAG, "Client read : " + read);


            Log.d(TAG, "FROM CLIENT");
            read = in.readLine();
            Log.d(TAG, "Client read : " + read);
            int sizeOfFiles = Integer.parseInt(read);

            for (int i = 0; i < sizeOfFiles; i++) {
                Log.d(TAG, "Entrou no FOR");
                readFile2();
            }


            Log.i(TAG, "TO CLIENT");

            String ExternalStorageDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String targetPath = ExternalStorageDirectoryPath + "/CMU/teste/";
            File targetDirector = new File(targetPath);
            File[] files = targetDirector.listFiles();

            Log.i(TAG, "GOING TO SEND : " + files.length);
            out.println(Integer.toString(files.length));
            for (File file : files) {
                Log.d(TAG, "Entrou no FOR");
                writeFile2(file);
//                sendPhoto(file);
            }

            out.flush();



        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "BIG EXCECAO : " +  e.toString());
        } finally {
            Log.d(TAG, "FECHOU COMMUNICATION MANAGER");
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeFile2(File f) throws IOException {

        ContentResolver cr = ctx.getContentResolver();
        InputStream is = null;
        Uri fileUri = Uri.fromFile(f);
        try {
            is = cr.openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        }

        copyFile(is, oStream);
        Log.d(TAG, "Client: Data written");
        is.close();
    }

    public void readFile2() throws IOException {
        try {
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
            //InputStream inputstream = socket.getInputStream();

           OutputStream outputStream =  new FileOutputStream(f);
           copyFile(iStream, new FileOutputStream(f));
           outputStream.close();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
           // out.close();
            //inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }

        return true;
    }

}
