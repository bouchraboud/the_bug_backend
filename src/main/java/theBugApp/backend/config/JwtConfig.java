package theBugApp.backend.config;


import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;


import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
@Configuration
public class JwtConfig {

    private static final Logger logger = Logger.getLogger(JwtConfig.class.getName());

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA512");

        logger.info("Configuring JwtEncoder with HmacSHA512 algorithm");

        // Construire une clé JWK
        JWK jwk = new OctetSequenceKey.Builder(secretKeySpec)
                .algorithm(JWSAlgorithm.HS512)
                .build();

        // Construire le JWKSet et le JWKSource
        JWKSet jwkSet = new JWKSet(jwk);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        // Utiliser NimbusJwtEncoder avec la source JWK
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA512");

        logger.info("Configuring JwtDecoder with HmacSHA512 algorithm");

        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}
