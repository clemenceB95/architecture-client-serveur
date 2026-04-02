package heptathlon.server.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

public final class DatabaseInitializer {

    private static final String SCHEMA_RESOURCE_PATH = "/database/schema.sql";
    private static final List<Path> SCHEMA_PATH_CANDIDATES = List.of(
            Path.of("database", "schema.sql"),
            Path.of("heptathlon-client-server", "database", "schema.sql")
    );

    private DatabaseInitializer() {
    }

    public static void initialize(boolean resetDatabase) throws SQLException, IOException {
        if (resetDatabase) {
            runResetScript();
            return;
        }

        ensureDatabaseExists();
        ensureSchema();
        seedProductsIfMissing();
    }

    private static void runResetScript() throws SQLException, IOException {
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

    private static void ensureDatabaseExists() throws SQLException {
        try (Connection connection = DatabaseConnection.getServerConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS heptathlon");
        }
    }

    private static void ensureSchema() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        reference VARCHAR(50) PRIMARY KEY,
                        family VARCHAR(100) NOT NULL,
                        unit_price DOUBLE NOT NULL,
                        stock_quantity INT NULL DEFAULT 0
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        client_name VARCHAR(120) NOT NULL,
                        total_amount DOUBLE NOT NULL,
                        payment_mode VARCHAR(20) NULL,
                        billing_date DATE NOT NULL,
                        paid BOOLEAN NOT NULL DEFAULT FALSE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS invoice_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        invoice_id INT NOT NULL,
                        product_reference VARCHAR(50) NOT NULL,
                        quantity INT NOT NULL,
                        unit_price DOUBLE NOT NULL,
                        CONSTRAINT fk_invoice_items_invoice
                            FOREIGN KEY (invoice_id) REFERENCES invoices(id),
                        CONSTRAINT fk_invoice_items_product
                            FOREIGN KEY (product_reference) REFERENCES products(reference)
                    )
                    """);
        }
    }

    private static void seedProductsIfMissing() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT IGNORE INTO products (reference, family, unit_price, stock_quantity)
                     VALUES (?, ?, ?, ?)
                     """)) {
            insertSeedProduct(statement, "BALLON-001", "football", 25.99, 20);
            insertSeedProduct(statement, "BALLON-002", "football", 19.99, 0);
            insertSeedProduct(statement, "BALLON-003", "football", 34.99, 7);
            insertSeedProduct(statement, "BALLON-004", "football", 29.99, null);
            insertSeedProduct(statement, "RAQUETTE-002", "tennis", 79.90, 10);
            insertSeedProduct(statement, "RAQUETTE-003", "tennis", 99.90, 0);
            insertSeedProduct(statement, "TAPIS-003", "fitness", 39.50, 15);
            insertSeedProduct(statement, "TAPIS-004", "fitness", 49.90, 0);
            insertSeedProduct(statement, "HALTERE-001", "musculation", 15.00, 12);
            insertSeedProduct(statement, "HALTERE-002", "musculation", 29.00, 0);
            insertSeedProduct(statement, "VELO-001", "cardio", 499.99, 3);
            statement.executeBatch();
        }
    }

    private static void insertSeedProduct(PreparedStatement statement,
                                          String reference,
                                          String family,
                                          double unitPrice,
                                          Integer stockQuantity) throws SQLException {
        statement.setString(1, reference);
        statement.setString(2, family);
        statement.setDouble(3, unitPrice);
        if (stockQuantity == null) {
            statement.setNull(4, Types.INTEGER);
        } else {
            statement.setInt(4, stockQuantity);
        }
        statement.addBatch();
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
