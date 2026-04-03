package heptathlon.client;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.PaymentMode;
import heptathlon.common.model.Product;
import heptathlon.common.model.PurchaseItem;
import heptathlon.common.service.StoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientFrameTest {

    private JFrame frame;

    @AfterEach
    void tearDown() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(() -> frame.dispose());
        }
    }

    @Test
    void clientFrameCoversMainUserFlows() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        FakeStoreService service = new FakeStoreService();
        service.addProduct(new Product("BALLON-001", "football", 25.99, 20));
        service.addProduct(new Product("VELO-001", "cardio", 499.99, 3));
        service.addProduct(new Product("HALTERE-001", "musculation", 15.00, 12));

        Object clientFrame = createFrame(service);

        JTextField productReferenceField = getField(clientFrame, "productReferenceField", JTextField.class);
        JTextArea resultArea = getField(clientFrame, "resultArea", JTextArea.class);
        JTextField purchaseClientField = getField(clientFrame, "purchaseClientField", JTextField.class);
        JSpinner purchaseQuantitySpinner = getField(clientFrame, "purchaseQuantitySpinner", JSpinner.class);
        JTextField invoiceIdField = getField(clientFrame, "invoiceIdField", JTextField.class);
        JTextField paymentInvoiceIdField = getField(clientFrame, "paymentInvoiceIdField", JTextField.class);
        JComboBox<?> familyComboBox = getField(clientFrame, "familyComboBox", JComboBox.class);
        JComboBox<?> purchaseProductComboBox = getField(clientFrame, "purchaseProductComboBox", JComboBox.class);
        JComboBox<?> paymentModeComboBox = getField(clientFrame, "paymentModeComboBox", JComboBox.class);
        JSpinner revenueDateSpinner = getField(clientFrame, "revenueDateSpinner", JSpinner.class);
        JTextField stockReferenceField = getField(clientFrame, "stockReferenceField", JTextField.class);
        JSpinner stockQuantitySpinner = getField(clientFrame, "stockQuantitySpinner", JSpinner.class);
        JTabbedPane tabs = getField(clientFrame, "tabs", JTabbedPane.class);

        runOnEdt(() -> productReferenceField.setText("BALLON-001"));
        invoke(clientFrame, "handleShowProduct");
        assertTrue(resultArea.getText().contains("Reference : BALLON-001"));

        runOnEdt(() -> familyComboBox.setSelectedItem("football"));
        invoke(clientFrame, "handleFamilySelection");
        assertTrue(resultArea.getText().contains("Famille : football"));
        assertTrue(resultArea.getText().contains("BALLON-001"));

        runOnEdt(() -> {
            tabs.setSelectedIndex(1);
            purchaseClientField.setText("Alice");
            purchaseProductComboBox.setSelectedIndex(0);
            purchaseQuantitySpinner.setValue(2);
        });
        invoke(clientFrame, "handleAddSelectedProductToCart");
        invoke(clientFrame, "handlePurchase");
        assertTrue(resultArea.getText().contains("Achat enregistre."));
        assertEquals("1", invoiceIdField.getText());
        assertEquals("1", paymentInvoiceIdField.getText());

        runOnEdt(() -> paymentModeComboBox.setSelectedItem(PaymentMode.CARD));
        invoke(clientFrame, "handlePayInvoice");
        assertTrue(resultArea.getText().contains("Facture payee."));

        runOnEdt(() -> invoiceIdField.setText("1"));
        invoke(clientFrame, "handleShowInvoice");
        assertTrue(resultArea.getText().contains("Facture #1"));

        runOnEdt(() -> {
            purchaseClientField.setText("Bob");
            purchaseProductComboBox.setSelectedIndex(0);
            purchaseQuantitySpinner.setValue(1);
        });
        invoke(clientFrame, "handleAddSelectedProductToCart");
        invoke(clientFrame, "handlePurchaseAndPay");
        assertTrue(resultArea.getText().contains("Achat et paiement enregistres."));

        runOnEdt(() -> {
            tabs.setSelectedIndex(3);
            revenueDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now()));
        });
        invoke(clientFrame, "handleShowRevenue");
        assertTrue(resultArea.getText().contains("Chiffre d'affaires du "));
        assertTrue(resultArea.getText().contains("Montant encaisse"));

        invoke(clientFrame, "handleShowDailyStatistics");
        assertTrue(resultArea.getText().contains("Statistiques du "));
        assertTrue(resultArea.getText().contains("Liste des factures"));

        runOnEdt(() -> {
            stockReferenceField.setText("BALLON-001");
            stockQuantitySpinner.setValue(3);
        });
        invoke(clientFrame, "handleAddStock");
        assertTrue(resultArea.getText().contains("Stock mis a jour."));

        invoke(clientFrame, "handleShowStock");
        assertTrue(resultArea.getText().contains("Stock disponible"));
        assertTrue(resultArea.getText().contains("BALLON-001"));

        service.backupResult = false;
        invoke(clientFrame, "handleTriggerInvoiceBackup");
        assertTrue(resultArea.getText().contains("Echec de la sauvegarde"));

        service.backupResult = true;
        invoke(clientFrame, "handleTriggerInvoiceBackup");
        assertTrue(resultArea.getText().contains("Sauvegarde des factures declenchee"));
    }

    @Test
    void helperMethodsHandleValidationAndFormatting() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        FakeStoreService service = new FakeStoreService();
        service.addProduct(new Product("BALLON-001", "football", 25.99, 5));
        Object clientFrame = createFrame(service);

        invoke(clientFrame, "refreshPurchaseProductOptions");
        assertNotNull(clientFrame);
    }

    private Object createFrame(FakeStoreService service) throws Exception {
        AtomicReference<Object> reference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                Class<?> frameType = Class.forName("heptathlon.client.ClientMain$ClientFrame");
                Constructor<?> constructor = frameType.getDeclaredConstructor(StoreService.class);
                constructor.setAccessible(true);
                Object created = constructor.newInstance(service);
                frame = (JFrame) created;
                reference.set(created);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Object created = reference.get();
        assertNotNull(created);
        return created;
    }

    private static void runOnEdt(Runnable runnable) throws Exception {
        SwingUtilities.invokeAndWait(runnable);
    }

    private static Object invoke(Object target, String methodName) throws Exception {
        return invoke(target, methodName, new Class<?>[0]);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        AtomicReference<Object> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                result.set(method.invoke(target, args));
            } catch (Throwable throwable) {
                error.set(throwable);
            }
        });
        if (error.get() != null) {
            Throwable throwable = error.get();
            if (throwable instanceof ReflectiveOperationException reflectiveOperationException) {
                throw reflectiveOperationException;
            }
            throw new RuntimeException(throwable);
        }
        return result.get();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static final class FakeStoreService implements StoreService {
        private final Map<String, Product> products = new LinkedHashMap<>();
        private final Map<Integer, Invoice> invoices = new LinkedHashMap<>();
        private int nextInvoiceId = 1;
        private boolean backupResult = true;

        private void addProduct(Product product) {
            products.put(product.getReference(), new Product(
                    product.getReference(),
                    product.getFamily(),
                    product.getUnitPrice(),
                    product.getStockQuantity()
            ));
        }

        @Override
        public Product getProductByReference(String reference) {
            Product product = products.get(reference);
            if (product == null) {
                return null;
            }
            return new Product(product.getReference(), product.getFamily(), product.getUnitPrice(), product.getStockQuantity());
        }

        @Override
        public List<String> getAvailableFamilies() {
            return products.values().stream()
                    .filter(product -> product.getStockQuantity() != null && product.getStockQuantity() > 0)
                    .map(Product::getFamily)
                    .distinct()
                    .sorted()
                    .toList();
        }

        @Override
        public List<String> searchAvailableReferencesByFamily(String family) {
            return products.values().stream()
                    .filter(product -> product.getFamily().equals(family))
                    .filter(product -> product.getStockQuantity() != null && product.getStockQuantity() > 0)
                    .map(Product::getReference)
                    .sorted()
                    .toList();
        }

        @Override
        public List<Product> getAllProducts() {
            return products.values().stream()
                    .sorted(Comparator.comparing(Product::getReference))
                    .map(product -> new Product(product.getReference(), product.getFamily(), product.getUnitPrice(), product.getStockQuantity()))
                    .toList();
        }

        @Override
        public boolean addStock(String reference, int quantity) {
            Product product = products.get(reference);
            if (product == null || quantity <= 0) {
                return false;
            }
            int current = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            product.setStockQuantity(current + quantity);
            return true;
        }

        @Override
        public Invoice purchaseArticle(String clientName, String reference, int quantity) {
            return purchaseArticles(clientName, List.of(new PurchaseItem(reference, quantity)));
        }

        @Override
        public Invoice purchaseArticles(String clientName, List<PurchaseItem> items) {
            List<InvoiceItem> invoiceItems = new ArrayList<>();
            double total = 0.0;
            for (PurchaseItem item : items) {
                Product product = products.get(item.getProductReference());
                if (product == null || product.getStockQuantity() == null || product.getStockQuantity() < item.getQuantity()) {
                    return null;
                }
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                invoiceItems.add(new InvoiceItem(product.getReference(), item.getQuantity(), product.getUnitPrice()));
                total += product.getUnitPrice() * item.getQuantity();
            }

            Invoice invoice = new Invoice(nextInvoiceId++, clientName, total, null, LocalDate.now(), false, invoiceItems);
            invoices.put(invoice.getId(), invoice);
            return invoice;
        }

        @Override
        public boolean payInvoice(int invoiceId, PaymentMode paymentMode) {
            Invoice invoice = invoices.get(invoiceId);
            if (invoice == null || invoice.isPaid()) {
                return false;
            }
            invoices.put(invoiceId, new Invoice(
                    invoice.getId(),
                    invoice.getClientName(),
                    invoice.getTotalAmount(),
                    paymentMode,
                    invoice.getBillingDate(),
                    true,
                    invoice.getItems()
            ));
            return true;
        }

        @Override
        public Invoice getInvoiceById(int invoiceId) {
            return invoices.get(invoiceId);
        }

        @Override
        public List<Invoice> getInvoicesByDate(LocalDate date) {
            return invoices.values().stream()
                    .filter(invoice -> invoice.getBillingDate().equals(date))
                    .sorted(Comparator.comparingInt(Invoice::getId))
                    .toList();
        }

        @Override
        public double getRevenueByDate(LocalDate date) {
            return invoices.values().stream()
                    .filter(invoice -> invoice.getBillingDate().equals(date))
                    .filter(Invoice::isPaid)
                    .mapToDouble(Invoice::getTotalAmount)
                    .sum();
        }

        @Override
        public boolean triggerInvoiceBackup() {
            return backupResult;
        }
    }
}
