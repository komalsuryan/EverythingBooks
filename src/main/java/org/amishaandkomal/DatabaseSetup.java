package org.amishaandkomal;

import com.password4j.Hash;
import com.password4j.Password;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    public final static String databaseUrl = "jdbc:sqlite:src/main/resources/EverythingBooks.db";
    private static void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(databaseUrl)) {
            if (conn != null) {
                System.out.println("Connection to SQLite has been established.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createTables() {
        // SQL statement for creating a new table for users
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                	id integer PRIMARY KEY AUTOINCREMENT,
                	firstname text NOT NULL,
                	lastname text NOT NULL,
                	email text NOT NULL UNIQUE,
                	password_hash text NOT NULL
                );""";
        execute(sql, false);

        // SQL statement for creating a new table to store OTPs
        sql = """
                CREATE TABLE IF NOT EXISTS otp (
                	email text NOT NULL UNIQUE,
                	otp integer NOT NULL,
                	expiry DATETIME NOT NULL,
                	PRIMARY KEY (email, otp)
                );""";
        execute(sql, false);
    }

    private static void insertData() {
        // generate password hash using password4j
        Hash hash = Password.hash("password").withBcrypt();
        // SQL statement for creating a new table
        String sql = """
                INSERT INTO users (firstname, lastname, email, password_hash) VALUES
                ('Komal', 'Suryan', 'komal.suryan@gmail.com', '%s'),
                ('Amisha', 'Kalhan', 'amishakalhan@gmail.com', '%s');
                """.formatted(hash.getResult(), hash.getResult());
        execute(sql, false);
    }

    public static void execute(String sql, boolean query) {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            if (query) {
                stmt.executeQuery(sql);
            } else {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setupDatabase() {
        createNewDatabase();
        createTables();
//        insertData();
    }
}
