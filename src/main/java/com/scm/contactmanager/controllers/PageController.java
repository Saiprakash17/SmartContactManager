package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.FeedbackForm;
import com.scm.contactmanager.forms.UserForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.entities.PasswordResetToken;
import com.scm.contactmanager.services.PasswordResetTokenService;
import com.scm.contactmanager.forms.ResetPasswordForm;
import com.scm.contactmanager.helper.UserHelper;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Optional;


@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @RequestMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String home(Model model) {
        System.out.println("Home page requested");

        // Setting the attributes for the home page
        model.addAttribute("title", "Home - Contact Manager");
        model.addAttribute("message", "Contact Manager helps you to manage your contacts efficiently and easily.");
        model.addAttribute("gitHub", "https://github.com/Saiprakash17");
        
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        System.out.println("About page requested");

        // Setting the attributes for the about page
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        return "about";
    }

    @RequestMapping("/services")
    public String services(Model model) {
        System.out.println("services page requested");

        // Setting the attributes for the about page
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        model.addAttribute("feedbackForm", new FeedbackForm());
        return "services";
    }

    @RequestMapping("/contact")
    public String contact(Model model) {
        System.out.println("contact page requested");

        // Setting the attributes for the about page
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        return "contact";
    }

    @RequestMapping("/login")
    public String login(Model model) {
        System.out.println("login page requested");

        // Setting the attributes for the about page
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        return "login";
    }

    @RequestMapping("/register")
    public String register(Model model) {
        System.out.println("register page requested");

        // Setting the attributes for the about page
        UserForm userForm = new UserForm();
        model.addAttribute("userForm", userForm);
        return "register";
    }

    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute UserForm userForm, BindingResult rBindingResult, HttpSession session) {

        System.out.println("Start Register User");
        System.out.println(userForm);

        // Check for errors
        if (rBindingResult.hasErrors()) {
            System.out.println("Error: " + rBindingResult);
            return "register";
        }

        if(!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            System.out.println("Password and Confirm Password are not same");
            Message message = Message.builder().content("Password and Confirm Password are not same").type(MessageType.red).build();
            session.setAttribute("message", message);
            return "redirect:/register";
        }

        // Create a new user
        User user = new User();
        user.setName(userForm.getName());   
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setImageUrl(null);
        user.setAbout(userForm.getAbout());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setEnabled(false);

        User savedUser = userService.saveUser(user);
        System.out.println("Saved User: " + savedUser);

        //resisteration message
        Message message = Message.builder().content("Registration Successful!! Verify your email. Link sent to your email").type(MessageType.green).build();
        session.setAttribute("message", message);

        System.out.println("End Register User");

        return "redirect:/login";
    }

    @PostMapping("/feedback")
    public String feedback(@Valid @ModelAttribute("feedbackForm") FeedbackForm feedbackForm, 
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        System.out.println("Feedback page requested");

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Feedback - Contact Manager");
            model.addAttribute("message", "Please correct the errors in the form.");
            return "services";
        }

        try {
            if (feedbackForm.getEmail() == null || feedbackForm.getEmail().trim().isEmpty() ||
                feedbackForm.getName() == null || feedbackForm.getName().trim().isEmpty() ||
                feedbackForm.getMessage() == null || feedbackForm.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("All fields are required");
            }

            emailService.sendFeedbackEmail(
                feedbackForm.getEmail().trim(),
                "Contact Manager APP Feedback from: " + feedbackForm.getName().trim(),
                feedbackForm.getMessage().trim()
            );

            session.setAttribute("message", Message.builder()
                .type(MessageType.green)
                .content("Thank you for your feedback!")
                .build());
        } catch (Exception e) {
            session.setAttribute("message", Message.builder()
                .type(MessageType.red)
                .content("Error sending feedback: " + e.getMessage())
                .build());
        }

        return "redirect:/services";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam(value = "email", required = false) String email, 
                                      Model model) {
        try {
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email address is required.");
                return "forgot_password";
            }

            email = email.trim().toLowerCase();
            User user = userService.getUserByEmail(email);
            
            if (user == null) {
                model.addAttribute("error", "No account found with that email address.");
                return "forgot_password";
            }

            // Check if a token already exists and is not expired
            Optional<PasswordResetToken> existingToken = passwordResetTokenService.findValidTokenForUser(user);
            PasswordResetToken token;
            
            if (existingToken.isPresent()) {
                token = existingToken.get();
            } else {
                token = passwordResetTokenService.createTokenForUser(user);
            }

            String resetLink = UserHelper.getLinkForPasswordReset(token.getToken());
            emailService.sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "Click the link to reset your password: " + resetLink + "\n\nThis link will expire in 24 hours."
            );

            model.addAttribute("success", "A password reset link has been sent to your email.");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while processing your request. Please try again later.");
        }
        return "forgot_password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenService.findByToken(token);
        if (resetTokenOpt.isEmpty() || passwordResetTokenService.isTokenExpired(resetTokenOpt.get())) {
            model.addAttribute("error", "Invalid or expired password reset token.");
            model.addAttribute("resetPasswordForm", new ResetPasswordForm());
            return "reset_password";
        }
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                                       BindingResult bindingResult,
                                       Model model,
                                       HttpSession session) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenService.findByToken(token);
        if (resetTokenOpt.isEmpty() || passwordResetTokenService.isTokenExpired(resetTokenOpt.get())) {
            model.addAttribute("error", "Invalid or expired password reset token.");
            model.addAttribute("resetPasswordForm", new ResetPasswordForm());
            return "reset_password";
        }

        // Check for validation errors (including password matching)
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "reset_password";
        }

        // Additional check for password match (although @AssertTrue also handles this)
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("token", token);
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "reset_password";
        }

        try {
            User user = resetTokenOpt.get().getUser();
            userService.updatePassword(user, form.getPassword());
            passwordResetTokenService.deleteToken(resetTokenOpt.get());
            emailService.sendEmail(user.getEmail(), "Password Changed", 
                "Your password has been successfully changed. If you did not request this change, please contact support immediately.");
            
            session.setAttribute("message", Message.builder()
                    .type(MessageType.green)
                    .content("Your password has been reset successfully. Please log in with your new password.")
                    .build());
            
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("error", "An error occurred while resetting your password. Please try again.");
            return "reset_password";
        }
    }

}
