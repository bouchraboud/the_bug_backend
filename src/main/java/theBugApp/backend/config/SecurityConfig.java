package theBugApp.backend.config;




import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          @Value("${jwt.secret}") String secretKey) {
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain answersSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers(matchers ->
                        matchers.requestMatchers("/api/answers", "/api/answers/**"))
                .authorizeHttpRequests(auth -> auth
                        // Secure POST endpoints (create answers)
                        .requestMatchers(HttpMethod.POST, "/api/answers").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/answers/*/accept").authenticated()
                        // Public GET endpoints (view answers)
                        .requestMatchers(HttpMethod.GET, "/api/answers/question/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/answers/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/answers/user/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/answers/*/voters").permitAll()

                        // Secure all other endpoints
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/questions/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/questions").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/questions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions/*/voters").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/tags/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.disable());
        return http.build();
    }
    @Bean
    @Order(4)
    public SecurityFilterChain votesSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/votes/**")
                .authorizeHttpRequests(auth -> auth
                        // Tous les endpoints de vote nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }
    @Bean
    @Order(5) // Ou un ordre approprié
    public SecurityFilterChain usersSecurityFilterChain(HttpSecurity http) throws Exception {
        http

                .securityMatchers(matchers ->
                        matchers.requestMatchers("api/users", "api/users/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/questions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/answers").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/users").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/exchange-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/is-following/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/followers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/following").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/follow-stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "api/users/follow/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "api/users/unfollow/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "api/users/profile/**").authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }
    @Bean
    @Order(6)
    public SecurityFilterChain commentsSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/comments/**")
                .authorizeHttpRequests(auth -> auth
                        // POST requests require authentication (to add comments)
                        .requestMatchers(HttpMethod.POST, "/api/comments/**").authenticated()

                        // GET requests to fetch comments are public
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()

                        // Any other request under /api/comments requires authentication by default
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }
    @Bean
    @Order(7)
    public SecurityFilterChain followSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/follow/**")
                .authorizeHttpRequests(auth -> auth
                        // All follow endpoints require authentication
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }
    @Bean
    @Order(8)
    public SecurityFilterChain notificationSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/notifications/**")
                .authorizeHttpRequests(auth -> auth
                        // All follow endpoints require authentication
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }
    @Bean
    @Order(9)
    public SecurityFilterChain reputationSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/reputation/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/reputation/users/history").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reputation/users/*/privileges").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reputation/users/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reputation/users/daily-limit").authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
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
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Tu peux aussi définir un converter personnalisé ici si besoin
        return converter;
    }


}