package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.UpdateUserDto;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.UserConfirmationToken;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.exception.UsernameExistsException;
import theBugApp.backend.service.AnswerService;
import theBugApp.backend.service.FollowService;
import theBugApp.backend.service.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import theBugApp.backend.repository.UserConfirmationTokenRepo;
import theBugApp.backend.repository.UserRepository;


@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/api") // <-- ADD THIS LINE
public class UserController {
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;
    private final UserConfirmationTokenRepo confirmationTokenRepo;
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final AnswerService answerService;
    private final FollowService followService; // Nouveau service



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
    @GetMapping("/users/{id}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(answerService.getAnswersByUserId(id));
    }
    @PostMapping("/users/follow/{followingId}")
    public ResponseEntity<?> followUser(@PathVariable Long followingId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Map<String, Object> claims = jwt.getClaim("claims");
            Long followerId = ((Number) claims.get("userId")).longValue();
            boolean success = followService.followUser(followerId, followingId);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "User followed successfully"));
            } else {
                return ResponseEntity.badRequest().body("Already following this user");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/users/unfollow/{followingId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long followingId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Map<String, Object> claims = jwt.getClaim("claims");
            Long followerId = ((Number) claims.get("userId")).longValue();
            boolean success = followService.unfollowUser(followerId, followingId);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "User unfollowed successfully")); // Fixed
            } else {
                return ResponseEntity.badRequest().body("Not following this user"); // Fixed
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Vérifier si un utilisateur en suit un autre
    @GetMapping("/users/{followerId}/is-following/{followingId}")
    public ResponseEntity<?> isFollowing(@PathVariable Long followerId, @PathVariable Long followingId) {
        try {
            boolean isFollowing = followService.isFollowing(followerId, followingId);
            return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Obtenir les followers d'un utilisateur
    @GetMapping("/users/{id}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable Long id) {
        try {
            List<UserDto> followers = followService.getFollowers(id);
            return ResponseEntity.ok(followers);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Obtenir les utilisateurs qu'un utilisateur suit
    @GetMapping("/users/{id}/following")
    public ResponseEntity<?> getFollowing(@PathVariable Long id) {
        try {
            List<UserDto> following = followService.getFollowing(id);
            return ResponseEntity.ok(following);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Obtenir les statistiques de suivi
    @GetMapping("/users/{id}/follow-stats")
    public ResponseEntity<?> getFollowStats(@PathVariable Long id) {
        try {
            long followersCount = followService.getFollowersCount(id);
            long followingCount = followService.getFollowingCount(id);

            Map<String, Object> stats = new HashMap<>();
            stats.put("followersCount", followersCount);
            stats.put("followingCount", followingCount);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PutMapping("/users")
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateUserDto updateUserDto) {

        try {
            Map<String, Object> claims = jwt.getClaim("claims");
            Long userId = ((Number) claims.get("userId")).longValue();
            UserDto updatedUser = userService.updateUser(userId, updateUserDto);
            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "VALIDATION_ERROR", "message", e.getMessage()));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("non trouvé")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "USER_NOT_FOUND", "message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "Erreur interne du serveur"));
        }
    }



}