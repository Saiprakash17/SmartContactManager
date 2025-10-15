package com.scm.contactmanager.config;



import org.springframework.boot.test.context.TestConfiguration;import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Primary;import org.springframework.context.annotation.Primary;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.User;import org.springframework.security.core.userdetails.User;

import org.springframework.security.core.userdetails.UserDetailsService;import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.SecurityFilterChain;



@TestConfigurationimport java.util.ArrayList;

@EnableWebSecurity

@EnableMethodSecurity@TestConfiguration

public class TestSecurityConfig {@EnableWebSecurity

    private static final String[] PUBLIC_URLS = {@EnableMethodSecurity

        "/", "/about", "/signup", "/login", "/css/**", "/js/**", "/img/**",public class TestSecurityConfig {

        "/index", "/register", "/authenticate", "/h2-console/**"    private static final String[] PUBLIC_URLS = {

    };        "/", "/about", "/signup", "/login", "/css/**", "/js/**", "/img/**",

            "/index", "/register", "/authenticate", "/h2-console/**"

    @Bean    };

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {    

        http.csrf(csrf -> csrf.ignoringRequestMatchers(PUBLIC_URLS))    @Bean

                .authorizeHttpRequests(authorize -> authorize    public static org.springframework.context.support.PropertySourcesPlaceholderConfigurer propertiesResolver() {

                        .requestMatchers(PUBLIC_URLS).permitAll()        return new org.springframework.context.support.PropertySourcesPlaceholderConfigurer();

                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")    }

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()    @Bean

                )    @Primary

                .formLogin(form -> form    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

                        .loginPage("/login")        return new InMemoryUserDetailsManager(

                        .loginProcessingUrl("/authenticate")            org.springframework.security.core.userdetails.User.builder()

                        .defaultSuccessUrl("/user/index")                .username("test@example.com")

                        .failureUrl("/login?error=true")                .password(passwordEncoder.encode("testpassword"))

                        .permitAll()                .roles("USER")

                )                .build(),

                .logout(logout -> logout            org.springframework.security.core.userdetails.User.builder()

                        .logoutUrl("/logout")                .username("admin@example.com")

                        .logoutSuccessUrl("/login?logout")                .password(passwordEncoder.encode("admin"))

                        .deleteCookies("JSESSIONID")                .roles("USER", "ADMIN")

                        .permitAll()                .build()

                )        );

                .headers(headers -> {    }

                    headers.addHeaderWriter((request, response) -> {

                        response.setHeader("X-Frame-Options", "SAMEORIGIN");    @Bean

                        response.setHeader("X-Content-Type-Options", "nosniff");    @Primary

                        response.setHeader("X-XSS-Protection", "1; mode=block");    public PasswordEncoder passwordEncoder() {

                        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");        return new BCryptPasswordEncoder();

                        response.setHeader("Content-Security-Policy",     }

                            "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");

                    });    @Bean

                })public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                .sessionManagement(session -> session    http.csrf(csrf -> csrf.ignoringRequestMatchers(PUBLIC_URLS))

                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)            .authorizeHttpRequests(authorize -> authorize

                );                    .requestMatchers(PUBLIC_URLS).permitAll()

                    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")

        return http.build();                    .requestMatchers("/admin/**").hasRole("ADMIN")

    }                    .anyRequest().authenticated()

            )

    @Bean            .formLogin(form -> form

    @Primary                    .loginPage("/login")

    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {                    .loginProcessingUrl("/authenticate")

        return new InMemoryUserDetailsManager(                    .defaultSuccessUrl("/user/index")

            User.builder()                    .failureUrl("/login?error=true")

                .username("test@example.com")                    .permitAll()

                .password(passwordEncoder.encode("testpassword"))            )

                .roles("USER")            .logout(logout -> logout

                .build(),                    .logoutUrl("/logout")

            User.builder()                    .logoutSuccessUrl("/login?logout")

                .username("admin@example.com")                    .deleteCookies("JSESSIONID")

                .password(passwordEncoder.encode("admin"))                    .permitAll()

                .roles("USER", "ADMIN")            )

                .build()            .headers(headers -> {

        );                headers.addHeaderWriter((request, response) -> {

    }                    response.setHeader("X-Frame-Options", "SAMEORIGIN");

                    response.setHeader("X-Content-Type-Options", "nosniff");

    @Bean                    response.setHeader("X-XSS-Protection", "1; mode=block");

    @Primary                    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

    public PasswordEncoder passwordEncoder() {                    response.setHeader("Content-Security-Policy", 

        return new BCryptPasswordEncoder();                        "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");

    }                });

}            })
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

    return http.build();
}

    @Bean
    @Primary
    public Cloudinary cloudinary() {
        return new Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "test_cloud",
                "api_key", "test_key",
                "api_secret", "test_secret"
            )
        );
    }

    @Bean
    @Primary
    public ContactService contactService() {
        ContactService contactService = mock(ContactService.class);
        Page<Contact> emptyPage = new PageImpl<>(java.util.Collections.emptyList());
        when(contactService.getByUser(any(User.class), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(emptyPage);
        return contactService;
    }

    @Bean
    @Primary
    public UserService userService(PasswordEncoder passwordEncoder) {
        UserService userService = mock(UserService.class);
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword(passwordEncoder.encode("testpass"));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(userService.getUserByEmail(anyString())).thenReturn(null);
        return userService;
    }
}
