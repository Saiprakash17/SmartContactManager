package com.scm.contactmanager.services;

import java.util.List;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ImportantDate;

public interface ImportantDateService {
    ImportantDate createImportantDate(Contact contact, String name, String dateStr, Boolean notificationEnabled, Integer daysBeforeNotify);

    List<ImportantDate> getContactDates(Contact contact);

    void updateImportantDate(Long dateId, String name, String dateStr, Boolean notificationEnabled, Integer daysBeforeNotify);

    void deleteImportantDate(Long dateId);

    ImportantDate getImportantDateById(Long dateId);

    void sendUpcomingDateReminders();
}
