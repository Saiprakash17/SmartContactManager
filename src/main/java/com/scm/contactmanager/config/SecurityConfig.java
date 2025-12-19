package com.scm.contactmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@org.springframework.context.annotation.Profile("!test")
public class SecurityConfig {

    private final SecurityCustomUserDeatilsService userDetailsService;
    private final AuthFailtureHandler authFailtureHandler;

    public SecurityConfig(SecurityCustomUserDeatilsService userDetailsService,
                         AuthFailtureHandler authFailtureHandler) {
        this.userDetailsService = userDetailsService;
        this.authFailtureHandler = authFailtureHandler;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // Configure authentication provider
            .authenticationProvider(daoAuthenticationProvider())
            // Configure CSRF protection
            .csrf(csrf -> csrf.ignoringRequestMatchers("/css/**", "/js/**", "/img/**", "/api/**"))
            // Configure authorization rules
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/signup", "/login", "/css/**", "/js/**", "/img/**").permitAll()
                    .requestMatchers("/user/**").authenticated()
                    .anyRequest().permitAll()
            )
            // Configure login
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticate")
                    .defaultSuccessUrl("/user/dashboard", true)
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .failureHandler(authFailtureHandler)
            )
            // Configure logout
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            )
            // Configure session management
            .sessionManagement(session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .invalidSessionUrl("http://localhost/login?expired=true")
                    .sessionFixation().newSession()
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                    .expiredUrl("http://localhost/login?expired=true")
            )
            // Configure security headers
            .headers(headers -> 
                headers
                    .httpStrictTransportSecurity(hsts -> 
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000)
                    )
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives(
                            "default-src 'self'; " +
                            "img-src 'self' data: https: blob:; " +
                            "script-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://unpkg.com; " +
                            "style-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                            "connect-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https:; " +
                            "font-src 'self' data: https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                            "worker-src 'self' blob:; " +
                            "frame-src 'self'; " +
                            "object-src 'none'; " +
                            "base-uri 'self'; " +
                            "form-action 'self'; " +
                            "upgrade-insecure-requests;"
                        )
                    )
                    .frameOptions(frame -> frame.deny())
                    .xssProtection()
            );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
