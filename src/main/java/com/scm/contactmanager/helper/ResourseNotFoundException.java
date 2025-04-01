package com.scm.contactmanager.helper;

public class ResourseNotFoundException extends RuntimeException {

    public ResourseNotFoundException() {
        super("Resource not found");
    }

    public ResourseNotFoundException(String message) {
        super(message);
    }

}
