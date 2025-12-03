package com.scm.contactmanager.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {

    private static final Logger logger = LoggerFactory.getLogger(SessionHelper.class);

    public void removeMessage() {
        try {
            logger.debug("Removing message from session");
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
                session.removeAttribute("message");
            } else {
                logger.debug("No request attributes found in SessionHelper");
            }
        } catch (Exception e) {
            logger.error("Error removing message from session", e);
        }
    }
    
    public String getMessage() {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
            String message = (String) session.getAttribute("message");
            return message;
        }
        return null;
    }

    public void setMessage(String message) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
            session.setAttribute("message", message);
        }
    }

}
