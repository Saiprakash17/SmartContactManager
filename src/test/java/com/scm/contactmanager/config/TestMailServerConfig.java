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
        greenMail = new GreenMail(new ServerSetup(port, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
        greenMail.start();
    }

    @PreDestroy
    public void stopMailServer() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }
}
