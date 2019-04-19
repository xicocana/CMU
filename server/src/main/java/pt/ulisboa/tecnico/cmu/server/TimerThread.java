package pt.ulisboa.tecnico.cmu.server;

import pt.ulisboa.tecnico.cmu.server.exceptions.ServerLibraryException;

public class TimerThread implements Runnable {

	private static final String REGISTER_CLIENTS_FILE = "register_clients.json";
	
	public TimerThread() {
		
	}

	@Override
	public void run() {
		ServerLibrary lib = new ServerLibrary();
		while(true) {			
			try {
				Thread.sleep(10*60*1000);
				System.out.println("Ready to wipe out all available tokens...");
				lib.wipeOutSessionKeys(REGISTER_CLIENTS_FILE);
			} catch (ServerLibraryException sle) {
				System.out.println("run(): Something went wrong with the Utils class...");
				break;
			} catch (InterruptedException e) {
				System.out.println("run(): Something went wrong with the Thread class...");
			}
		}
	}
}
