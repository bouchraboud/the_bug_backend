package theBugApp.backend.service;

import lombok.AllArgsConstructor;
import theBugApp.backend.entity.PasswordResetToken;
import theBugApp.backend.entity.User;
import theBugApp.backend.exception.InvalidResetPasswordTokenException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.repository.PasswordResetTokenRepository;
import theBugApp.backend.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void initiatePasswordReset(String email) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByInfoUser_Email(email);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found");
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        Date expiryDate = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 heure

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        tokenRepository.save(resetToken);

        sendPasswordResetEmail(user.getInfoUser().getEmail(), token);
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        return resetToken != null && resetToken.getExpiryDate().after(new Date());
    }

    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) throws InvalidResetPasswordTokenException {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            throw new InvalidResetPasswordTokenException("Invalid or expired token");
        }

        User user = resetToken.getUser();
        user.getInfoUser().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Voici votre code de réinitialisation : " + token + "\nCe code expirera dans 1 heure.");
        mailSender.send(message);
    }
}
