package heptathlon.common.service;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.PaymentMode;
import heptathlon.common.model.Product;
import heptathlon.common.model.PurchaseItem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface StoreService extends Remote {

    Product getProductByReference(String reference) throws RemoteException;

    List<String> getAvailableFamilies() throws RemoteException;

    List<String> searchAvailableReferencesByFamily(String family) throws RemoteException;

    List<Product> getAllProducts() throws RemoteException;

    boolean addStock(String reference, int quantity) throws RemoteException;

    Invoice purchaseArticle(String clientName, String reference, int quantity) throws RemoteException;

    Invoice purchaseArticles(String clientName, List<PurchaseItem> items) throws RemoteException;

    boolean payInvoice(int invoiceId, PaymentMode paymentMode) throws RemoteException;

    Invoice getInvoiceById(int invoiceId) throws RemoteException;

    List<Invoice> getInvoicesByDate(LocalDate date) throws RemoteException;

    double getRevenueByDate(LocalDate date) throws RemoteException;

    boolean triggerInvoiceBackup() throws RemoteException;
}
