package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.UpdateUserDto;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.UserConfirmationToken;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.TokenNotFoundException;
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
    @GetMapping("/users/me")
    public ResponseEntity<?> getUserBytoken(@AuthenticationPrincipal Jwt jwt) {
        try {
            Map<String, Object> claims = jwt.getClaim("claims");
            Long id = ((Number) claims.get("userId")).longValue();
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
        } catch (EmailNonValideException e) {
            logger.warning("Failed to register user - email error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "field", "email"));
        } catch (UsernameExistsException e) {
            logger.warning("Failed to register user - username error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "field", "username"));
        } catch (Exception e) {
            logger.severe("Unexpected error during registration: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred"));
        }
    }
    @GetMapping("/register/users/confirmation")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        try {
            userService.confirmEmail(token); // Your existing service method
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Email confirmed successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
    @PostMapping("users/exchange-token")
    public ResponseEntity<?> exchangeToken(@RequestBody Map<String, String> body) {
        // 1. Vérification du token dans la requête
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Token is required"));
        }

        try {
            // 2. Recherche du token en base de données
            UserConfirmationToken confirmationToken = confirmationTokenRepo.findByToken(token)
                    .orElseThrow(() -> new TokenNotFoundException("Invalid or expired token"));

            // 3. Vérification de l'utilisateur associé
            User user = confirmationToken.getUser();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user associated with this token");
            }

            // 4. Génération du JWT
            Instant now = Instant.now();
            String email = user.getInfoUser().getEmail();

            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getInfoUser().getUsername());
            claims.put("email", email);
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

            // 5. Nettoyage du token temporaire
            confirmationTokenRepo.delete(confirmationToken);

            // 6. Retour du token JWT
            return ResponseEntity.ok(Map.of(
                    "access-token", jwt,
                    "userId", user.getUserId(),
                    "email", email
            ));

        } catch (TokenNotFoundException e) {
            // Format de réponse spécifique pour token invalide/expiré
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Invalid or expired token"));
        } catch (Exception e) {
            // Format cohérent pour les autres erreurs
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "An error occurred while processing your request"));
        }
    }


    @GetMapping("/users/{id}/questions")
    public ResponseEntity<?> getUserQuestions(@PathVariable Long id) {
        try {
            List<QuestionResponseDTO> questions = userService.getQuestionsByUserId(id);
            return ResponseEntity.ok(questions);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
    }
    @GetMapping("/users/{id}/answers")
    public ResponseEntity<?> getAnswersByUser(
            @PathVariable Long id) {
        try {
            List<AnswerResponseDTO> answers = answerService.getAnswersByUserId(id);
        return ResponseEntity.ok(answers);
    } catch (UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
    }
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