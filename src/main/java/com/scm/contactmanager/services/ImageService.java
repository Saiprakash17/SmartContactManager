package com.scm.contactmanager.services;

import org.springframework.web.multipart.MultipartFile;
import com.scm.contactmanager.entities.Contact;

public interface ImageService {

    public String uploadImage(MultipartFile file, String fileName);
    public String getUrlFromPublicId(String publicId);
    public byte[] generateQRCode(Contact contact);
}
