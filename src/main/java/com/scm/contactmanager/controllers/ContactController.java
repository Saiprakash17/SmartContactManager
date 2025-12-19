package com.scm.contactmanager.controllers;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.Set;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ContactForm;
import com.scm.contactmanager.forms.ContactsSearchForm;
import com.scm.contactmanager.helper.AppConstants;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.helper.UserHelper;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
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
    private QRCodeGeneratorService qrCodeGeneratorService;

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
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

    if (!result.hasErrors()) {
        try {
            Contact savedContact = contactService.saveContact(contactForm, authentication);
            logger.info("Contact saved: {}", savedContact);
            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("You have successfully added a new contact")
                    .type(MessageType.green)
                    .build());
            return "redirect:/user/contacts/view";
        } catch (Exception e) {
            logger.error("Error saving contact", e);
            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("Error saving contact: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
        }
    }
    redirectAttributes.addFlashAttribute("message", Message.builder()
        .content("Please correct the errors and try again")
        .type(MessageType.red)
        .build());
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.contactForm", result);
        redirectAttributes.addFlashAttribute("contactForm", contactForm);
        return "redirect:/user/contacts/add";
    }
    


    @RequestMapping("/view")
    public String viewContacts(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
        @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
        @RequestParam(value = "direction", defaultValue = "asc") String direction,
        Model model, 
        Authentication authentication
    ) {
        // SECURITY: Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 50) {
            size = AppConstants.PAGE_SIZE;
        }
        
        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
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
        org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        User user = userService.getUserByEmail(UserHelper.getEmailOfLoggedInUser(authentication));
        
        // Validate search parameters
        if (contactSearchForm.getField() == null || contactSearchForm.getField().isEmpty() ||
            contactSearchForm.getKeyword() == null || contactSearchForm.getKeyword().isEmpty()) {
            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("Please select a field and enter a keyword to search")
                    .type(MessageType.red)
                    .build());
            return "redirect:/user/contacts/view";
        }
        
        // SECURITY: Validate sortBy - whitelist of allowed fields
        Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "email", "phoneNumber", "address", "about", "favorite", "createdAt", "updatedAt"
        );
        
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            logger.warn("Invalid sort field attempted: {}", sortBy);
            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("Invalid sort field")
                    .type(MessageType.red)
                    .build());
            return "redirect:/user/contacts/view";
        }
        
        // SECURITY: Validate direction - only asc or desc
        if (!direction.matches("^(asc|desc)$")) {
            logger.warn("Invalid sort direction attempted: {}", direction);
            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("Invalid sort direction")
                    .type(MessageType.red)
                    .build());
            return "redirect:/user/contacts/view";
        }
        
        // SECURITY: Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 50) {
            size = AppConstants.PAGE_SIZE;
        }

        try {
            Page<Contact> contactsPage = contactService.searchContacts(user, contactSearchForm, page, size, sortBy, direction);
            if (contactsPage == null) {
                contactsPage = org.springframework.data.domain.Page.empty();
            }
            model.addAttribute("contactSearchForm", contactSearchForm);
            model.addAttribute("contactsPage", contactsPage);
            model.addAttribute("pageSize", AppConstants.PAGE_SIZE);
            model.addAttribute("user", user);
            return "user/view_contacts";
        } catch (Exception e) {
            logger.error("Error searching contacts: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("Error performing search: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
            return "redirect:/user/contacts/view";
        }
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
        Contact contact = contactService.editContact(contactIdLong);
        ContactForm contactForm = createFormFromContact(contact); // Use helper method below
        model.addAttribute("contactId", contactIdLong);
        model.addAttribute("contactForm", contactForm);
        return "user/edit_contact";
    }


    @RequestMapping(value = "/update_contact/{contactId}", method = RequestMethod.POST)
    public String updateContact(@PathVariable("contactId") String contactId,
            @Valid @ModelAttribute ContactForm contactForm,
            BindingResult result,
            Model model,
            Authentication authentication,
            org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        Long contactIdLong = Long.parseLong(contactId);
        model.addAttribute("contactId", contactIdLong);

        // Handle validation errors first
        if (result.hasErrors()) {
            logger.info("Form validation errors: {}", result.getAllErrors());
            model.addAttribute("contactForm", contactForm);
            model.addAttribute("message",
                Message.builder()
                    .content("Please correct the validation errors")
                    .type(MessageType.red)
                    .build());
            return "user/edit_contact";
        }

        try {
            // First get the existing contact to ensure it exists
            Contact existingContact = contactService.getContactById(contactIdLong);
            if (existingContact == null) {
                throw new ResourceNotFoundException("Contact not found");
            }

            // Handle file upload if present
            if (multipartRequest != null) {
                org.springframework.web.multipart.MultipartFile file = multipartRequest.getFile("contactImage");
                if (file != null && !file.isEmpty()) {
                    try {
                        String picture = imageService.uploadImage(file, "contact_" + contactIdLong);
                        contactForm.setPicture(picture);
                    } catch (Exception e) {
                        logger.error("Error uploading image", e);
                        // Continue with contact update even if image upload fails
                    }
                }
            }

            // Update the contact
            Contact updatedContact = updateContactFromForm(contactIdLong, contactForm);
            
            // Save the updated contact
            contactService.updateContact(updatedContact);
            
            logger.info("Contact updated successfully: {}", updatedContact);

            redirectAttributes.addFlashAttribute("message",
                Message.builder()
                    .content("Contact updated successfully")
                    .type(MessageType.green)
                    .build());
            return "redirect:/user/contacts/view";

        } catch (ResourceNotFoundException e) {
            logger.error("Contact not found", e);
            model.addAttribute("message",
                Message.builder()
                    .content("Contact not found: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
            return "user/edit_contact";
        } catch (Exception e) {
            logger.error("Error updating contact", e);
            model.addAttribute("message",
                Message.builder()
                    .content("Error updating contact: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
            return "user/edit_contact";
        }
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
    User user = userService.getUserByEmail(UserHelper.getEmailOfLoggedInUser(authentication));
        Page<Contact> contactsPage;
        String field = contactSearchForm.getField();
        String keyword = contactSearchForm.getKeyword();
        if (field == null || field.isEmpty() || keyword == null || keyword.isEmpty()) {
            session.setAttribute("message",
                    Message.builder()
                            .content("Please select a field and enter a keyword to search")
                            .type(MessageType.red)
                            .build());
            contactsPage = contactService.getFavoriteContactsByUser(user, page, size, sortBy, direction);
        } else {
            switch (field) {
                case "name":
                    contactsPage = contactService.searchFavoriteByName(keyword, size, page, sortBy, direction, user);
                    break;
                case "email":
                    contactsPage = contactService.searchFavoriteByEmail(keyword, size, page, sortBy, direction, user);
                    break;
                case "phoneNumber":
                    contactsPage = contactService.searchFavoriteByPhoneNumber(keyword, size, page, sortBy, direction, user);
                    break;
                case "relationship":
                    contactsPage = contactService.searchFavoriteByRelationship(keyword, size, page, sortBy, direction, user);
                    break;
                default:
                    contactsPage = contactService.getFavoriteContactsByUser(user, page, size, sortBy, direction);
            }
        }

        // Set message if no field or keyword provided
        if (contactSearchForm.getField() == null || contactSearchForm.getField().isEmpty() 
            || contactSearchForm.getKeyword() == null || contactSearchForm.getKeyword().isEmpty()) {
            session.setAttribute("message",
                    Message.builder()
                            .content("Please select a field and enter a keyword to search")
                            .type(MessageType.red)
                            .build());
        }

        // Add attributes to the model
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
        // SECURITY: Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 50) {
            size = AppConstants.PAGE_SIZE;
        }
        
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

    // Helper method to create ContactForm from Contact
    private ContactForm createFormFromContact(Contact contact) {
        ContactForm form = new ContactForm();
        form.setName(contact.getName());
        form.setEmail(contact.getEmail());
        form.setPhoneNumber(contact.getPhoneNumber());
        form.setRelationship(contact.getRelationship());
        form.setFavorite(contact.isFavorite());
    // form.setAddress(contact.getAddress()); // Remove if ContactForm does not have setAddress
        // Add other fields as needed
        return form;
    }

    // Helper method to update Contact from ContactForm
    private Contact updateContactFromForm(Long contactId, ContactForm contactForm) {
        Contact contact = contactService.editContact(contactId);
        contact.setName(contactForm.getName());
        contact.setEmail(contactForm.getEmail());
        contact.setPhoneNumber(contactForm.getPhoneNumber());
        contact.setRelationship(contactForm.getRelationship());
        contact.setFavorite(contactForm.isFavorite());
    // contact.setAddress(contactForm.getAddress()); // Remove if ContactForm does not have getAddress
        // Add other fields as needed
        return contact;
    }


    @GetMapping(value = "/qrcode/{contactId}")
    @ResponseBody
    public ResponseEntity<?> getContactQRCode(
            @PathVariable("contactId") Long contactId,
            @RequestParam(value = "width", defaultValue = "250") int width,
            @RequestParam(value = "height", defaultValue = "250") int height) {
        try {
            // Validate dimensions
            if (width <= 0 || height <= 0 || width > 1000 || height > 1000) {
                logger.error("Invalid QR code dimensions: width={}, height={}", width, height);
                return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Invalid dimensions\"}");
            }

            // Get the contact
            Contact contact = contactService.getContactById(contactId);
            if (contact == null) {
                logger.error("Contact not found with id: {}", contactId);
                return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\": \"Contact not found\"}");
            }

            // Generate QR code
            byte[] qrCode = qrCodeGeneratorService.generateQRCodeFromContact(contact, width, height);
            if (qrCode == null || qrCode.length == 0) {
                logger.error("Generated QR code is empty for contact {}", contactId);
                return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Failed to generate QR code\"}");
            }

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);

        } catch (ResourceNotFoundException e) {
            logger.error("Contact not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\": \"Contact not found\"}");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error generating QR code: {}", e.getMessage());
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Internal server error\"}");
        }
    }

    @RequestMapping(value = "/decode-qr", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> decodeContactQR(
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"No file provided\"}");
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Unsupported media type\"}");
            }
            
            String decodedData = qrCodeGeneratorService.decodeContactQR(file);
            
            // If decoded data is empty, return error
            if (decodedData == null || decodedData.isEmpty()) {
                return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"QR code does not contain valid contact data\"}");
            }

            try {
                // Validate JSON format
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                // Just validate that it's valid JSON by trying to parse it
                mapper.readTree(decodedData);
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(decodedData);
            } catch (IOException e) {
                return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"QR code does not contain valid JSON data\"}");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid QR code data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Invalid QR code data\"}");
        } catch (Exception e) {
            logger.error("Error decoding QR code: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Internal server error\"}");
        }
    }
}
