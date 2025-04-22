package theBugApp.backend;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailTest {

    public static void main(String[] args) {
        // Set up SMTP server details
        String host = "smtp.gmail.com";
        String port = "587";
        String user = "boudiwanbouchra@gmail.com"; // Replace with your Gmail address
        String password = "jcvv wrym qroz vusa"; // Replace with your Gmail app password or password

        // Set up recipient and email content
        String to = "rihanbouch@gmail.com"; // Replace with the recipient's email
        String subject = "Test Email";
        String body = "<p>This is a test email sent via Gmail's SMTP server.</p>";

        // Properties for the connection
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Create a message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(body, "text/html");

            // Send the message
            Transport.send(message);
            System.out.println("Test email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
