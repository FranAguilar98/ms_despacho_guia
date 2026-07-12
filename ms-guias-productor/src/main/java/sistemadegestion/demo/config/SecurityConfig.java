package sistemadegestion.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!dev")
public class SecurityConfig {

    // Emisor de tokens de USUARIO (login interactivo / Authorization Code) vía policy B2C
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String b2cIssuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String b2cJwkSetUri;

    // Emisor de tokens APP-ONLY (Client Credentials), siempre vía AAD estándar,
    // nunca vía b2clogin.com/policy. Azure AD B2C no soporta client credentials
    // a través del user flow: el token siempre lo emite login.microsoftonline.com.
    @Value("${azure.tenant-id:bffd2098-bc19-42ec-9208-0941f3424faf}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                // .requestMatchers(HttpMethod.GET, "/guias/*/object").hasAnyRole("DESCARGA", "ADMIN")
                // .requestMatchers("/guias/**").hasRole("ADMIN")
                .requestMatchers("/guias/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationManagerResolver(authenticationManagerResolver())
            );
        return http.build();
    }

    /**
     * Resuelve dinámicamente qué JwtDecoder usar según el "iss" (issuer) que venga
     * en el token, sin verificar todavía la firma. Así soportamos en simultáneo:
     *  - Tokens de usuario (Authorization Code) emitidos por la policy B2C.
     *  - Tokens app-only (Client Credentials) emitidos por el tenant AAD estándar.
     */
    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        String aadIssuerUri = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
        String aadJwkSetUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";

        AuthenticationManager b2cManager = buildAuthenticationManager(b2cIssuerUri, b2cJwkSetUri);
        AuthenticationManager aadManager = buildAuthenticationManager(aadIssuerUri, aadJwkSetUri);

        Map<String, AuthenticationManager> managersByIssuer = Map.of(
            b2cIssuerUri, b2cManager,
            aadIssuerUri, aadManager
        );

        return new JwtIssuerAuthenticationManagerResolver(managersByIssuer::get);
    }

    private AuthenticationManager buildAuthenticationManager(String issuerUri, String jwkSetUri) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<>(
            JwtClaimNames.AUD,
            aud -> aud != null && aud.toString().contains(clientId)
        );
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));

        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
        return provider::authenticate;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Mismo enfoque que en el instructivo del docente: JwtGrantedAuthoritiesConverter
        // estándar, apuntando al nombre real del custom claim en Azure AD B2C.
        // Aunque el valor llega como string simple (ej: "ADMIN", no un array), Spring
        // lo soporta igual: si el claim es un String sin espacios, lo trata como
        // una lista de un solo elemento.
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("extension_rolAcceso");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
