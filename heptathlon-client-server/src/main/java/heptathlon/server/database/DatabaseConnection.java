package heptathlon.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/heptathlon";
    private static final String DEFAULT_SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("HEPTATHLON_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("HEPTATHLON_DB_USER", DEFAULT_USER);
        String password = System.getenv().getOrDefault("HEPTATHLON_DB_PASSWORD", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getServerConnection() throws SQLException {
        String url = System.getenv().getOrDefault("HEPTATHLON_DB_SERVER_URL", DEFAULT_SERVER_URL);
        String user = System.getenv().getOrDefault("HEPTATHLON_DB_USER", DEFAULT_USER);
        String password = System.getenv().getOrDefault("HEPTATHLON_DB_PASSWORD", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }
}
