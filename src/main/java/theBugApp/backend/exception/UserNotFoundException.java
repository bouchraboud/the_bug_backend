package theBugApp.backend.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("User with email " + email + " does not exist");
    }
    public UserNotFoundException(Long id) {
        super("User with ID " + id + " does not exist");
    }
}