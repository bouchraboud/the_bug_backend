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

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub"); // Google's unique ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Vérifier si l'utilisateur existe déjà
        Optional<User> userOptional = userRepository.findByInfoUser_ProviderAndInfoUser_ProviderId(provider, providerId);

        if (userOptional.isPresent()) {
            // L'utilisateur existe déjà, vous pouvez mettre à jour ses informations si nécessaire
            return oAuth2User;
        } else {
            // Créer un nouvel utilisateur
            InfoUser infoUser = new InfoUser();
            infoUser.setEmail(email);
            infoUser.setUsername(email.split("@")[0]); // Utilisez une partie de l'email comme nom d'utilisateur par défaut
            infoUser.setProvider(provider);
            infoUser.setProviderId(providerId);
            // Mot de passe null pour les utilisateurs Google

            User newUser = new User();
            newUser.setInfoUser(infoUser);
            newUser.setConfirmed(true); // Avec Google, on peut considérer que l'email est confirmé

            // Lier l'infoUser au user
            infoUser.setUser(newUser);

            userRepository.save(newUser);

            return oAuth2User;
        }
    }
}