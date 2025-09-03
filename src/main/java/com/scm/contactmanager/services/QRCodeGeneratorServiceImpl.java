package com.scm.contactmanager.services;

import org.springframework.stereotype.Service;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.QRCodeGenerator;
import java.io.IOException;
import com.google.zxing.WriterException;

@Service
public class QRCodeGeneratorServiceImpl implements QRCodeGeneratorService {
    @Override
    public byte[] generateQRCodeFromContact(Contact contact, int width, int height) throws WriterException, IOException {
        return QRCodeGenerator.generateQRCodeFromContact(contact, width, height);
    }
}
