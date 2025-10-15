package com.scm.contactmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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

    @Autowired
    private SecurityCustomUserDeatilsService userDetailsService;

    @Autowired
    private AuthFailtureHandler authFailtureHandler;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // Configure CSRF protection
            .csrf(csrf -> csrf.ignoringRequestMatchers("/css/**", "/js/**", "/img/**", "/user/contacts/decode-qr"))
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
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                      .invalidSessionUrl("/login?expired=true")
                      .maximumSessions(1)
                      .maxSessionsPreventsLogin(true)
                      .expiredUrl("/login?expired=true")
            )
            // Configure security headers
            .headers(headers -> 
                headers
                    .xssProtection(xss -> {})
                    .contentTypeOptions(cto -> {})
                    .frameOptions(frame -> frame.deny())
                    .httpStrictTransportSecurity(hsts -> 
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000)
                    )
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives(
                            "default-src 'self'; " +
                            "img-src 'self' data: https: http: blob:; " +
                            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://unpkg.com; " +
                            "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                            "connect-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https:; " +
                            "font-src 'self' data: https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                            "worker-src 'self' blob:; " +
                            "frame-src 'self'; " +
                            "script-src-elem 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://unpkg.com;"
                        )
                    )
            );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
