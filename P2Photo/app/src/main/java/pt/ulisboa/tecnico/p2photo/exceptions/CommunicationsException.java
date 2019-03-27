package pt.ulisboa.tecnico.p2photo.exceptions;

public class CommunicationsException extends Exception {
	private static final long serialVersionUID = 1L;

	public CommunicationsException(String errorMessage) {
        super(errorMessage);
    }
}
