package com.scm.contactmanager.config;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestMailServerConfig {

    @Value("${spring.mail.port:3025}")
    private int port;

    private GreenMail greenMail;

    @PostConstruct
    public void startMailServer() {
        // Use dynamic ports to avoid conflicts
        ServerSetup serverSetup = new ServerSetup(0, null, "smtp").dynamicPort(); 
        greenMail = new GreenMail(serverSetup);
        greenMail.start();

        // Override the mail port property for the test context
        System.setProperty("spring.mail.port", String.valueOf(greenMail.getSmtp().getPort()));
    }

    @PreDestroy
    public void stopMailServer() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }
}
