package com.scm.contactmanager.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {

    public static void removeMessage() {
        try {
            System.out.println("removing message from session");
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
                session.removeAttribute("message");
            } else {
                System.out.println("No request attributes found in SessionHelper");
            }
        } catch (Exception e) {
            System.out.println("Error in SessionHelper: " + e);
            e.printStackTrace();
        }

    }

}
