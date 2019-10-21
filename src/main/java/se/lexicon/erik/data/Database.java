package se.lexicon.erik.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String DB_URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        DB_URL = "jdbc:mysql://localhost:3306/persondb?&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        USER = "root";
        PASSWORD = "1234";
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL,USER, PASSWORD);
    }

}
