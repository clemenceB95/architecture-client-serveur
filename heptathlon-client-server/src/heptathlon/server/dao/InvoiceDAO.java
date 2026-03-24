package heptathlon.server.dao;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.PaymentMode;
import heptathlon.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceDAO {

    private static final Logger logger = Logger.getLogger(InvoiceDAO.class.getName());

    public record InvoiceCreationItem(String reference, int quantity, double unitPrice) {
    }

    public Invoice createInvoice(Connection connection,
                                 String clientName,
                                 List<InvoiceCreationItem> itemsToCreate) throws SQLException {
        String insertInvoiceSql = """
                INSERT INTO invoices (client_name, total_amount, payment_mode, billing_date, paid)
                VALUES (?, ?, ?, ?, ?)
                """;
        String insertItemSql = """
                INSERT INTO invoice_items (invoice_id, product_reference, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                """;

        double totalAmount = itemsToCreate.stream()
                .mapToDouble(item -> item.unitPrice() * item.quantity())
                .sum();
        LocalDate billingDate = LocalDate.now();

        try (PreparedStatement invoiceStatement =
                     connection.prepareStatement(insertInvoiceSql, Statement.RETURN_GENERATED_KEYS)) {

            invoiceStatement.setString(1, clientName);
            invoiceStatement.setDouble(2, totalAmount);
            invoiceStatement.setNull(3, Types.VARCHAR);
            invoiceStatement.setDate(4, Date.valueOf(billingDate));
            invoiceStatement.setBoolean(5, false);
            invoiceStatement.executeUpdate();

            try (ResultSet generatedKeys = invoiceStatement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Impossible de créer la facture : aucune clé générée.");
                }

                int invoiceId = generatedKeys.getInt(1);

                try (PreparedStatement itemStatement = connection.prepareStatement(insertItemSql)) {
                    for (InvoiceCreationItem item : itemsToCreate) {
                        itemStatement.setInt(1, invoiceId);
                        itemStatement.setString(2, item.reference());
                        itemStatement.setInt(3, item.quantity());
                        itemStatement.setDouble(4, item.unitPrice());
                        itemStatement.addBatch();
                    }
                    itemStatement.executeBatch();
                }

                List<InvoiceItem> items = itemsToCreate.stream()
                        .map(item -> new InvoiceItem(item.reference(), item.quantity(), item.unitPrice()))
                        .toList();
                return new Invoice(invoiceId, clientName, totalAmount, null, billingDate, false, items);
            }
        }
    }

    public boolean payInvoice(int invoiceId, PaymentMode paymentMode) {
        String sql = """
                UPDATE invoices
                SET payment_mode = ?, paid = ?
                WHERE id = ? AND paid = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, paymentMode.name());
            statement.setBoolean(2, true);
            statement.setInt(3, invoiceId);
            statement.setBoolean(4, false);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du paiement de la facture " + invoiceId, e);
            return false;
        }
    }

    public Invoice findById(int invoiceId) {
        String invoiceSql = """
                SELECT id, client_name, total_amount, payment_mode, billing_date, paid
                FROM invoices
                WHERE id = ?
                """;
        String itemsSql = """
                SELECT product_reference, quantity, unit_price
                FROM invoice_items
                WHERE invoice_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement invoiceStatement = connection.prepareStatement(invoiceSql);
             PreparedStatement itemsStatement = connection.prepareStatement(itemsSql)) {

            invoiceStatement.setInt(1, invoiceId);

            try (ResultSet invoiceResult = invoiceStatement.executeQuery()) {
                if (!invoiceResult.next()) {
                    return null;
                }

                itemsStatement.setInt(1, invoiceId);
                List<InvoiceItem> items = new ArrayList<>();

                try (ResultSet itemsResult = itemsStatement.executeQuery()) {
                    while (itemsResult.next()) {
                        items.add(new InvoiceItem(
                                itemsResult.getString("product_reference"),
                                itemsResult.getInt("quantity"),
                                itemsResult.getDouble("unit_price")
                        ));
                    }
                }

                String paymentMode = invoiceResult.getString("payment_mode");
                return new Invoice(
                        invoiceResult.getInt("id"),
                        invoiceResult.getString("client_name"),
                        invoiceResult.getDouble("total_amount"),
                        paymentMode == null ? null : PaymentMode.valueOf(paymentMode),
                        invoiceResult.getDate("billing_date").toLocalDate(),
                        invoiceResult.getBoolean("paid"),
                        items
                );
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche de la facture " + invoiceId, e);
            return null;
        }
    }

    public List<Invoice> findByDate(LocalDate date) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = """
                SELECT id
                FROM invoices
                WHERE billing_date = ?
                ORDER BY id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Invoice invoice = findById(resultSet.getInt("id"));
                    if (invoice != null) {
                        invoices.add(invoice);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche des factures pour la date " + date, e);
        }

        return invoices;
    }

    public List<Invoice> findAll() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = """
                SELECT id
                FROM invoices
                ORDER BY billing_date DESC, id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Invoice invoice = findById(resultSet.getInt("id"));
                if (invoice != null) {
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la lecture complete des factures", e);
        }

        return invoices;
    }

    public double getRevenueByDate(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) AS revenue FROM invoices WHERE billing_date = ? AND paid = true";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("revenue");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du calcul du chiffre d'affaires pour la date " + date, e);
        }

        return 0;
    }
}
