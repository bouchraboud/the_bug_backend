package theBugApp.backend.service;


import theBugApp.backend.exception.InvalidResetPasswordTokenException;
import theBugApp.backend.exception.UserNotFoundException;

public interface PasswordResetService {
    void initiatePasswordReset(String email) throws UserNotFoundException;
    boolean validatePasswordResetToken(String token);
    void resetPassword(String token, String newPassword) throws InvalidResetPasswordTokenException;
}
