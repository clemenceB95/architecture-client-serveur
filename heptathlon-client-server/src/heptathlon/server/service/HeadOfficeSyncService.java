package heptathlon.server.service;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.server.dao.InvoiceDAO;
import heptathlon.server.dao.ProductDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadOfficeSyncService {

    private static final Logger logger = Logger.getLogger(HeadOfficeSyncService.class.getName());
    private static final LocalTime DEFAULT_PRICE_UPDATE_TIME = LocalTime.of(6, 0);
    private static final LocalTime DEFAULT_INVOICE_BACKUP_TIME = LocalTime.of(22, 0);
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Locale JSON_NUMBER_LOCALE = Locale.US;

    private final ProductDAO productDAO;
    private final InvoiceDAO invoiceDAO;
    private final ScheduledExecutorService scheduler;
    private final Path priceUpdatesFile;
    private final Path invoiceBackupsDirectory;

    public HeadOfficeSyncService(ProductDAO productDAO, InvoiceDAO invoiceDAO) {
        this.productDAO = productDAO;
        this.invoiceDAO = invoiceDAO;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.priceUpdatesFile = Path.of(
                System.getenv().getOrDefault(
                        "HEPTATHLON_HEAD_OFFICE_PRICE_FILE",
                        "heptathlon-client-server/head-office/incoming/price-updates.csv"
                )
        );
        this.invoiceBackupsDirectory = Path.of(
                System.getenv().getOrDefault(
                        "HEPTATHLON_HEAD_OFFICE_BACKUP_DIR",
                        "heptathlon-client-server/head-office/backups"
                )
        );
    }

    public void start() {
        ensureDirectories();

        long priceUpdateIntervalSeconds = getOverrideIntervalSeconds("HEPTATHLON_PRICE_UPDATE_INTERVAL_SECONDS");
        long invoiceBackupIntervalSeconds = getOverrideIntervalSeconds("HEPTATHLON_INVOICE_BACKUP_INTERVAL_SECONDS");

        scheduleTask(
                "mise a jour automatique des prix",
                this::updatePricesFromHeadOffice,
                DEFAULT_PRICE_UPDATE_TIME,
                priceUpdateIntervalSeconds
        );
        scheduleTask(
                "sauvegarde automatique des factures",
                this::backupInvoicesToHeadOffice,
                DEFAULT_INVOICE_BACKUP_TIME,
                invoiceBackupIntervalSeconds
        );
    }

    private void scheduleTask(String taskName, Runnable task, LocalTime dailyTime, long overrideIntervalSeconds) {
        if (overrideIntervalSeconds > 0) {
            scheduler.scheduleAtFixedRate(wrap(taskName, task), 0, overrideIntervalSeconds, TimeUnit.SECONDS);
            logger.info(() -> taskName + " planifiee toutes les " + overrideIntervalSeconds + " secondes.");
            return;
        }

        long initialDelay = secondsUntil(dailyTime);
        long period = TimeUnit.DAYS.toSeconds(1);
        scheduler.scheduleAtFixedRate(wrap(taskName, task), initialDelay, period, TimeUnit.SECONDS);
        logger.info(() -> taskName + " planifiee chaque jour a " + dailyTime + ".");
    }

    private Runnable wrap(String taskName, Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors de la " + taskName, e);
            }
        };
    }

    private long getOverrideIntervalSeconds(String envName) {
        String rawValue = System.getenv(envName);
        if (rawValue == null || rawValue.isBlank()) {
            return -1;
        }

        try {
            long value = Long.parseLong(rawValue.trim());
            return value > 0 ? value : -1;
        } catch (NumberFormatException e) {
            logger.warning(() -> "Valeur ignoree pour " + envName + " : " + rawValue);
            return -1;
        }
    }

    private long secondsUntil(LocalTime time) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(time.getHour()).withMinute(time.getMinute()).withSecond(0).withNano(0);
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).getSeconds();
    }

    public void updatePricesFromHeadOffice() {
        if (!Files.exists(priceUpdatesFile)) {
            logger.warning("Aucun fichier de mise a jour des prix trouve: " + priceUpdatesFile.toAbsolutePath());
            return;
        }

        try {
            List<String> lines = Files.readAllLines(priceUpdatesFile, StandardCharsets.UTF_8);
            int updatedCount = 0;

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#") || trimmedLine.startsWith("reference,")) {
                    continue;
                }

                String[] parts = trimmedLine.split(",");
                if (parts.length != 2) {
                    logger.warning("Ligne de prix ignoree: " + trimmedLine);
                    continue;
                }

                String reference = parts[0].trim();
                try {
                    double unitPrice = Double.parseDouble(parts[1].trim());
                    if (productDAO.updateUnitPrice(reference, unitPrice)) {
                        updatedCount++;
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Prix invalide ignore pour la ligne: " + trimmedLine);
                }
            }

            logger.info("Mise a jour des prix terminee. Produits modifies: " + updatedCount + ".");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Lecture impossible du fichier de prix du siege", e);
        }
    }

    public void backupInvoicesToHeadOffice() {
        List<Invoice> invoices = invoiceDAO.findAll();
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
        Path backupFile = invoiceBackupsDirectory.resolve("invoices-" + timestamp + ".json");

        try {
            Files.createDirectories(invoiceBackupsDirectory);
            Files.writeString(backupFile, toJson(invoices), StandardCharsets.UTF_8);
            logger.info("Sauvegarde des factures terminee: " + backupFile.toAbsolutePath());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ecriture impossible de la sauvegarde des factures", e);
        }
    }

    private void ensureDirectories() {
        try {
            Path parent = priceUpdatesFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.createDirectories(invoiceBackupsDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'initialiser les repertoires de synchronisation.", e);
        }
    }

    private String toJson(List<Invoice> invoices) {
        List<String> invoiceJson = new ArrayList<>();
        for (Invoice invoice : invoices) {
            invoiceJson.add("""
                    {
                      "id": %d,
                      "clientName": "%s",
                      "totalAmount": %s,
                      "paymentMode": %s,
                      "billingDate": "%s",
                      "paid": %s,
                      "items": %s
                    }
                    """.formatted(
                    invoice.getId(),
                    escapeJson(invoice.getClientName()),
                    formatJsonNumber(invoice.getTotalAmount()),
                    invoice.getPaymentMode() == null ? "null" : "\"" + invoice.getPaymentMode().name() + "\"",
                    invoice.getBillingDate(),
                    invoice.isPaid(),
                    toJsonItems(invoice.getItems())
            ).indent(2).trim());
        }

        return "[\n" + String.join(",\n", invoiceJson) + "\n]\n";
    }

    private String toJsonItems(List<InvoiceItem> items) {
        List<String> itemJson = new ArrayList<>();
        for (InvoiceItem item : items) {
            itemJson.add("""
                    {
                      "productReference": "%s",
                      "quantity": %d,
                      "unitPrice": %s,
                      "lineTotal": %s
                    }
                    """.formatted(
                    escapeJson(item.getProductReference()),
                    item.getQuantity(),
                    formatJsonNumber(item.getUnitPrice()),
                    formatJsonNumber(item.getLineTotal())
            ).indent(2).trim());
        }

        return "[\n" + String.join(",\n", itemJson) + "\n  ]";
    }

    private String formatJsonNumber(double value) {
        return String.format(JSON_NUMBER_LOCALE, "%.2f", value);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
