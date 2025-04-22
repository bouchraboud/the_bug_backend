package theBugApp.backend.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByInfoUser_Email(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé pour cet email :: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getInfoUser().getEmail(),
                user.getInfoUser().getPassword(),
                user.isConfirmed(), // compte activé seulement si email confirmé
                true, // compte non expiré
                true, // identifiants non expirés
                true, // compte non verrouillé
                getAuthorities("USER")
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
