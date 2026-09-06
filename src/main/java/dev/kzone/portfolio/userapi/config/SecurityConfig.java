package dev.kzone.portfolio.userapi.config;

import dev.kzone.portfolio.userapi.security.RestAccessDeniedHandler;
import dev.kzone.portfolio.userapi.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    public static final String DEMO_ADMIN_USERNAME = "demo-admin";
    public static final String DEMO_ADMIN_PASSWORD = "admin-demo";
    public static final String DEMO_STAFF_USERNAME = "demo-staff";
    public static final String DEMO_STAFF_PASSWORD = "staff-demo";

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername(DEMO_ADMIN_USERNAME)
                .password(passwordEncoder.encode(DEMO_ADMIN_PASSWORD))
                .roles("ADMIN", "STAFF")
                .build();
        UserDetails staff = User.withUsername(DEMO_STAFF_USERNAME)
                .password(passwordEncoder.encode(DEMO_STAFF_PASSWORD))
                .roles("STAFF")
                .build();
        return new InMemoryUserDetailsManager(admin, staff);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/work-orders/*/approval-decision").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().permitAll()
                )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
