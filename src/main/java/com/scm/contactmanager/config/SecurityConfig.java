package com.scm.contactmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

@Configuration
public class SecurityConfig {

    @Autowired
    private SecurityCustomUserDeatilsService userDetailsService;

    @Autowired
    private OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler;

    @Autowired
    private AuthFailtureHandler authFailtureHandler;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers("/user/**").authenticated()
                        .anyRequest().permitAll()
        );

        httpSecurity.formLogin(formLogin ->
                formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/authenticate")
                        .defaultSuccessUrl("/user/dashboard", true)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .failureHandler(authFailtureHandler)
        );

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.logout(logout ->
                logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
        );


        httpSecurity.oauth2Login(oauth ->{
            oauth.loginPage("/login");
            oauth.successHandler(oAuthAuthenticationSuccessHandler);
        });

        return httpSecurity.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
