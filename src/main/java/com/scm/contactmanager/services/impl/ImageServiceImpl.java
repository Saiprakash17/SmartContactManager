package com.scm.contactmanager.services.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.scm.contactmanager.helper.AppConstants;
import com.scm.contactmanager.helper.QRCodeGenerator;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.entities.Contact;
import com.google.zxing.WriterException;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String fileName) {
        
        try {
            byte[] data = new byte[file.getInputStream().available()];

            file.getInputStream().read(data);
            cloudinary.uploader().upload(data, ObjectUtils.asMap(
                "public_id", fileName
            ));

            return this.getUrlFromPublicId(fileName);
        } catch (IOException e) {
            logger.error("Error uploading image for file: {}", fileName, e);
            return null;
        }

    }

    @Override
    public String getUrlFromPublicId(String publicId) {
        return cloudinary
                .url()
                .transformation(
                        new Transformation<>()
                                .width(AppConstants.CONTACT_IMAGE_WIDTH)
                                .height(AppConstants.CONTACT_IMAGE_HEIGHT)
                                .crop(AppConstants.CONTACT_IMAGE_CROP))
                .generate(publicId);
    }
    
    @Override
    public byte[] generateQRCode(Contact contact) {
        try {
            return QRCodeGenerator.generateQRCodeFromContact(contact, 250, 250);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}

