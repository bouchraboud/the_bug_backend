package theBugApp.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.InfoUser;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.UserConfirmationToken;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.exception.UsernameExistsException;
import theBugApp.backend.mappers.UserMapper;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.UserConfirmationTokenRepo;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.dto.UpdateUserDto;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;


    @Override
    public UserDto saveUser(User user) throws EmailNonValideException, UsernameExistsException {
        validateUserInfo(user);

        InfoUser info = user.getInfoUser();
        user.getInfoUser().setPassword(passwordEncoder.encode(user.getInfoUser().getPassword()));
        // Set up bidirectional relationship
        info.setUser(user);

        user.setReputation(0);
        user.setConfirmed(false);
        user.setCreatedDate(LocalDateTime.now());
        user.setLastSeen(LocalDateTime.now());

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

    @Transactional
    @Override
    public void sendConfirmationEmailAsync(User user) {
        String token = UUID.randomUUID().toString();
        // Use the constructor you already have
        UserConfirmationToken confirmationToken = new UserConfirmationToken(token, user);
        // Don't set the ID manually - let it be auto-generated

        UserConfirmationToken savedToken = confirmationTokenRepo.save(confirmationToken);
        System.out.println("Saved token ID: " + savedToken.getId());

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



        return userRepo.save(user);
    }

    @Override
    public void sendConfirmationEmail(String recipientEmail, String confirmationToken) {
        // Use environment variable for the backend URL
        String confirmationUrl = "localhost:3000/confirm-email?token=" + confirmationToken;

        String subject = "Confirm Your Email Address";
        String emailContent = """
    <html>
    <body style="font-family: Arial, sans-serif;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;">
            <h2 style="color: #333;">Email Confirmation</h2>
            <p>Dear User,</p>
            <p>Thank you for registering. Please click below to confirm your email:</p>
            <div style="margin: 25px 0; text-align: center;">
                <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 20px; 
                   text-decoration: none; border-radius: 4px; display: inline-block;">
                    Confirm Email
                </a>
            </div>
            <p>Ignore this email if you didn't create an account.</p>
            <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
            <p style="font-size: 12px; color: #777;">
                Link expires in 24 hours. Alternatively, paste this URL in your browser:
            </p>
            <p style="font-size: 12px; color: #777; word-break: break-all;">%s</p>
        </div>
    </body>
    </html>
    """.formatted(confirmationUrl, confirmationUrl);

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

    @Override
    @Transactional(readOnly = true)  // This is the correct syntax
    public List<QuestionResponseDTO> getQuestionsByUserId(Long userId) throws UserNotFoundException {
        // First verify user exists
        if (!userRepo.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        // Get questions and convert to DTOs
        return questionRepository.findByUserId(userId).stream()
                .map(questionService::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    @Override
    public UserDto updateUser(Long userId, UpdateUserDto dto) {
        // Validation
        if (dto == null || !dto.hasUpdates()) {
            throw new IllegalArgumentException("Aucune mise à jour fournie");
        }

        // Récupérer l'utilisateur
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Mise à jour basée sur les champs marqués pour la mise à jour
        if (dto.shouldUpdate("photoUrl")) {
            user.setPhotoUrl(dto.getPhotoUrl()); // Peut être null pour "vider" le champ
        }

        if (dto.shouldUpdate("reputation")) {
            user.setReputation(dto.getReputation() != null ? dto.getReputation() : 0);
        }

        if (dto.shouldUpdate("isConfirmed")) {
            user.setConfirmed(dto.getIsConfirmed() != null ? dto.getIsConfirmed() : false);
        }

        if (dto.shouldUpdate("country")) {
            user.setCountry(dto.getCountry()); // Peut être null pour "supprimer" le pays
        }
        if (dto.shouldUpdate("about")) {
            user.setAbout(dto.getAbout()); // ✅ FIXED - now correctly sets about
        }

        if (dto.shouldUpdate("githubLink")) {
            user.setGithubLink(dto.getGithubLink()); // ✅ FIXED - now correctly sets githubLink
        }

        if (dto.shouldUpdate("portfolioLink")) {
            user.setPortfolioLink(dto.getPortfolioLink()); // ✅ FIXED - now correctly sets portfolioLink
        }

        // Sauvegarder et retourner
        User savedUser = userRepo.save(user);
        return userMapper.toUserDto(savedUser);
    }

}