package heptathlon.server.service;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.PaymentMode;
import heptathlon.common.model.Product;
import heptathlon.common.model.PurchaseItem;
import heptathlon.common.service.StoreService;
import heptathlon.server.dao.InvoiceDAO;
import heptathlon.server.dao.ProductDAO;
import heptathlon.server.database.DatabaseConnection;

import java.io.Serial;
import java.sql.Connection;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StoreServiceImpl extends UnicastRemoteObject implements StoreService {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ProductDAO productDAO;
    private final transient InvoiceDAO invoiceDAO;
    private transient HeadOfficeSyncService headOfficeSyncService;

    public StoreServiceImpl() throws RemoteException {
        super();
        this.productDAO = new ProductDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    public ProductDAO getProductDAO() {
        return productDAO;
    }

    public InvoiceDAO getInvoiceDAO() {
        return invoiceDAO;
    }

    public void setHeadOfficeSyncService(HeadOfficeSyncService headOfficeSyncService) {
        this.headOfficeSyncService = headOfficeSyncService;
    }

    @Override
    public Product getProductByReference(String reference) throws RemoteException {
        return productDAO.findByReference(reference);
    }

    @Override
    public List<String> getAvailableFamilies() throws RemoteException {
        return productDAO.findAvailableFamilies();
    }

    @Override
    public List<String> searchAvailableReferencesByFamily(String family) throws RemoteException {
        return productDAO.findAvailableByFamily(family);
    }

    @Override
    public List<Product> getAllProducts() throws RemoteException {
        return productDAO.findAll();
    }

    @Override
    public boolean addStock(String reference, int quantity) throws RemoteException {
        return productDAO.addStock(reference, quantity);
    }

    @Override
    public Invoice purchaseArticle(String clientName, String reference, int quantity) throws RemoteException {
        return purchaseArticles(clientName, List.of(new PurchaseItem(reference, quantity)));
    }

    @Override
    public Invoice purchaseArticles(String clientName, List<PurchaseItem> items) throws RemoteException {
        if (clientName == null || clientName.isBlank() || items == null || items.isEmpty()) {
            return null;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                List<InvoiceDAO.InvoiceCreationItem> invoiceItems = new ArrayList<>();

                for (PurchaseItem item : items) {
                    if (item == null
                            || item.getProductReference() == null
                            || item.getProductReference().isBlank()
                            || item.getQuantity() <= 0) {
                        connection.rollback();
                        return null;
                    }

                    Product product = productDAO.findByReference(connection, item.getProductReference());
                    if (product == null
                            || product.getStockQuantity() == null
                            || product.getStockQuantity() < item.getQuantity()) {
                        connection.rollback();
                        return null;
                    }

                    boolean stockUpdated = productDAO.decrementStock(connection, item.getProductReference(), item.getQuantity());
                    if (!stockUpdated) {
                        connection.rollback();
                        return null;
                    }

                    invoiceItems.add(new InvoiceDAO.InvoiceCreationItem(
                            item.getProductReference(),
                            item.getQuantity(),
                            product.getUnitPrice()
                    ));
                }

                Invoice invoice = invoiceDAO.createInvoice(connection, clientName, invoiceItems);
                connection.commit();
                return invoice;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RemoteException("Erreur lors de l'achat de l'article.", e);
        }
    }

    @Override
    public boolean payInvoice(int invoiceId, PaymentMode paymentMode) throws RemoteException {
        if (invoiceId <= 0 || paymentMode == null) {
            return false;
        }

        return invoiceDAO.payInvoice(invoiceId, paymentMode);
    }

    @Override
    public Invoice getInvoiceById(int invoiceId) throws RemoteException {
        if (invoiceId <= 0) {
            return null;
        }

        return invoiceDAO.findById(invoiceId);
    }

    @Override
    public List<Invoice> getInvoicesByDate(LocalDate date) throws RemoteException {
        if (date == null) {
            return List.of();
        }

        return invoiceDAO.findByDate(date);
    }

    @Override
    public double getRevenueByDate(LocalDate date) throws RemoteException {
        if (date == null) {
            return 0;
        }

        return invoiceDAO.getRevenueByDate(date);
    }

    @Override
    public boolean triggerInvoiceBackup() throws RemoteException {
        if (headOfficeSyncService == null) {
            return false;
        }

        return headOfficeSyncService.backupInvoicesToHeadOffice();
    }
}
