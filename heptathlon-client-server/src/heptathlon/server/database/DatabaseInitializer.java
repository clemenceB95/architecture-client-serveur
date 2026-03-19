package heptathlon.server.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class DatabaseInitializer {

    private static final String SCHEMA_RESOURCE_PATH = "/database/schema.sql";
    private static final List<Path> SCHEMA_PATH_CANDIDATES = List.of(
            Path.of("database", "schema.sql"),
            Path.of("heptathlon-client-server", "database", "schema.sql")
    );

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException, IOException {
        String script = loadSchemaScript();
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

    private static String loadSchemaScript() throws IOException {
        try (InputStream inputStream = DatabaseInitializer.class.getResourceAsStream(SCHEMA_RESOURCE_PATH)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        for (Path path : SCHEMA_PATH_CANDIDATES) {
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        }

        throw new IOException("Impossible de localiser schema.sql. Emplacements testes: "
                + SCHEMA_PATH_CANDIDATES + " et ressource " + SCHEMA_RESOURCE_PATH);
    }
}
