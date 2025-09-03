
package com.scm.contactmanager.helper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.scm.contactmanager.entities.Contact;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeGenerator {

    // Generate QR code from contact details as JSON
    public static byte[] generateQRCodeFromContact(Contact contact, int width, int height) throws WriterException, IOException {
        String json = contactToJson(contact);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(json, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    // Convert contact to JSON string
    public static String contactToJson(Contact contact) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(contact.getName()).append("\"");
        sb.append(",\"email\":\"").append(contact.getEmail()).append("\"");
        sb.append(",\"phoneNumber\":\"").append(contact.getPhoneNumber()).append("\"");
        sb.append(",\"favorite\":").append(contact.isFavorite());
        if (contact.getAddress() != null) {
            sb.append(",\"address\":{");
            sb.append("\"street\":\"").append(contact.getAddress().getStreet()).append("\"");
            sb.append(",\"city\":\"").append(contact.getAddress().getCity()).append("\"");
            sb.append(",\"state\":\"").append(contact.getAddress().getState()).append("\"");
            sb.append(",\"zipCode\":\"").append(contact.getAddress().getZipCode()).append("\"");
            sb.append(",\"country\":\"").append(contact.getAddress().getCountry()).append("\"");
            sb.append("}");
        }
        sb.append(",\"about\":\"").append(contact.getAbout()).append("\"");
        sb.append(",\"linkedin\":\"").append(contact.getLinkedin()).append("\"");
        sb.append(",\"website\":\"").append(contact.getWebsite()).append("\"");
        if (contact.getRelationship() != null) {
            sb.append(",\"relationship\":\"").append(contact.getRelationship().getLabel()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    // Decode QR code image and return embedded JSON string
    public static String decodeQRCodeImage(byte[] imageBytes) throws Exception {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
        java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(bais);
        com.google.zxing.LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(bufferedImage);
        com.google.zxing.BinaryBitmap bitmap = new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
        com.google.zxing.Result result = new com.google.zxing.qrcode.QRCodeReader().decode(bitmap);
        return result.getText();
    }

    public static void generateQRCodeImageToFile(String text, int width, int height, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    // Helper method to generate QR code from any string
    public static byte[] generateQRCodeFromString(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
