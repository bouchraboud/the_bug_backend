package theBugApp.backend.exception;


public class UsernameExistsException extends Exception {
  public UsernameExistsException(String message) {
    super(message);
  }
}