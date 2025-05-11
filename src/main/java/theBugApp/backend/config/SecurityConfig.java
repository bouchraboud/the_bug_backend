package theBugApp.backend.config;




import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import theBugApp.backend.service.CustomOAuth2UserService;
import theBugApp.backend.service.UserDetailsServiceImpl;


import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@PropertySource("classpath:application.properties")
@Slf4j
public class SecurityConfig {


    private String secretKey;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          @Value("${jwt.secret}") String secretKey) {
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Configuring Security Filter Chain");

        return httpSecurity
                // ✅ Use session-based auth (important for OAuth2 success handler + @AuthenticationPrincipal)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 🔒 Disable CSRF for simplicity (in production, consider enabling it with proper config)
                .csrf(csrf -> csrf.disable())

                // 🌐 Enable CORS with default settings (customize as needed)
                .cors(Customizer.withDefaults())

                // 🔐 Authorization rules
                .authorizeHttpRequests(ar -> ar
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/password/forgot", "/password/reset").permitAll()
                        .requestMatchers("/register/users").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()


                        // Require authentication for all /api/** routes
                        .requestMatchers("/api/**").authenticated()

                        // Fallback: everything else requires authentication
                        .anyRequest().authenticated()
                )

                // 🌐 OAuth2 login setup
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            log.info("Authentication successful: {}", authentication.getName());
                            response.sendRedirect("/api/login/oauth2/success");
                        })
                        .failureHandler((request, response, exception) -> {
                            log.error("Authentication failed: {}", exception.getMessage());
                            response.sendRedirect("/auth/failure");
                        })
                )

                .build();
    }

    @Bean
    public AuthenticationManager authenticationManagerUser() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setExposedHeaders(List.of("x-auth-token"));

        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsSource;
    }


}
