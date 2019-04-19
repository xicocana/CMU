package pt.ulisboa.tecnico.cmu.server;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import pt.ulisboa.tecnico.sec.communications.Communications;
import pt.ulisboa.tecnico.sec.communications.exceptions.CommunicationsException;

public class ServerApp {
	
	private static boolean isRunning = true;
	
	public static void waitForClients(ServerSocket serverSocket) {
		new Thread(new TimerThread()).start();
		Socket clientSocket = null;
		
		while(isRunning) {
	        try {
	        	clientSocket = serverSocket.accept();
	        	
	            InetAddress serverInetAddress = clientSocket.getLocalAddress();
	            InetAddress clientInetAddress = clientSocket.getInetAddress();
	            
	    		System.out.println("Your current IP address : " + serverInetAddress.toString());
	    		System.out.println("Your current Hostname : " + serverInetAddress.getHostName());
	            System.out.printf("Connected to client %s on port %d %n", clientInetAddress.getHostAddress(), clientSocket.getPort());
	        } catch (IOException e) {
	            System.err.println("Accept failed.");
	            System.exit(1);
	        }	        
	        new Thread(new WorkerThread(clientSocket, "Multithreaded Server")).start();
		}
	}
	
	public static void main(String[] args) {
		
		String hostname = "localhost";
		System.out.println("############### CMU SERVER ###############");
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket();
			InetSocketAddress endpoint = new InetSocketAddress("192.168.43.141", 8080);
			serverSocket.bind(endpoint);

			System.out.printf("My hostname is %s, my IP is %s and my service port is %d %n", serverSocket.getInetAddress().getHostName(), serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
		    waitForClients(serverSocket);

	        serverSocket.close();
	        System.out.println("Connection closed");
		} catch (IOException e) {
			System.err.println("Could not initialize server socket! Aborting...");
			System.exit(-1);
		}		
		
	}
}
