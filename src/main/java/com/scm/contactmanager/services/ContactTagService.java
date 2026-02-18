package com.scm.contactmanager.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactTag;
import com.scm.contactmanager.entities.User;

public interface ContactTagService {
    ContactTag createTag(String name, String color, String description, User user);

    void addTagToContact(Long contactId, Long tagId, User user);

    void removeTagFromContact(Long contactId, Long tagId);

    Page<Contact> getContactsByTag(Long tagId, int page, int size);

    List<ContactTag> getUserTags(User user);

    void deleteTag(Long tagId, User user);

    void updateTag(Long tagId, String name, String color, String description, User user);

    ContactTag getTagById(Long tagId);

    boolean tagExists(Long tagId, User user);
}
