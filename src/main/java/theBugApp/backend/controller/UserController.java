package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.UserConfirmationToken;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.exception.UsernameExistsException;
import theBugApp.backend.service.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import theBugApp.backend.repository.UserConfirmationTokenRepo;
import theBugApp.backend.repository.UserRepository;


@RestController
@AllArgsConstructor
@CrossOrigin("*")
public class UserController {
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;
    private final UserConfirmationTokenRepo confirmationTokenRepo;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;


    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable(value = "id") Long id) {
        try {
            UserDto user = userService.getUserById(id);
            return ResponseEntity.ok().body(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/register/users")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {

            logger.info("Received request to register new user: " + user.getInfoUser().getUsername());
            UserDto savedUser = userService.saveUser(user);
            logger.info("Successfully registered user: " + user.getInfoUser().getUsername());
            return ResponseEntity.ok(savedUser);
        } catch (EmailNonValideException | UsernameExistsException e) {
            logger.warning("Failed to register user: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/register/users/confirmation")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        try {
            userService.confirmEmail(token);

            // Page HTML avec postMessage qui envoie le token au frontend
            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Confirmation</title>
                <script>
                    window.addEventListener('load', () => {
                        // Envoie le token au frontend via postMessage
                        window.opener?.postMessage({
                            type: 'EMAIL_VERIFIED',
                            token: '%s'
                        }, 'http://localhost:3000'); // <- adapte l'URL à ton frontend
                        setTimeout(() => window.close(), 1500);
                    });
                </script>
            </head>
            <body>
                <h1>Votre adresse email a bien été confirmée.</h1>
                <p>Cette fenêtre va se fermer automatiquement.</p>
            </body>
            </html>
        """.formatted(token);

            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/exchange-token")
    public ResponseEntity<?> exchangeToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token manquant");
        }

        UserConfirmationToken confirmationToken = confirmationTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        User user = confirmationToken.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur introuvable");
        }

        // Génération du JWT
        Instant now = Instant.now();
        String email = user.getInfoUser().getEmail();

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getInfoUser().getUsername());
        claims.put("email", email);
        // Suppression du rôle
        claims.put("userId", user.getUserId());
        claims.put("reputation", user.getReputation());
        claims.put("confirmed", user.isConfirmed());
        if (user.getPhotoUrl() != null) {
            claims.put("photoUrl", user.getPhotoUrl());
        }

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuer("theBugApp")
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .subject(email)
                .claim("claims", claims)
                .build();

        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                jwtClaimsSet
        );

        String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

        // Supprime le token temporaire après usage
        confirmationTokenRepo.delete(confirmationToken);

        return ResponseEntity.ok(Map.of("access-token", jwt));
    }


    @GetMapping("/users/{id}/questions")
    public ResponseEntity<?> getUserQuestions(@PathVariable Long id) {
        try {
            List<QuestionResponseDTO> questions = userService.getQuestionsByUserId(id);
            return ResponseEntity.ok(questions);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}