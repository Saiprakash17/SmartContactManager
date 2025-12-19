package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;

import com.scm.contactmanager.forms.FeedbackForm;
import com.scm.contactmanager.forms.UserForm;
import com.scm.contactmanager.forms.ResetPasswordForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.PageService;
import com.scm.contactmanager.security.RateLimiter;
import com.scm.contactmanager.security.SecurityAuditLogger;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @Autowired(required = false)
    private RateLimiter rateLimiter;

    @Autowired(required = false)
    private SecurityAuditLogger auditLogger;

    @RequestMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String home(Model model) {
        model.addAttribute("title", "Home - Contact Manager");
        model.addAttribute("message", "Contact Manager helps you to manage your contacts efficiently and easily.");
        model.addAttribute("gitHub", "https://github.com/Saiprakash17");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        return "about";
    }

    @RequestMapping("/services")
    public String services(Model model) {
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        model.addAttribute("feedbackForm", new FeedbackForm());
        return "services";
    }

    @RequestMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        return "contact";
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "About - Contact Manager");
        model.addAttribute("message", "Contact Manager is a simple web application to manage your contacts.");
        return "login";
    }

    @RequestMapping("/register")
    public String register(Model model) {
        UserForm userForm = new UserForm();
        model.addAttribute("userForm", userForm);
        return "register";
    }

    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute UserForm userForm, BindingResult bindingResult, HttpSession session, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        
        // Check rate limiting for registration endpoint
        if (rateLimiter != null && rateLimiter.isRateLimitExceeded(clientIp, RateLimiter.REGISTER_ENDPOINT)) {
            long waitTime = rateLimiter.getTimeRemainingSeconds(clientIp, RateLimiter.REGISTER_ENDPOINT);
            if (auditLogger != null) {
                auditLogger.logFailedLogin(userForm.getEmail(), "Registration rate limit exceeded after " + 5 + " attempts");
            }
            Message message = Message.builder()
                .type(MessageType.red)
                .content("Too many registration attempts. Please try again in " + waitTime + " seconds.")
                .build();
            session.setAttribute("message", message);
            return "redirect:/register";
        }
        
        if (bindingResult.hasErrors()) {
            if (auditLogger != null) {
                auditLogger.logInvalidInput(userForm.getEmail(), "registration_form", "Form validation errors");
            }
            return "register";
        }

        Message message = pageService.registerUser(userForm, bindingResult);
        session.setAttribute("message", message);

        if (message.getType() == MessageType.green) {
            // Successful registration - record success to clear rate limit counter
            if (rateLimiter != null) {
                rateLimiter.recordSuccess(clientIp, RateLimiter.REGISTER_ENDPOINT);
            }
            if (auditLogger != null) {
                auditLogger.logRegistration(userForm.getName(), userForm.getEmail());
            }
            return "redirect:/login";
        } else {
            // Registration failed - increment rate limit counter
            if (auditLogger != null) {
                auditLogger.logFailedLogin(userForm.getEmail(), "Registration failed: " + message.getContent());
            }
            return "redirect:/register";
        }
    }

    @PostMapping("/feedback")
    public String feedback(@Valid @ModelAttribute("feedbackForm") FeedbackForm feedbackForm, 
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            if (auditLogger != null) {
                auditLogger.logInvalidInput("anonymous", "feedback_form", "Form validation errors");
            }
            model.addAttribute("title", "Feedback - Contact Manager");
            model.addAttribute("message", "Please correct the errors in the form.");
            return "services";
        }

        Message message = pageService.processFeedback(feedbackForm);
        session.setAttribute("message", message);
        if (auditLogger != null) {
            auditLogger.logAuditEvent(SecurityAuditLogger.AuditEventType.ADMIN_ACTION, "anonymous", "User feedback submitted");
        }
        return "redirect:/services";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam(value = "email", required = false) String email, 
                                      Model model,
                                      HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        
        // Check rate limiting for password reset endpoint
        if (rateLimiter != null && rateLimiter.isRateLimitExceeded(clientIp, RateLimiter.PASSWORD_RESET_ENDPOINT)) {
            long waitTime = rateLimiter.getTimeRemainingSeconds(clientIp, RateLimiter.PASSWORD_RESET_ENDPOINT);
            if (auditLogger != null) {
                auditLogger.logFailedLogin("unknown", "Password reset rate limit exceeded for email: " + email);
            }
            model.addAttribute("error", "Too many password reset attempts. Please try again in " + waitTime + " seconds.");
            return "forgot_password";
        }
        
        String result = pageService.processForgotPassword(email);
        
        if ("SUCCESS".equals(result)) {
            // Record successful attempt to clear rate limit counter
            if (rateLimiter != null) {
                rateLimiter.recordSuccess(clientIp, RateLimiter.PASSWORD_RESET_ENDPOINT);
            }
            if (auditLogger != null) {
                auditLogger.logPasswordReset(email);
            }
            model.addAttribute("success", "A password reset link has been sent to your email.");
        } else {
            // Log failed password reset attempt
            if (auditLogger != null) {
                auditLogger.logFailedLogin("unknown", "Password reset failed: " + result);
            }
            model.addAttribute("error", result);
        }
        
        return "forgot_password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        if (!pageService.isValidPasswordResetToken(token)) {
            model.addAttribute("error", "Invalid or expired password reset token.");
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
        Message message = pageService.processResetPassword(token, form, bindingResult);
        
        if (message.getType() == MessageType.green) {
            if (auditLogger != null) {
                auditLogger.logPasswordChange("user_from_reset_token");
            }
        } else {
            if (auditLogger != null) {
                auditLogger.logFailedLogin("unknown", "Password reset failed: " + message.getContent());
            }
        }
        
        if (message.getType() == MessageType.red) {
            model.addAttribute("token", token);
            model.addAttribute("error", message.getContent());
            return "reset_password";
        }
        
        session.setAttribute("message", message);
        return "redirect:/login";
    }

}
