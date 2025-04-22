package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
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
    public Map<String, String> login(@RequestBody AuthRequest request) throws BadRequestException, UserNotFoundException {
        String email = request.getEmail();
        String password = request.getPassword();

        logger.info("Login attempt for user: " + email);

        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Get the current time
            Instant instant = Instant.now();

            // Retrieve the user from the repository
            User user = userRepository.findByInfoUser_Email(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Get the roles (scope) of the user
            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            // Create claims for the JWT token
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

            // Build the JWT token
            JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                    .issuer("theBugApp")
                    .issuedAt(instant)
                    .expiresAt(instant.plus(24, ChronoUnit.HOURS))
                    .subject(email)
                    .claim("claims", claims)
                    .build();

            // Encode the JWT token
            JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS512).build(),
                    jwtClaimsSet
            );

            // Return the token as a response
            String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
            logger.info("Successfully generated JWT token for user: " + email);
            return Map.of("access-token", jwt);

        } catch (BadCredentialsException ex) {
            logger.severe("Invalid credentials for user: " + email);
            throw new BadRequestException("Invalid email or password");
        } catch (UserNotFoundException ex) {
            logger.severe("User not found: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.severe("Authentication failed for user: " + email + " - " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Authentication failed", ex);
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