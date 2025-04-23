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
@RestController
@RequestMapping("/api/login/oauth2")
public class OAuth2Controller {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/success")
    public Map<String, Object> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            // Si le principal est null, l'utilisateur n'est pas correctement authentifié
            response.put("authenticated", false);
            response.put("message", "Aucune authentification Google détectée");
            return response;
        }

        String email = principal.getAttribute("email");
        try {
            User user = userRepository.findByInfoUser_Email(email).orElseThrow();

            response.put("userId", user.getUserId());
            response.put("username", user.getInfoUser().getUsername());
            response.put("email", email);
            response.put("authenticated", true);
            response.put("provider", "google");
            response.put("message", "Connexion réussie via Google");
        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("message", "Utilisateur non trouvé: " + email);
        }

        return response;
    }
}