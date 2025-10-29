package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.List;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Import(CommonTestConfig.class)
@org.springframework.context.annotation.Profile("test")
public class TestSecurityConfig {

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(List.of(
            new SessionFixationProtectionStrategy(),
            new RegisterSessionAuthenticationStrategy(sessionRegistry())
        ));
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    private static final String[] PUBLIC_URLS = {
        "/", "/about", "/signup", "/login", "/css/**", "/js/**", "/img/**",
        "/home", "/register", "/authenticate", "/h2-console/**", "/user/contacts/decode-qr",
        "/services", "/contact", "/forgot-password", "/reset-password"
    };

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/user/dashboard");
        handler.setTargetUrlParameter("continue");
        return handler;
    }

    @Bean 
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider,
                                         AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {
        http
            .authenticationProvider(authenticationProvider)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/css/**", "/js/**", "/img/**", "/user/contacts/decode-qr")
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticate")
                    .successHandler(authenticationSuccessHandler)
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .failureUrl("/login?error=true")
                    .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> {
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .invalidSessionUrl("/login?expired=true")
                    .sessionFixation(fix -> fix.newSession())
                    .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                    .maximumSessions(1)
                    .expiredUrl("/login?expired=true")
                    .maxSessionsPreventsLogin(false);
            })
            .securityContext(context -> context
                .requireExplicitSave(false)
            )
            .headers(headers -> 
                headers.defaultsDisabled()
                    .xssProtection(Customizer.withDefaults())
                    .cacheControl(Customizer.withDefaults())
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions(frame -> frame.deny())
                    .httpStrictTransportSecurity(hsts -> hsts
                        .maxAgeInSeconds(31536000)
                        .includeSubDomains(true))
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
                    ));
        
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username("test@example.com")
                .password(passwordEncoder.encode("testpassword"))
                .roles("USER")
                .build(),
            User.builder()
                .username("admin@example.com")
                .password(passwordEncoder.encode("admin"))
                .roles("USER", "ADMIN")
                .build()
        );
    }
}
