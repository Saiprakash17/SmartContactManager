package com.scm.contactmanager.services;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    public String uploadImage(MultipartFile file, String fileName);
    public String getUrlFromPublicId(String publicId);
}
