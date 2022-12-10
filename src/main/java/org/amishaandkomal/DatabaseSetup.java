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
        // SQL statement for creating a new table
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                	id integer PRIMARY KEY AUTOINCREMENT,
                	firstname text NOT NULL,
                	lastname text NOT NULL,
                	email text NOT NULL UNIQUE,
                	password_hash text NOT NULL
                );""";
        execute(sql);
    }

    private static void insertData() {
        // generate password hash using password4j
        Hash hash = Password.hash("password").withBcrypt();
        // SQL statement for creating a new table
        String sql = "INSERT INTO users (firstname, lastname, email, password_hash) VALUES ('Komal', 'Suryan', 'komal.suryan@gmail.com', '%s');".formatted(hash.getResult());
        execute(sql);
    }

    public static void execute(String sql) {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
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
