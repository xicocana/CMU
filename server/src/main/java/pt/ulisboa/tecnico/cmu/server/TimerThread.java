package pt.ulisboa.tecnico.cmu.server;

import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;

public class TimerThread implements Runnable {

	private static final String REGISTER_CLIENTS_FILE = "register_clients.json";
	
	public TimerThread() {
		
	}

	@Override
	public void run() {
		while(true) {			
			try {
				Thread.sleep(30*60*1000);
				System.out.println("Ready to wipe out all available tokens...");
				Utils.readFile(REGISTER_CLIENTS_FILE);
			} catch (ServerLibraryException sle) {
				System.out.println("run(): Something went wrong with the Utils class...");
				break;
			} catch (InterruptedException ie) {
				System.out.println("run(): Something went wrong with the Thread class...");
			}
		}
	}
}
