package pt.ulisboa.tecnico.cmu.server;

import java.net.Socket;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;

import org.json.JSONObject;

public class WorkerThread implements Runnable{

    private boolean isRunning = true;

    protected Socket clientSocket = null;
    protected String serverText   = null;
    protected Thread runningThread= null;

    public WorkerThread(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        //TODO melhorar controlo do multi-threading
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }

        Communications communications = new Communications(clientSocket);

        try {


            //TODO implementar o paralelismo das nossas threads como em SIRS. Aqui nao fiz nada, apenas implementei o paralelismo na sua criacao acima
            while(isRunning) {
                String input;
                    input = (String) communications.receiveInChunks();
                    System.out.println(input);


                    switch(input) {
                        case "LUSIADAS":
                            String lusiadas = (String) communications.receiveInChunks();
                            System.out.println(lusiadas);
                            break;
                        case "SIGN-UP":
                            String data = (String) communications.receiveInChunks();
                            System.out.println(data);

                            //TODO mudar esta excepcao de merda aquando a biblioteca
                            JSONObject obj = new JSONObject(data);
                            String user = (String) obj.get("user-name");
                            String password = (String) obj.get("password");
                            
                            try {
                                BufferedWriter bw = new BufferedWriter(new FileWriter("registered_clients.txt"));
                                
                                bw.write(data);
                                bw.close();
                            } catch(IOException ioe) {
                                System.out.println("foda-se");
                            }
                            break;
                        default:
                            System.out.println("Wrong input command. Try another one.");
                    }
            }
        } catch (CommunicationsException ce) {
            System.out.println("Communications module broke down. Aborting...");
            isRunning = false;
        }
    }
}
