package pt.ulisboa.tecnico.p2photo;

import java.net.Socket;
import pt.ulisboa.tecnico.p2photo.Communications;
import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

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