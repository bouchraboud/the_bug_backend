package theBugApp.backend.service;


import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ValidatorService implements Validator{

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PHONE_PATTERN = "^(\\+212|0)([5-7])\\d{8}$";
    private static final String CIN_PATTERN = "^[A-Z]{2}[0-9]{6}$";

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(PHONE_PATTERN).matcher(phoneNumber).matches();
    }
}