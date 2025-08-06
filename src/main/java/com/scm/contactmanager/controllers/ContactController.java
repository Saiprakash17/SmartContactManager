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
    // --- Declarations ---
    Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;




    @RequestMapping("/add")
    public String addContactView(Model model) {
        // Create a new ContactForm and set default favorite to true
        ContactForm contactForm = new ContactForm();
        contactForm.setFavorite(true);

        // Add the form to the model
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

        // Get user
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        // Form to address
        Address address = new Address();
        address.setStreet(contactForm.getStreet());
        address.setCity(contactForm.getCity());
        address.setState(contactForm.getState());
        address.setZipCode(contactForm.getZipCode());
        address.setCountry(contactForm.getCountry());

        // Form to contact
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
        contact.setRelationship(contactForm.getRelationship());

        // Process image
        if(contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            String fileName = UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(contactForm.getContactImage(), fileName);
            contact.setImageUrl(fileURL);
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
        // Get user
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        // Get all contacts by user id
        Page<Contact> contactsPage = contactService.getByUser(user, page, size, sortBy, direction);

        // Add attributes to the model
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
        // Log the search field and keyword
        logger.info("field {} keyword {}", contactSearchForm.getField(), contactSearchForm.getKeyword());

        // Get the user
        var user = userService.getUserByEmail(UserHelper.getEmailOfLoggedInUser(authentication));

        Page<Contact> contactsPage = null;

        // Determine which field to search by
        if (contactSearchForm.getField().equalsIgnoreCase("name")) {
            contactsPage = contactService.searchByName(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("email")) {
            contactsPage = contactService.searchByEmail(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("phone")) {
            contactsPage = contactService.searchByPhoneNumber(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("relationship")) {
            contactsPage = contactService.searchByRelationship(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
        } else {
            // Default to fetching all contacts if no specific field is selected
            contactsPage = contactService.getByUser(user, page, size, sortBy, direction);
            session.setAttribute("message",
                    Message.builder()
                            .content("Please select a field to search")
                            .type(MessageType.red)
                            .build());
        }

        // Log the result
        logger.info("contactsPage {}", contactsPage);

        // Add attributes to the model
        model.addAttribute("contactSearchForm", contactSearchForm);
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);

        return "user/view_contacts";
    }


    @RequestMapping("/delete/{contactId}")
    public String deleteContact(@PathVariable("contactId") String contactId, HttpSession session) {
        // Parse the contact ID
        Long contactIdLong = Long.parseLong(contactId);

        // Delete the contact
        contactService.deleteContact(contactIdLong);

        // Set success message
        session.setAttribute("message",
                Message.builder()
                        .content("Contact deleted successfully")
                        .type(MessageType.green)
                        .build());

        return "redirect:/user/contacts/view";
    }


    @RequestMapping("/edit/{contactId}")
    public String editContact(@PathVariable("contactId") String contactId, Model model) {
        // Parse the contact ID
        Long contactIdLong = Long.parseLong(contactId);

        // Get the contact by ID
        Contact contact = contactService.getContactById(contactIdLong);

        // Create and populate the ContactForm
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
        contactForm.setRelationship(contact.getRelationship());

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
        // Validate the form
        if (result.hasErrors()) {
            System.out.println("Errors in form");
            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());
            return "user/edit_contact";
        }

        // Prepare address
        Address address = new Address();
        address.setStreet(contactForm.getStreet());
        address.setCity(contactForm.getCity());
        address.setState(contactForm.getState());
        address.setZipCode(contactForm.getZipCode());
        address.setCountry(contactForm.getCountry());

        // Get and update contact
        Contact contact = contactService.getContactById(Long.parseLong(contactId));
        contact.setName(contactForm.getName());
        contact.setEmail(contactForm.getEmail());
        contact.setPhoneNumber(contactForm.getPhoneNumber());
        contact.setFavorite(contactForm.isFavorite());
        contact.setAddress(address);
        contact.setAbout(contactForm.getDescription());
        contact.setLinkedin(contactForm.getLinkedInLink());
        contact.setWebsite(contactForm.getWebsiteLink());
        contact.setRelationship(contactForm.getRelationship());

        // Process image if provided
        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            String fileName = UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(contactForm.getContactImage(), fileName);
            contact.setImageUrl(fileURL);
            contact.setCloudinaryImagePublicId(fileName);
            System.out.println("Image uploaded successfully: " + fileURL);
        }
        else {
            logger.info("Image not uploaded, using existing image");
        }

        // Update the contact
        Contact updatedContact = contactService.updateContact(contact);
        logger.info("Contact updated: " + updatedContact);

        // Set success message
        session.setAttribute("message",
                Message.builder()
                        .content("You have successfully updated the contact")
                        .type(MessageType.green)
                        .build());

        return "redirect:/user/contacts/view";
    }



    // Search favorite contacts
    @RequestMapping(value = "/favorites", method = RequestMethod.POST)
    public String searchFavoriteContacts(
            @ModelAttribute ContactsSearchForm contactSearchForm,
            @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model,
            Authentication authentication,
            HttpSession session
    ) {
        // Get the logged-in user's username
        String username = UserHelper.getEmailOfLoggedInUser(authentication);

        // Fetch the user entity
        User user = userService.getUserByEmail(username);

        Page<Contact> contactsPage = null;

        // Check if both field and keyword are provided for searching
        if (contactSearchForm.getField() != null && !contactSearchForm.getField().isEmpty() && contactSearchForm.getKeyword() != null && !contactSearchForm.getKeyword().isEmpty()) {

            // Switch based on the selected field for searching favorite contacts
            switch (contactSearchForm.getField().toLowerCase()) {
                case "name":
                    contactsPage = contactService.searchFavoriteByName(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
                    break;

                case "email":
                    contactsPage = contactService.searchFavoriteByEmail(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
                    break;

                case "phone":
                    contactsPage = contactService.searchFavoriteByPhoneNumber(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
                    break;

                case "relationship":
                    contactsPage = contactService.searchFavoriteByRelationship(contactSearchForm.getKeyword(), size, page, sortBy, direction, user);
                    break;

                default:
                    contactsPage = contactService.getFavoriteContactsByUser(user, page, size, sortBy, direction);
                    session.setAttribute("message",
                            Message.builder()
                                    .content("Please select a valid field to search")
                                    .type(MessageType.red)
                                    .build());
            }

        } else {

            // If no field or keyword, return all favorite contacts and show a message
            contactsPage = contactService.getFavoriteContactsByUser(user, page, size, sortBy, direction);
            session.setAttribute("message",
                    Message.builder()
                            .content("Please select a field and enter a keyword to search")
                            .type(MessageType.red)
                            .build());
        }

        // Add attributes to the model for rendering in the view
        model.addAttribute("contactSearchForm", contactSearchForm);
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);
        model.addAttribute("user", user);
        model.addAttribute("favoriteView", true);

        return "user/view_favorite_contacts";
    }

    // View favorite contacts page
    @RequestMapping("/favorites")
    public String viewFavoriteContacts(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
                                       @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
                                       @RequestParam(value = "direction", defaultValue = "asc") String direction,
                                       Model model, Authentication authentication) {
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
        Page<Contact> contactsPage = contactService.getFavoriteContactsByUser(user, page, size, sortBy, direction);
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);
        model.addAttribute("user", user);
        model.addAttribute("favoriteView", true);
        model.addAttribute("contactSearchForm", new ContactsSearchForm());
        return "user/view_favorite_contacts";
    }
}
