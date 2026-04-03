package heptathlon.server.service;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.PaymentMode;
import heptathlon.server.dao.InvoiceDAO;
import heptathlon.server.dao.ProductDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadOfficeSyncServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void updatePricesFromHeadOfficeUpdatesOnlyValidLines() throws Exception {
        RecordingProductDAO productDAO = new RecordingProductDAO();
        HeadOfficeSyncService service = new HeadOfficeSyncService(productDAO, new StubInvoiceDAO(List.of()));
        Path priceFile = tempDir.resolve("incoming").resolve("price-updates.csv");
        Files.createDirectories(priceFile.getParent());
        Files.writeString(priceFile, String.join("\n",
                "reference,unit_price",
                "BALLON-001,25.99",
                "",
                "# commentaire",
                "invalid-line",
                "BALLON-002,not-a-number",
                "VELO-001,499.99"
        ));
        setField(service, "priceUpdatesFile", priceFile);

        service.updatePricesFromHeadOffice();

        assertEquals(List.of("BALLON-001=25.99", "VELO-001=499.99"), productDAO.updatedPrices);
    }

    @Test
    void backupInvoicesToHeadOfficeCreatesJsonFile() throws Exception {
        Invoice invoice = new Invoice(
                12,
                "Alice \"Sport\"",
                41.98,
                PaymentMode.CARD,
                LocalDate.of(2026, 4, 2),
                true,
                List.of(new InvoiceItem("BALLON-001", 2, 20.99))
        );
        HeadOfficeSyncService service = new HeadOfficeSyncService(new RecordingProductDAO(), new StubInvoiceDAO(List.of(invoice)));
        Path backupDirectory = tempDir.resolve("backups");
        setField(service, "invoiceBackupsDirectory", backupDirectory);

        boolean backedUp = service.backupInvoicesToHeadOffice();

        assertTrue(backedUp);
        List<Path> files;
        try (Stream<Path> stream = Files.list(backupDirectory)) {
            files = stream.toList();
        }
        assertEquals(1, files.size());
        String json = Files.readString(files.get(0));
        assertTrue(json.contains("\"id\": 12"));
        assertTrue(json.contains("\"clientName\": \"Alice \\\"Sport\\\"\""));
        assertTrue(json.contains("\"paymentMode\": \"CARD\""));
        assertTrue(json.contains("\"productReference\": \"BALLON-001\""));
    }

    @Test
    void backupInvoicesToHeadOfficeReturnsFalseWhenTargetCannotBeCreated() throws Exception {
        HeadOfficeSyncService service = new HeadOfficeSyncService(new RecordingProductDAO(), new StubInvoiceDAO(List.of()));
        Path blockingFile = tempDir.resolve("not-a-directory.txt");
        Files.writeString(blockingFile, "x");
        setField(service, "invoiceBackupsDirectory", blockingFile.resolve("backups"));

        boolean backedUp = service.backupInvoicesToHeadOffice();

        assertFalse(backedUp);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class RecordingProductDAO extends ProductDAO {
        private final List<String> updatedPrices = new ArrayList<>();

        @Override
        public boolean updateUnitPrice(String reference, double unitPrice) {
            updatedPrices.add(reference + "=" + String.format(java.util.Locale.US, "%.2f", unitPrice));
            return true;
        }
    }

    private static final class StubInvoiceDAO extends InvoiceDAO {
        private final List<Invoice> invoices;

        private StubInvoiceDAO(List<Invoice> invoices) {
            this.invoices = invoices;
        }

        @Override
        public List<Invoice> findAll() {
            return invoices;
        }
    }
}
