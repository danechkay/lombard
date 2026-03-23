package ru.lombard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/catalog", "/catalog/**", "/product/**", "/login", "/register", "/css/**", "/js/**", "/uploads/**", "/images/**").permitAll()
                        .requestMatchers("/api/catalog/**", "/api/auth/register", "/api/auth/me").permitAll()
                        .requestMatchers("/api/admin/users/**", "/api/admin/orders/**", "/api/admin/settings/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/cart/**", "/api/order/**", "/api/account/**", "/api/auth/logout").authenticated()
                        .requestMatchers("/admin/users/**", "/admin/orders/**", "/admin/settings/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/cart/**", "/order/**", "/account/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); // если понадобится API
        return http.build();
    }
}
