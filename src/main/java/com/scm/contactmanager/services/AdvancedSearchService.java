package com.scm.contactmanager.services;

import org.springframework.data.domain.Page;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.AdvancedSearchCriteria;

public interface AdvancedSearchService {
    Page<Contact> search(AdvancedSearchCriteria criteria, User user);
}
