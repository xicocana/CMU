package pt.ulisboa.tecnico.p2photo.exceptions;

public class UtilsException extends Exception {
    private static final long serialVersionUID = 1L;
    private boolean requiresTermination = false;

    public UtilsException(String errorMessage, Throwable err, boolean requiresTermination) {
        super(errorMessage, err);
        this.requiresTermination = requiresTermination;
    }

    public UtilsException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public UtilsException(String errorMessage, boolean requiresTermination) {
        super(errorMessage);
        this.requiresTermination = requiresTermination;
    }

    public UtilsException(String errorMessage) {
        super(errorMessage);
    }

    public boolean getTerminationStatus() {
        return this.requiresTermination;
    }
}