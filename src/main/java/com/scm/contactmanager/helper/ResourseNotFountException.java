package com.scm.contactmanager.helper;

public class ResourseNotFountException extends RuntimeException {

    public ResourseNotFountException() {
        super("Resource not found");
    }

    public ResourseNotFountException(String message) {
        super(message);
    }

}
