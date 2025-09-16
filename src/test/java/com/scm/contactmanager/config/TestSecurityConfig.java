package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    
    @Bean
    @Primary 
    public SecurityCustomUserDeatilsService userDetailsService(UserRepo userRepo) {
        return new SecurityCustomUserDeatilsService(userRepo);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/css/**", "/js/**", "/img/**"))
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/signup", "/login", "/css/**", "/js/**", "/img/**").permitAll()
                    .requestMatchers("/user/**").authenticated()
                    .anyRequest().permitAll()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticate")
                    .defaultSuccessUrl("/user/dashboard", true)
                    .usernameParameter("email")
                    .passwordParameter("password")
            )
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
                session.maximumSessions(1).maxSessionsPreventsLogin(true);
            });

        return http.build();
    }

    @Bean
    @Primary
    public AuthenticationProvider authenticationProvider(SecurityCustomUserDeatilsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}

