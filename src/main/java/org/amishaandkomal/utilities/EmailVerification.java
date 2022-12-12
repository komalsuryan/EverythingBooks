package org.amishaandkomal.utilities;

import org.amishaandkomal.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class EmailVerification {
    public static void sendOTP(String email) {
        // check if the email is already in the database
        String sql = "SELECT * FROM otp WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(Database.databaseUrl);
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            // if the email is already in the database, delete the old OTP
            if (rs.next()) {
                sql = "DELETE FROM otp WHERE email = '%s';".formatted(email);
                try (java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                    stmt2.executeUpdate();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred in the verification process. " + e);
        }
        // generate 6 digit OTP
        int otp = (int) (Math.random() * 900000) + 100000;
        // add the email and OTP to the database along with the expiry time of 15 minutes
        sql = "INSERT INTO otp (email, otp, expiry) VALUES (?, ?, timestamp(DATE_ADD(NOW(), INTERVAL 15 MINUTE)));";
        try (Connection conn = DriverManager.getConnection(Database.databaseUrl);
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, otp);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // create message
        String message = "Your OTP to register for EverythingBooks is " + otp;
        // send message
        EmailSender.sendEmail(email, "Registration Verification", message);
    }

    public static boolean verifyOtp(String email, int otp) {
        // get the otp and expiry time from the database
        String sql = "SELECT * FROM otp WHERE email = ? AND otp = ?";
        try (Connection conn = DriverManager.getConnection(Database.databaseUrl);
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, otp);
            ResultSet rs = stmt.executeQuery();
            // if the email and OTP are not in the database, return false
            if (!rs.next()) {
                return false;
            }
            // delete the OTP from the database
            sql = "DELETE FROM otp WHERE email = '%s';".formatted(email);
            try (java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                stmt2.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // get the current datetime from database
            sql = "SELECT timestamp(NOW());";
            try (java.sql.PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                ResultSet rs2 = stmt2.executeQuery();
                rs2.next();
                Timestamp current_time = rs2.getTimestamp(1);
                Timestamp expiry_time = rs.getTimestamp("expiry");
                // if the current time is after the expiry time, return false
                return !current_time.after(expiry_time);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
