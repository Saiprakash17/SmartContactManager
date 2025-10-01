package com.scm.contactmanager.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.QRCodeGenerator;
import com.scm.contactmanager.services.QRCodeGeneratorService;

import java.io.IOException;

@Service
public class QRCodeGeneratorServiceImpl implements QRCodeGeneratorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] generateQRCodeFromContact(Contact contact, int width, int height) throws WriterException, IOException {
        return QRCodeGenerator.generateQRCodeFromContact(contact, width, height);
    }

    @Override
    public String decodeContactQR(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No QR code file provided");
        }

        byte[] imageBytes = file.getBytes();
        String contactJson = QRCodeGenerator.decodeQRCodeImage(imageBytes);
        
        // Validate JSON format
        objectMapper.readTree(contactJson); // This will throw if invalid JSON
        
        return contactJson;
    }
}