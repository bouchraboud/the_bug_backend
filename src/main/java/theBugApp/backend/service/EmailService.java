package theBugApp.backend.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTestEmail() throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("boudiwanbouchra@gmail.com");
        helper.setTo("bouchraboudio@gmail.com");
        helper.setSubject("Test Email");
        helper.setText("<p>This is a test email sent via Spring Boot!</p>", true);

        mailSender.send(message);
        System.out.println("✅ Email sent successfully!");
    }
}
