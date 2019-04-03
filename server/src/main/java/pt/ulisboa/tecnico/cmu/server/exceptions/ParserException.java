package pt.ulisboa.tecnico.cmu.server.exceptions;

public class ParserException extends Exception {
	private boolean requiresTermination = false;
	
	public ParserException(String errorMessage, Throwable err, boolean requiresTermination) {
		super(errorMessage, err);
		this.requiresTermination = requiresTermination;
	}
	
	public ParserException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}
	
	public ParserException(String errorMessage, boolean requiresTermination) {
		super(errorMessage);
		this.requiresTermination = requiresTermination;
	}
	
	public ParserException(String errorMessage) {
		super(errorMessage);
	}
	
	public boolean getTerminationStatus() {
		return this.requiresTermination;
	}
}
