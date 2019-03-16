package pt.ulisboa.tecnico.p2photo;

import java.io.IOException;
import java.net.Socket;

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
    	synchronized(this){
            this.runningThread = Thread.currentThread();
        }
    	
    	Communications communications = new Communications(clientSocket);
    	
    	//implementar o paralelismo das nossas threads como em SIRS. Aqui nao fiz nada, apenas implementei o paralelismo na sua criacao acima
    	while(isRunning) {
			try {
				String input = (String) communications.receiveInChunks();
				System.out.println(input);

		    	switch(input) {
		    		case "LUSIADAS":
		    			String livro = (String) communications.receiveInChunks();
		    			System.out.println(livro);
		    			break;
		    		default:
		    			System.out.println("Wrong input command. Try another one.");
		    	}
		    	//TODO mudar esta excepcao para uma excepcao da propria classe das comunicacoes
			} catch (IOException e) {
				// caso o socket do cliente seja fechado vai haver uma explosao no socket deste lado
				// como tal o String input nao vai conseguir ler nada do seu lado.
				System.out.println("Lost communication to the client. Ending worker thread...");
				isRunning = false;
			}
			
    	}
        
    }
    
}
