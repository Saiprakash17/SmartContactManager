package com.scm.contactmanager.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ContactForm;
import com.scm.contactmanager.forms.ContactsSearchForm;
import com.scm.contactmanager.helper.AppConstants;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.helper.UserHelper;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("user/contacts")
public class ContactController {

    Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @RequestMapping("/add")
    public String addContactView(Model model) {
        ContactForm contactForm = new ContactForm();

        contactForm.setFavorite(true);
        model.addAttribute("contactForm", contactForm);
        return "user/add_contact";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String saveContact(@Valid @ModelAttribute ContactForm contactForm,
            BindingResult result,
            Authentication authentication,
            HttpSession session) {
        // Logic to save the contact

        if (result.hasErrors()) {
            System.out.println("Errors in form");
            // Handle the errors
            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());

            return "user/add_contact";
        }

        //get user
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        //Form to address
        Address address = new Address();
        address.setStreet(contactForm.getStreet());
        address.setCity(contactForm.getCity());
        address.setState(contactForm.getState());
        address.setZipCode(contactForm.getZipCode());
        address.setCountry(contactForm.getCountry());

        //Form to contact
        Contact contact = new Contact();
        contact.setName(contactForm.getName());
        contact.setEmail(contactForm.getEmail());
        contact.setPhoneNumber(contactForm.getPhoneNumber());
        contact.setFavorite(contactForm.isFavorite());
        contact.setAddress(address);
        contact.setAbout(contactForm.getDescription());
        contact.setLinkedin(contactForm.getLinkedInLink());
        contact.setWebsite(contactForm.getWebsiteLink());
        contact.setUser(user);

        // Process image
        // Check if the image is not null and not empty
        if(contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            // Generate a unique file name using UUID
            String fileName = UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(contactForm.getContactImage(), fileName);
            // Set the image URL in the contact object
            contact.setImageUrl(fileURL);
            // Set the public ID for the image in Cloudinary
            contact.setCloudinaryImagePublicId(fileName);
            System.out.println("Image uploaded successfully: " + fileURL);
        }
        
        // Save the contact
        Contact savedContact = contactService.saveContact(contact);
        System.out.println("Contact saved: " + savedContact);

        session.setAttribute("message",
                Message.builder()
                        .content("You have successfully added a new contact")
                        .type(MessageType.green)
                        .build());
        return "redirect:/user/contacts/add";
    }

    @RequestMapping("/view")
    public String viewContacts(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication){

        //get user
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        //get all contacts by user id
        Page<Contact> contactsPage = contactService.getByUser(user, page, size, sortBy, direction);
    
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);
        model.addAttribute("user", user);
        model.addAttribute("contactSearchForm", new ContactsSearchForm());
        return "user/view_contacts";
    }

    @RequestMapping("/search")
    public String SearchHandler(
        @ModelAttribute ContactsSearchForm contactSearchForm,
        @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
        @RequestParam(value = "direction", defaultValue = "asc") String direction,
        Model model,
        Authentication authentication,
        HttpSession session
    ) {
        logger.info("field {} keyword {}", contactSearchForm.getField(), contactSearchForm.getKeyword());

        var user = userService.getUserByEmail(UserHelper.getEmailOfLoggedInUser(authentication));

        Page<Contact> contactsPage = null;
        if (contactSearchForm.getField().equalsIgnoreCase("name")) {
            contactsPage = contactService.searchByName(contactSearchForm.getKeyword(), size, page, sortBy, direction,
                    user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("email")) {
            contactsPage = contactService.searchByEmail(contactSearchForm.getKeyword(), size, page, sortBy, direction,
                    user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("phone")) {
            contactsPage = contactService.searchByPhoneNumber(contactSearchForm.getKeyword(), size, page, sortBy,
                    direction, user);
        } else {
            // Default to fetching all contacts if no specific field is selected
            contactsPage = contactService.getByUser(user, page, size, sortBy, direction);
            session.setAttribute("message",
                    Message.builder()
                            .content("Please select a field to search")
                            .type(MessageType.red)
                            .build());
        }

        logger.info("contactsPage {}", contactsPage);

        model.addAttribute("contactSearchForm", contactSearchForm);

        model.addAttribute("contactsPage", contactsPage);

        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);

        return "user/view_contacts";
    }

    @RequestMapping("/delete/{contactId}")
    public String deleteContact(@PathVariable("contactId") String contactId, HttpSession session) {
        // Delete the contact
        Long contactIdLong = Long.parseLong(contactId);
        contactService.deleteContact(contactIdLong);
        session.setAttribute("message",
                Message.builder()
                        .content("Contact deleted successfully")
                        .type(MessageType.green)
                        .build());
        return "redirect:/user/contacts/view";
    }

    @RequestMapping("/edit/{contactId}")
    public String editContact(@PathVariable("contactId") String contactId, Model model) {
        // Get the contact by ID
        Long contactIdLong = Long.parseLong(contactId);
        Contact contact = contactService.getContactById(contactIdLong);

        // Create a ContactForm object and populate it with the contact's data
        ContactForm contactForm = new ContactForm();
        contactForm.setName(contact.getName());
        contactForm.setEmail(contact.getEmail());
        contactForm.setPhoneNumber(contact.getPhoneNumber());
        contactForm.setStreet(contact.getAddress().getStreet());
        contactForm.setCity(contact.getAddress().getCity());
        contactForm.setState(contact.getAddress().getState());
        contactForm.setZipCode(contact.getAddress().getZipCode());
        contactForm.setCountry(contact.getAddress().getCountry());
        contactForm.setDescription(contact.getAbout());
        contactForm.setLinkedInLink(contact.getLinkedin());
        contactForm.setWebsiteLink(contact.getWebsite());
        contactForm.setPicture(contact.getImageUrl());

        // Add the ContactForm object to the model
        model.addAttribute("contactId", contactIdLong);
        model.addAttribute("contactForm", contactForm);
        return "user/edit_contact";
    }

    @RequestMapping(value = "/update_contact/{contactId}", method = RequestMethod.POST)
    public String updateContact(@PathVariable("contactId") String contactId,
            @Valid @ModelAttribute ContactForm contactForm,
            BindingResult result,
            Authentication authentication,
            HttpSession session) {
        // Logic to update the contact

        if (result.hasErrors()) {
            System.out.println("Errors in form");
            // Handle the errors
            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());

            return "user/edit_contact";
        }

        //get user
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        //Form to address
        Address address = new Address();
        address.setStreet(contactForm.getStreet());
        address.setCity(contactForm.getCity());
        address.setState(contactForm.getState());
        address.setZipCode(contactForm.getZipCode());
        address.setCountry(contactForm.getCountry());

        //Form to contact
        Contact contact = contactService.getContactById(Long.parseLong(contactId));
        contact.setName(contactForm.getName());
        contact.setEmail(contactForm.getEmail());
        contact.setPhoneNumber(contactForm.getPhoneNumber());
        contact.setFavorite(contactForm.isFavorite());
        contact.setAddress(address);
        contact.setAbout(contactForm.getDescription());
        contact.setLinkedin(contactForm.getLinkedInLink());
        contact.setWebsite(contactForm.getWebsiteLink());
        

        // Process image
        // Check if the image is not null and not empty
        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            // Generate a unique file name using UUID
            String fileName = UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(contactForm.getContactImage(), fileName);
            // Set the image URL in the contact object
            contact.setImageUrl(fileURL);
            // Set the public ID for the image in Cloudinary
            contact.setCloudinaryImagePublicId(fileName);
            System.out.println("Image uploaded successfully: " + fileURL);
        }
        else{
            logger.info("Image not uploaded, using existing image");
        }

        // Update the contact
        Contact updatedContact = contactService.updateContact(contact);

        logger.info("Contact updated: " + updatedContact);

        session.setAttribute("message",
                Message.builder()
                        .content("You have successfully updated the contact")
                        .type(MessageType.green)
                        .build());
        return "redirect:/user/contacts/view";
    }
}
