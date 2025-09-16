package com.scm.contactmanager.helper;

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
