package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Import(CommonTestConfig.class)
@org.springframework.context.annotation.Profile("test")
public class TestSecurityConfig {
    
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        return resolver;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionStrategy = 
            new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionStrategy.setMaximumSessions(1);
        concurrentSessionStrategy.setExceptionIfMaximumExceeded(true);

        SessionFixationProtectionStrategy sessionFixationStrategy = new SessionFixationProtectionStrategy();
        sessionFixationStrategy.setMigrateSessionAttributes(true);

        RegisterSessionAuthenticationStrategy registerSessionStrategy = 
            new RegisterSessionAuthenticationStrategy(sessionRegistry);

        return new CompositeSessionAuthenticationStrategy(Arrays.asList(
            concurrentSessionStrategy,
            sessionFixationStrategy,
            registerSessionStrategy
        ));
    }

    private static final String[] PUBLIC_URLS = {
        "/", "/about", "/signup", "/login", "/css/**", "/js/**", "/img/**",
        "/home", "/register", "/authenticate", "/h2-console/**", "/user/contacts/decode-qr",
        "/services", "/contact", "/forgot-password", "/reset-password"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(PUBLIC_URLS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers("/api/**").hasRole("USER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .defaultSuccessUrl("/user/dashboard")
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired=true")
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
                .expiredUrl("/login?expired=true")
                .sessionRegistry(sessionRegistry)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> 
                    response.sendRedirect("/login"))
                .accessDeniedHandler((request, response, accessDeniedException) -> 
                    response.sendError(403, "Access Denied"))
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
    @Primary
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.builder()
                .username("test@example.com")
                .password(passwordEncoder.encode("testpassword"))
                .roles("USER")
                .build(),
            User.builder()
                .username("admin@example.com")
                .password(passwordEncoder.encode("testpassword"))
                .roles("USER", "ADMIN")
                .build()
        );
    }
}