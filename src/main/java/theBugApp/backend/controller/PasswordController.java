package theBugApp.backend.controller;



import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.exception.InvalidResetPasswordTokenException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/password")
@AllArgsConstructor
@CrossOrigin("*")
public class PasswordController {

    private final PasswordResetService passwordResetService;

    // 📬 1. L'utilisateur entre son email pour recevoir un lien de réinitialisation
    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            passwordResetService.initiatePasswordReset(email);
            return ResponseEntity.ok("Un lien de réinitialisation a été envoyé à votre email");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun utilisateur trouvé avec cet email");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue");
        }
    }

    // 🔐 2. L'utilisateur clique sur le lien (avec token) et fournit le nouveau mot de passe
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Le token et le nouveau mot de passe sont requis");
        }

        try {
            if (passwordResetService.validatePasswordResetToken(token)) {
                passwordResetService.resetPassword(token, newPassword);
                return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le token est expiré");
        } catch (InvalidResetPasswordTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue");
        }
    }
}
