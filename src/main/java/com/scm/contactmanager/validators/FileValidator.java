package com.scm.contactmanager.validators;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile>{

    private static final long MAX_FILE_SIZE = 1024 * 1024 * 2; // 2MB
    
    // Magic numbers (file signatures) for validation
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] GIF_MAGIC = {'G', 'I', 'F'};
    
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg",
        "image/png",
        "image/gif"
    ));
    
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "jpg",
        "jpeg",
        "png",
        "gif"
    ));

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }

        // 1. Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            addViolation(context, "File size should be less than 2MB");
            return false;
        }

        // 2. Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            addViolation(context, "Only JPEG, PNG, and GIF images are allowed");
            return false;
        }

        // 3. Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !hasAllowedExtension(filename)) {
            addViolation(context, "Invalid file extension");
            return false;
        }

        // 4. Check magic number (file signature)
        try {
            byte[] fileBytes = file.getBytes();
            if (!isValidImageMagicNumber(fileBytes, contentType)) {
                addViolation(context, "File content does not match the claimed type");
                return false;
            }
        } catch (IOException e) {
            addViolation(context, "Unable to verify file content");
            return false;
        }

        return true;
    }

    private boolean hasAllowedExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return false;
        }
        String extension = filename.substring(lastDot + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private boolean isValidImageMagicNumber(byte[] bytes, String contentType) {
        if (bytes.length < 4) {
            return false;
        }

        // Check for JPEG
        if ("image/jpeg".equals(contentType)) {
            return bytes[0] == JPEG_MAGIC[0] && 
                   bytes[1] == JPEG_MAGIC[1] && 
                   bytes[2] == JPEG_MAGIC[2];
        }

        // Check for PNG
        if ("image/png".equals(contentType)) {
            return bytes[0] == PNG_MAGIC[0] && 
                   bytes[1] == PNG_MAGIC[1] && 
                   bytes[2] == PNG_MAGIC[2] && 
                   bytes[3] == PNG_MAGIC[3];
        }

        // Check for GIF
        if ("image/gif".equals(contentType)) {
            return bytes[0] == GIF_MAGIC[0] && 
                   bytes[1] == GIF_MAGIC[1] && 
                   bytes[2] == GIF_MAGIC[2];
        }

        return false;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
