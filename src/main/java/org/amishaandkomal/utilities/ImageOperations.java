package org.amishaandkomal.utilities;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ImageOperations {
    public static String uploadImage(File image, String name) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/credentials.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Cloudinary cloudinary = new Cloudinary(properties.getProperty("cloudinary.url"));
        try {
            Map uploadResult = cloudinary.uploader().upload(image, ObjectUtils.asMap("public_id", "everythingBooks/rentalUserIds/" + name));
            return (String) uploadResult.get("url");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getScaledImageURL(String imageURL) {
        return imageURL.replace("upload/", "upload/w_" + 200 + ",h_" + 150 + ",c_fill/");
    }
}
