package theBugApp.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import theBugApp.backend.entity.InfoUser;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.UserRepository;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = Logger.getLogger(CustomOAuth2UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            String provider = userRequest.getClientRegistration().getRegistrationId();
            logger.info("OAuth2 provider detected: " + provider);

            // Attention: Les attributs diffèrent selon le provider
            String email = null;
            String providerId = null;
            String name = null;

            // Gestion des attributs selon le provider
            if ("github".equals(provider)) {
                // GitHub utilise "id" comme identifiant unique
                Object idObject = oAuth2User.getAttribute("id");
                providerId = idObject != null ? idObject.toString() : null;

                email = oAuth2User.getAttribute("email");
                // GitHub peut ne pas fournir d'email si les paramètres de confidentialité l'empêchent
                String login = oAuth2User.getAttribute("login");
                if (email == null && login != null) {
                    email = login + "@github.user";
                }
                name = login != null ? login : "github_user";

                logger.info("GitHub user detected: " + name + ", providerId: " + providerId);
            } else {
                // Google et autres providers
                providerId = oAuth2User.getAttribute("sub");
                email = oAuth2User.getAttribute("email");
                name = oAuth2User.getAttribute("name");

                if (email == null) {
                    // Fallback pour les cas où l'email n'est pas disponible
                    logger.warning("Email not found for OAuth user from provider: " + provider);
                    email = "user_" + providerId + "@" + provider + ".user";
                }
            }

            logger.info("Processing OAuth2 user: email=" + email + ", provider=" + provider);

            // Vérifier si l'utilisateur existe déjà
            Optional<User> userOptional = null;
            if (providerId != null) {
                userOptional = userRepository.findByInfoUser_ProviderAndInfoUser_ProviderId(provider, providerId);
            }

            if (userOptional == null || userOptional.isEmpty()) {
                // Vérifier par email aussi (au cas où l'utilisateur existe avec un autre provider)
                userOptional = userRepository.findByInfoUser_Email(email);
            }

            if (userOptional != null && userOptional.isPresent()) {
                // L'utilisateur existe déjà
                User existingUser = userOptional.get();
                logger.info("Existing user found: " + existingUser.getInfoUser().getUsername());

                // Si l'utilisateur existe mais avec un autre provider, on peut mettre à jour les infos
                if (providerId != null && !provider.equals(existingUser.getInfoUser().getProvider())) {
                    existingUser.getInfoUser().setProvider(provider);
                    existingUser.getInfoUser().setProviderId(providerId);
                    userRepository.save(existingUser);
                    logger.info("Updated provider info for user: " + existingUser.getInfoUser().getUsername());
                }
            } else {
                // Créer un nouvel utilisateur
                logger.info("Creating new user for OAuth2 login");
                InfoUser infoUser = new InfoUser();
                infoUser.setEmail(email);
                infoUser.setUsername(name != null ? name : email.split("@")[0]);
                infoUser.setProvider(provider);
                infoUser.setProviderId(providerId);
                // Mot de passe null pour les utilisateurs OAuth2

                User newUser = new User();
                newUser.setInfoUser(infoUser);
                newUser.setConfirmed(true); // Avec OAuth2, on considère que l'email est confirmé

                // Pour Github, on peut récupérer l'URL de l'avatar
                if ("github".equals(provider)) {
                    String avatarUrl = oAuth2User.getAttribute("avatar_url");
                    if (avatarUrl != null) {
                        newUser.setPhotoUrl(avatarUrl);
                    }
                }

                // Lier l'infoUser au user
                infoUser.setUser(newUser);

                userRepository.save(newUser);
                logger.info("New user created: " + infoUser.getUsername());
            }

            return oAuth2User;

        } catch (Exception e) {
            logger.severe("Error in OAuth2 user processing: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("oauth2_authentication_error", e.getMessage(), null),
                    e
            );
        }
    }
}