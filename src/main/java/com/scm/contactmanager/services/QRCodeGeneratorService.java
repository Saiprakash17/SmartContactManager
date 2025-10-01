package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.Contact;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.google.zxing.WriterException;

public interface QRCodeGeneratorService {
    /**
     * Generates a QR code image for a contact
     */
    byte[] generateQRCodeFromContact(Contact contact, int width, int height) throws WriterException, IOException;

    /**
     * Decodes a QR code image into contact JSON
     */
    String decodeContactQR(MultipartFile file) throws Exception;
}
