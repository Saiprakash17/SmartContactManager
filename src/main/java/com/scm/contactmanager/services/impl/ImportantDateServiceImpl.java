package com.scm.contactmanager.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ImportantDate;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.ImportantDateRepo;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.ImportantDateService;

@Service
@Transactional
public class ImportantDateServiceImpl implements ImportantDateService {

    @Autowired
    private ImportantDateRepo dateRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public ImportantDate createImportantDate(Contact contact, String name, String dateStr,
            Boolean notificationEnabled, Integer daysBeforeNotify) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, formatter);

        ImportantDate importantDate = ImportantDate.builder()
            .contact(contact)
            .name(name)
            .date(date)
            .notificationEnabled(notificationEnabled != null ? notificationEnabled : true)
            .daysBeforeNotify(daysBeforeNotify != null ? daysBeforeNotify : 7)
            .build();

        return dateRepository.save(importantDate);
    }

    @Override
    public List<ImportantDate> getContactDates(Contact contact) {
        return dateRepository.findByContact(contact);
    }

    @Override
    public void updateImportantDate(Long dateId, String name, String dateStr,
            Boolean notificationEnabled, Integer daysBeforeNotify) {
        
        ImportantDate importantDate = dateRepository.findById(dateId)
            .orElseThrow(() -> new RuntimeException("Important date not found"));

        importantDate.setName(name);
        if (dateStr != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            importantDate.setDate(LocalDate.parse(dateStr, formatter));
        }
        if (notificationEnabled != null) {
            importantDate.setNotificationEnabled(notificationEnabled);
        }
        if (daysBeforeNotify != null) {
            importantDate.setDaysBeforeNotify(daysBeforeNotify);
        }

        dateRepository.save(importantDate);
    }

    @Override
    public void deleteImportantDate(Long dateId) {
        dateRepository.deleteById(dateId);
    }

    @Override
    public ImportantDate getImportantDateById(Long dateId) {
        return dateRepository.findById(dateId)
            .orElseThrow(() -> new RuntimeException("Important date not found"));
    }

    @Override
    @Scheduled(cron = "0 0 9 * * *") // 9 AM every day
    public void sendUpcomingDateReminders() {
        LocalDate today = LocalDate.now();
        List<ImportantDate> upcomingDates = dateRepository.findUpcomingDates(today, today.plusDays(30));

        for (ImportantDate importantDate : upcomingDates) {
            if (shouldSendNotification(importantDate)) {
                Contact contact = importantDate.getContact();
                User user = contact.getUser();

                String subject = "Reminder: " + importantDate.getName() + " of " + contact.getName();
                String body = buildReminderEmail(importantDate);

                emailService.sendEmail(user.getEmail(), subject, body);
                importantDate.setLastNotified(LocalDateTime.now());
                dateRepository.save(importantDate);
            }
        }
    }

    private boolean shouldSendNotification(ImportantDate date) {
        return date.getNotificationEnabled() &&
               (date.getLastNotified() == null ||
                date.getLastNotified().isBefore(LocalDateTime.now().minusDays(1)));
    }

    private String buildReminderEmail(ImportantDate date) {
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), date.getDate());
        return String.format(
            "Don't forget! %s is coming up in %d days.",
            date.getName(),
            daysUntil
        );
    }
}
