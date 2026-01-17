package config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    // KONFIGURASI MYSQL
    private static final String DB_NAME = "catatin_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String URL =
            "jdbc:mysql://localhost:3306/" + DB_NAME +
            "?useSSL=false" +
            "&allowPublicKeyRetrieval=true" +
            "&characterEncoding=UTF-8" +
            "&serverTimezone=Asia/Jakarta";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            System.err.println("Failed to connect to MySQL!");
            e.printStackTrace();
            return null;
        }
    }
}
