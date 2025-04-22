package theBugApp.backend.exception;


public class EmailNonValideException extends Exception {

    public EmailNonValideException(String message) {
        super(message);
    }

    public EmailNonValideException(String message, Throwable cause) {
        super(message, cause);
    }
}