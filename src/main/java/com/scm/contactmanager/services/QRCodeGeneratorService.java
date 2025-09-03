package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.Contact;
import java.io.IOException;
import com.google.zxing.WriterException;

public interface QRCodeGeneratorService {
    byte[] generateQRCodeFromContact(Contact contact, int width, int height) throws WriterException, IOException;
}
