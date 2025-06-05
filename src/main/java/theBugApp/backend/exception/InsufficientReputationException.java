package theBugApp.backend.exception;

public class InsufficientReputationException extends RuntimeException {

    public InsufficientReputationException(String message) {
        super(message);
    }

    public InsufficientReputationException(String message, Throwable cause) {
        super(message, cause);
    }
}