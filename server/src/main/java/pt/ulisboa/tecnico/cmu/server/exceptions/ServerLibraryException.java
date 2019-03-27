package pt.ulisboa.tecnico.cmu.server.exceptions;

public class ServerLibraryException extends Exception {
	private static final long serialVersionUID = 1L;
	private boolean requiresTermination = false;
	
	public ServerLibraryException(String errorMessage, Throwable err, boolean requiresTermination) {
		super(errorMessage, err);
		this.requiresTermination = requiresTermination;
	}
	
	public ServerLibraryException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}
	
	public ServerLibraryException(String errorMessage, boolean requiresTermination) {
		super(errorMessage);
		this.requiresTermination = requiresTermination;
	}
	
	public ServerLibraryException(String errorMessage) {
		super(errorMessage);
	}
	
	public boolean getTerminationStatus() {
		return this.requiresTermination;
	}
}
