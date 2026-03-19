package heptathlon.server.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final Path SCHEMA_PATH = Path.of("database", "schema.sql");

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException, IOException {
        String script = Files.readString(SCHEMA_PATH);
        String[] statements = script.split(";");

        try (Connection connection = DatabaseConnection.getServerConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : statements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }
        }
    }
}
