package org.amishaandkomal;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Database {
    public final static String databaseUrl;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("src/main/resources/credentials.properties"));
            databaseUrl = properties.getProperty("database.connection");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
