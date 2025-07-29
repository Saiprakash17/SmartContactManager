package com.scm.contactmanager.entities;

public enum Relationship {
    FAMILY("Family"),
    FRIEND("Friend"),
    RELATIVE("Relative"),
    COLLEAGUE("Colleague"),
    CLASSMATE("Classmate"),
    OTHER("Other");

    private final String label;

    Relationship(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
} 