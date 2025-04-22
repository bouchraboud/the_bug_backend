package theBugApp.backend.service;


public interface Validator {
    boolean isValidEmail(String email);
    boolean isValidPhoneNumber(String phoneNumber);

}