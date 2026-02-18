package com.scm.contactmanager.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactTag;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.repositories.ContactTagRepo;
import com.scm.contactmanager.services.ContactTagService;

@Service
@Transactional
public class ContactTagServiceImpl implements ContactTagService {

    @Autowired
    private ContactTagRepo tagRepository;

    @Autowired
    private ContactRepo contactRepository;

    @Override
    public ContactTag createTag(String name, String color, String description, User user) {
        if (tagRepository.existsByNameAndUser(name, user)) {
            throw new IllegalArgumentException("Tag already exists: " + name);
        }

        ContactTag tag = ContactTag.builder()
            .name(name)
            .color(color)
            .description(description)
            .user(user)
            .build();

        return tagRepository.save(tag);
    }

    @Override
    public void addTagToContact(Long contactId, Long tagId, User user) {
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));

        ContactTag tag = tagRepository.findByIdAndUser(tagId, user)
            .orElseThrow(() -> new RuntimeException("Tag not found"));

        contact.getTags().add(tag);
        contactRepository.save(contact);
    }

    @Override
    public void removeTagFromContact(Long contactId, Long tagId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new RuntimeException("Contact not found"));

        contact.getTags().removeIf(tag -> tag.getId().equals(tagId));
        contactRepository.save(contact);
    }

    @Override
    public Page<Contact> getContactsByTag(Long tagId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findByTags_Id(tagId, pageable);
    }

    @Override
    public List<ContactTag> getUserTags(User user) {
        return tagRepository.findByUser(user);
    }

    @Override
    public void deleteTag(Long tagId, User user) {
        ContactTag tag = tagRepository.findByIdAndUser(tagId, user)
            .orElseThrow(() -> new RuntimeException("Tag not found"));

        tag.getContacts().clear();
        tagRepository.delete(tag);
    }

    @Override
    public void updateTag(Long tagId, String name, String color, String description, User user) {
        ContactTag tag = tagRepository.findByIdAndUser(tagId, user)
            .orElseThrow(() -> new RuntimeException("Tag not found"));

        tag.setName(name);
        tag.setColor(color);
        tag.setDescription(description);
        tagRepository.save(tag);
    }

    @Override
    public ContactTag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
    }

    @Override
    public boolean tagExists(Long tagId, User user) {
        return tagRepository.findByIdAndUser(tagId, user).isPresent();
    }
}
