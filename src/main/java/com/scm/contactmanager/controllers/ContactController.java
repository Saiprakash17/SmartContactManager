package com.scm.contactmanager.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ContactForm;
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
}
