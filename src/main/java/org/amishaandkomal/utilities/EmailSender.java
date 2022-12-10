package org.amishaandkomal.utilities;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailSender {
    public static void sendEmail(String email, String subject, String message) {
        // get the mail properties from the properties file
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/email.properties"));
            properties.load(new FileInputStream("src/main/resources/credentials.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // create a session
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("mail.user"), properties.getProperty("mail.password"));
            }
        });

        // create a message
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(properties.getProperty("mail.user"), properties.getProperty("mail.name")));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
