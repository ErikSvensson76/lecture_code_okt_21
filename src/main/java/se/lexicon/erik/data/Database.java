package se.lexicon.erik.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static final String DB_URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties properties = new Properties();

        try(FileInputStream inputStream = new FileInputStream("database.properties")) {
            properties.load(inputStream);
        }  catch (IOException e) {
            e.printStackTrace();
        }

        DB_URL = properties.getProperty("url");
        USER = properties.getProperty("user");
        PASSWORD = properties.getProperty("password");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL,USER, PASSWORD);
    }

}
