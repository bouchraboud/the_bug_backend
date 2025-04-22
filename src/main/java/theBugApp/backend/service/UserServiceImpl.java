package theBugApp.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.InfoUser;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.UserConfirmationToken;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.exception.UsernameExistsException;
import theBugApp.backend.mappers.UserMapper;
import theBugApp.backend.repository.UserConfirmationTokenRepo;
import theBugApp.backend.repository.UserRepository;


import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final long EXPIRATION_TIME_MS = 60 * 60 * 1000;

    private final UserMapper userMapper;
    private final UserRepository userRepo;
    private final Validator validator;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserConfirmationTokenRepo confirmationTokenRepo;

    @Override
    public UserDto saveUser(User user) throws EmailNonValideException, UsernameExistsException {
        validateUserInfo(user);

        InfoUser info = user.getInfoUser();
        user.getInfoUser().setPassword(passwordEncoder.encode(user.getInfoUser().getPassword()));
        // Set up bidirectional relationship
        info.setUser(user);

        user.setReputation(0);
        user.setConfirmed(false);

        User savedUser = userRepo.save(user);
        sendConfirmationEmailAsync(savedUser);

        return userMapper.toUserDto(savedUser);
    }



    private void validateUserInfo(User user) throws EmailNonValideException, UsernameExistsException {
        InfoUser info = user.getInfoUser();

        if (!validator.isValidEmail(info.getEmail())) {
            throw new EmailNonValideException("Format d'email invalide");
        }

        if (userRepo.existsByInfoUser_Email(info.getEmail())) {
            throw new EmailNonValideException("Cet email existe déjà");
        }

        if (userRepo.existsByInfoUser_Username(info.getUsername())) {
            throw new UsernameExistsException("Ce nom d'utilisateur existe déjà");
        }
    }


    private Long generateUserId() {
        return 100000L + new Random().nextInt(900000);
    }


    private void sendConfirmationEmailAsync(User user) {
        String token = UUID.randomUUID().toString();
        // Use the constructor you already have
        UserConfirmationToken confirmationToken = new UserConfirmationToken(token, user);
        // Don't set the ID manually - let it be auto-generated

        confirmationTokenRepo.save(confirmationToken);

        // Use CompletableFuture instead of raw threads
        CompletableFuture.runAsync(() -> {
            sendConfirmationEmail(user.getInfoUser().getEmail(), token);
        });
    }

    @Override
    @Transactional
    public User confirmEmail(String token) {
        UserConfirmationToken confirmationToken = confirmationTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token de confirmation invalide"));

        if (new Date().getTime() - confirmationToken.getCreatedDate().getTime() > EXPIRATION_TIME_MS) {
            throw new RuntimeException("Le token de confirmation a expiré");
        }

        // Get user directly from the token's relationship
        User user = confirmationToken.getUser();
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        user.setConfirmed(true);

        // Delete the token after use
        confirmationTokenRepo.delete(confirmationToken);

        return userRepo.save(user);
    }

    @Override
    public void sendConfirmationEmail(String recipientEmail, String confirmationToken) {
        String confirmationUrl = "http://localhost:8080/register/users/confirmation?token=" + confirmationToken;
        String subject = "Confirmez votre adresse email";
        String emailContent = "<p>Cher utilisateur,</p>"
                + "<p>Veuillez cliquer sur le lien ci-dessous pour confirmer votre adresse email:</p>"
                + "<p><a href=\"" + confirmationUrl + "\">Confirmer l'email</a></p>"
                + "<p>Merci!</p>";

        sendEmail(recipientEmail, subject, emailContent);
    }

    @Override
    public void sendEmail(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("ton.email@gmail.com"); // assure-toi de définir un expéditeur
            mailSender.send(message);
            System.out.println("✅ Email envoyé à : " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public UserDto getUserById(Long id) throws UserNotFoundException {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé pour cet id :: " + id));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) throws UserNotFoundException {
        User user = userRepo.findByInfoUser_Email(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé pour cet email :: " + email));
        return userMapper.toUserDto(user);
    }
}