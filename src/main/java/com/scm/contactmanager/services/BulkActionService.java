package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.BulkActionRequest;
import com.scm.contactmanager.payloads.BulkActionResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface BulkActionService {
    BulkActionResponse performBulkAction(BulkActionRequest request, User user, HttpServletRequest httpRequest);
}
