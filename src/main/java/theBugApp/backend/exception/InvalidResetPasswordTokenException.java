package theBugApp.backend.exception;

public class InvalidResetPasswordTokenException extends Exception{
    public InvalidResetPasswordTokenException(String message){
        super(message);
    }

}