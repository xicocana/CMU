package pt.ulisboa.tecnico.p2photo.exceptions;

public class TaskException extends Exception {
    private static final long serialVersionUID = 1L;
    private boolean requiresTermination = false;

    public TaskException(String errorMessage, Throwable err, boolean requiresTermination) {
        super(errorMessage, err);
        this.requiresTermination = requiresTermination;
    }

    public TaskException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public TaskException(String errorMessage, boolean requiresTermination) {
        super(errorMessage);
        this.requiresTermination = requiresTermination;
    }

    public TaskException(String errorMessage) {
        super(errorMessage);
    }

    public boolean getTerminationStatus() {
        return this.requiresTermination;
    }
}