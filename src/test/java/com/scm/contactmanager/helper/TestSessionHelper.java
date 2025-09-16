package com.scm.contactmanager.helper;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("sessionHelper")
@Primary
public class TestSessionHelper extends SessionHelper {
    @Override
    public void removeMessage() {
        // No-op for test
    }

    @Override
    public String getMessage() {
        return "Test Message";
    }

    @Override
    public void setMessage(String message) {
        // No-op for test
    }
}
