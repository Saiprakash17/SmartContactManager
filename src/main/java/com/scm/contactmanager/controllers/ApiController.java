package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.services.ContactService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ContactService contactService;

    //get contact by id
    @RequestMapping("/contact/{id}")
    public Contact getContact(@PathVariable String id) {
        System.out.println("Contact ID: " + id);
        return contactService.getContactById(Long.parseLong(id));
    }
}
