package theBugApp.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/login/oauth2")
public class OAuth2Controller {
//
//    private static final Logger logger = Logger.getLogger(OAuth2Controller.class.getName());
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @GetMapping("/success")
//    public Map<String, Object> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
//        Map<String, Object> response = new HashMap<>();
//
//        if (principal == null) {
//            response.put("authenticated", false);
//            response.put("message", "Aucune authentification OAuth2 détectée");
//            return response;
//        }
//
//        // Déterminer quel provider est utilisé
//        String email = principal.getAttribute("email");
//        String provider = "unknown";
//
//        // Détection du provider basée sur les attributs disponibles
//        if (principal.getAttribute("sub") != null) {
//            provider = "google";
//        } else if (principal.getAttribute("id") != null) {
//            provider = "github";
//            // GitHub peut ne pas exposer l'email directement selon les paramètres de confidentialité
//            if (email == null) {
//                email = principal.getAttribute("login") + "@github.user";
//            }
//        }
//
//        logger.info("OAuth2 login attempt for provider: " + provider + ", email: " + email);
//
//        try {
//            // Essayons d'abord de trouver l'utilisateur par email
//            User user = userRepository.findByInfoUser_Email(email).orElseThrow();
//
//            response.put("userId", user.getUserId());
//            response.put("username", user.getInfoUser().getUsername());
//            response.put("email", email);
//            response.put("authenticated", true);
//            response.put("provider", provider);
//
//            // Si l'utilisateur a une photo de profil, incluons-la dans la réponse
//            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
//                response.put("photoUrl", user.getPhotoUrl());
//            }
//
//            response.put("message", "Connexion réussie via " + provider);
//            logger.info("Successful OAuth2 authentication for user: " + email);
//
//        } catch (Exception e) {
//            logger.severe("OAuth2 authentication failed: " + e.getMessage());
//            response.put("authenticated", false);
//            response.put("message", "Utilisateur non trouvé: " + email);
//        }
//
//        return response;
//    }
}