package heptathlon.server.dao;

import heptathlon.common.model.Product;
import heptathlon.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductDAO {

    private static final Logger logger = Logger.getLogger(ProductDAO.class.getName());

    public Product findByReference(String reference) {
        String sql = "SELECT * FROM products WHERE reference = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reference);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getString("reference"),
                            rs.getString("family"),
                            rs.getDouble("unit_price"),
                            rs.getInt("stock_quantity")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du produit " + reference, e);
        }

        return null;
    }

    public List<String> findAvailableByFamily(String family) {
        List<String> references = new ArrayList<>();
        String sql = "SELECT reference FROM products WHERE family = ? AND stock_quantity > 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, family);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    references.add(rs.getString("reference"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche des produits de la famille " + family, e);
        }

        return references;
    }

    public boolean addStock(String reference, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE reference = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setString(2, reference);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ajout de stock pour le produit " + reference, e);
        }

        return false;
    }

    public Product findByReference(Connection connection, String reference) throws SQLException {
        String sql = "SELECT * FROM products WHERE reference = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getString("reference"),
                            rs.getString("family"),
                            rs.getDouble("unit_price"),
                            rs.getInt("stock_quantity")
                    );
                }
            }
        }

        return null;
    }

    public boolean decrementStock(Connection connection, String reference, int quantity) throws SQLException {
        String sql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE reference = ? AND stock_quantity >= ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setString(2, reference);
            statement.setInt(3, quantity);

            return statement.executeUpdate() > 0;
        }
    }
}