package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import theBugApp.backend.dto.AuthRequest;
import theBugApp.backend.entity.User;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.service.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @PostMapping("/login/user")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        logger.info("Login attempt for user: " + email);

        try {
            // Vérifiez si l'utilisateur existe avant d'essayer de l'authentifier
            User user = userRepository.findByInfoUser_Email(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Authentifiez l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Obtenez l'heure actuelle
            Instant instant = Instant.now();

            // Obtenez les rôles (scope) de l'utilisateur
            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            // Créez les revendications pour le jeton JWT
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getInfoUser().getUsername());
            claims.put("email", email);
            claims.put("role", scope);
            claims.put("userId", user.getUserId());
            claims.put("reputation", user.getReputation());
            claims.put("confirmed", user.isConfirmed());
            if (user.getPhotoUrl() != null) {
                claims.put("photoUrl", user.getPhotoUrl());
            }

            // Construisez le jeton JWT
            JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                    .issuer("theBugApp")
                    .issuedAt(instant)
                    .expiresAt(instant.plus(24, ChronoUnit.HOURS))
                    .subject(email)
                    .claim("claims", claims)
                    .build();

            // Encodez le jeton JWT
            JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS512).build(),
                    jwtClaimsSet
            );

            // Retournez le jeton en réponse
            String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
            logger.info("Successfully generated JWT token for user: " + email);
            return ResponseEntity.ok(Map.of("access-token", jwt));

        } catch (UserNotFoundException ex) {
            logger.warning("User not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        } catch (BadCredentialsException ex) {
            logger.warning("Invalid credentials for user: " + email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception ex) {
            logger.severe("Authentication failed for user: " + email + " - " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed due to an unexpected error"));
        }
    }



    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        // TODO: Implement account verification logic
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // JWT tokens are stateless, so server-side logout is not typically needed
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        // TODO: Implement token refresh logic
        return null;
    }
}