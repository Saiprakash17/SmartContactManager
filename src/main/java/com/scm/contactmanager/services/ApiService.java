package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.Contact;
import java.util.List;

public interface ApiService {
    Contact getContact(String id);
    List<Contact> searchContacts(String keyword, int page, int size);
    Contact toggleFavorite(String id);
    Long validateAndParseContactId(String id);
}