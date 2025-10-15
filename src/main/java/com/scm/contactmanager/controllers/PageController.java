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

import com.scm.contactmanager.forms.FeedbackForm;
import com.scm.contactmanager.forms.UserForm;
import com.scm.contactmanager.forms.ResetPasswordForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.PageService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class PageController {

    @Autowired
    private PageService pageService;

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
    public String registerUser(@Valid @ModelAttribute UserForm userForm, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        Message message = pageService.registerUser(userForm, bindingResult);
        session.setAttribute("message", message);

        return message.getType() == MessageType.green ? "redirect:/login" : "redirect:/register";
    }

    @PostMapping("/feedback")
    public String feedback(@Valid @ModelAttribute("feedbackForm") FeedbackForm feedbackForm, 
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Feedback - Contact Manager");
            model.addAttribute("message", "Please correct the errors in the form.");
            return "services";
        }

        Message message = pageService.processFeedback(feedbackForm);
        session.setAttribute("message", message);
        return "redirect:/services";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam(value = "email", required = false) String email, 
                                      Model model) {
        String result = pageService.processForgotPassword(email);
        
        if ("SUCCESS".equals(result)) {
            model.addAttribute("success", "A password reset link has been sent to your email.");
        } else {
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
        
        if (message.getType() == MessageType.red) {
            model.addAttribute("token", token);
            model.addAttribute("error", message.getContent());
            return "reset_password";
        }
        
        session.setAttribute("message", message);
        return "redirect:/login";
    }

}
