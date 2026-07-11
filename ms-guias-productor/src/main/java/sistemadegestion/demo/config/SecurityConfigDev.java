package sistemadegestion.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * SOLO PARA DESARROLLO LOCAL. Desactiva por completo la seguridad para poder
 * probar los endpoints de S3 (/guias/**) sin necesitar un JWT real de Azure
 * AD B2C. NUNCA se activa por defecto: solo si se corre con el perfil "dev".
 *
 *   mvn spring-boot:run -Dspring-boot.run.profiles=dev
 *   java -jar app.jar --spring.profiles.active=dev
 *   docker run -e SPRING_PROFILES_ACTIVE=dev ...
 *
 * En cualquier otro caso (sin perfil, prod, oracle, etc.) se usa el
 * SecurityConfig real con Azure AD B2C (@Profile("!dev") en esa clase).
 */
@Configuration
@Profile("dev")
public class SecurityConfigDev {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());
        return http.build();
    }
}
