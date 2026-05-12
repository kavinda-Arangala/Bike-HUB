package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.Security;

import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 * Package : security
 *
 * Configures Spring Security for the application.
 *
 * FOR DEVELOPMENT (current setup):
 *   All API endpoints are open — no login required.
 *   This lets you test with Postman and the frontend easily.
 *
 * FOR PRODUCTION:
 *   Replace permitAll() with role-based rules, e.g.:
 *   .requestMatchers(HttpMethod.POST, "/api/bikes").hasRole("OWNER")
 *   .requestMatchers(HttpMethod.GET,  "/api/bikes/**").permitAll()
 *
 * NOTE: If your project's User module handles JWT authentication,
 *       that team will update this file to add JWT filter chain.
 *       Just leave this as-is for now so the Bike module works standalone.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for REST APIs — CSRF is for form-based apps)
                .csrf(AbstractHttpConfigurer::disable)

                // Allow all requests without authentication (dev mode)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            boolean enabled = Boolean.TRUE.equals(user.getIsActive());
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .disabled(!enabled)
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
