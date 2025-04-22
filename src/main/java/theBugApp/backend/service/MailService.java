package theBugApp.backend.service;

import theBugApp.backend.entity.User;




public interface MailService {
    void sendEmail(String to, String subject, String body);
    User confirmEmail(String token);
    void sendConfirmationEmail(String to, String token);
}
