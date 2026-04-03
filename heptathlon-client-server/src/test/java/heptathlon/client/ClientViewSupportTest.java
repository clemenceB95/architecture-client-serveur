package heptathlon.client;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.PaymentMode;
import heptathlon.common.model.Product;
import heptathlon.common.model.PurchaseItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientViewSupportTest {

    @Test
    void validatesAndParsesUserInputs() {
        assertEquals("Alice", ClientViewSupport.requireText("  Alice  ", "client"));
        assertEquals(3, ClientViewSupport.requirePositiveNumber(3, "quantite"));
        assertEquals(12, ClientViewSupport.parsePositiveInteger("12", "facture"));

        assertThrows(IllegalArgumentException.class, () -> ClientViewSupport.requireText("   ", "client"));
        assertThrows(IllegalArgumentException.class, () -> ClientViewSupport.requirePositiveNumber(0, "quantite"));
        assertThrows(IllegalArgumentException.class, () -> ClientViewSupport.parsePositiveInteger("abc", "facture"));
    }

    @Test
    void buildsPurchaseItemsFromCartLines() {
        List<PurchaseItem> items = ClientViewSupport.buildPurchaseItems(List.of(
                new ClientViewSupport.ProductQuantity(new Product("BALLON-001", "football", 25.99, 5), 2),
                new ClientViewSupport.ProductQuantity(new Product("VELO-001", "cardio", 499.99, 1), 1)
        ));

        assertEquals(2, items.size());
        assertEquals("BALLON-001", items.get(0).getProductReference());
        assertEquals(2, items.get(0).getQuantity());
        assertThrows(IllegalArgumentException.class, () -> ClientViewSupport.buildPurchaseItems(List.of()));
    }

    @Test
    void formatsProductFamilyRevenueStatisticsAndStock() {
        Product football = new Product("BALLON-001", "football", 25.99, 5);
        Product cardio = new Product("VELO-001", "cardio", 499.99, null);
        Invoice paidInvoice = new Invoice(
                1,
                "Alice",
                25.99,
                PaymentMode.CARD,
                LocalDate.of(2026, 4, 2),
                true,
                List.of(new InvoiceItem("BALLON-001", 1, 25.99))
        );
        Invoice unpaidInvoice = new Invoice(
                2,
                "Bob",
                10.00,
                null,
                LocalDate.of(2026, 4, 2),
                false,
                List.of()
        );

        String family = ClientViewSupport.formatFamilyProducts("football", List.of(football));
        String emptyFamily = ClientViewSupport.formatFamilyProducts("fitness", List.of());
        String revenue = ClientViewSupport.formatRevenue(LocalDate.of(2026, 4, 2), 25.99, List.of(paidInvoice, unpaidInvoice));
        String stats = ClientViewSupport.formatDailyStatistics(LocalDate.of(2026, 4, 2), List.of(paidInvoice, unpaidInvoice), 25.99);
        String stock = ClientViewSupport.formatStock(List.of(football, cardio));

        assertTrue(family.contains("Famille : football"));
        assertTrue(family.contains("BALLON-001"));
        assertTrue(emptyFamily.contains("Aucun produit disponible"));
        assertTrue(revenue.contains("Montant encaisse : 25"));
        assertTrue(revenue.contains("Factures en attente : 1"));
        assertTrue(stats.contains("Nombre total de factures : 2"));
        assertTrue(stats.contains("Liste des factures"));
        assertTrue(stock.contains("Stock disponible"));
        assertTrue(stock.contains("VELO-001"));
        assertTrue(stock.contains("499"));
    }

    @Test
    void formatsCartProductAndInvoiceData() {
        Product product = new Product("BALLON-001", "football", 25.99, 5);
        Invoice invoice = new Invoice(
                4,
                "Client",
                25.99,
                PaymentMode.CHECK,
                LocalDate.of(2026, 4, 2),
                true,
                List.of(new InvoiceItem("BALLON-001", 1, 25.99))
        );

        assertEquals(1, ClientViewSupport.countPaidInvoices(List.of(invoice)));
        assertEquals("NULL", ClientViewSupport.formatStockQuantity(new Product("BALLON-002", "football", 19.99, null)));
        assertTrue(ClientViewSupport.formatProduct(product).contains("Reference : BALLON-001"));
        assertTrue(ClientViewSupport.formatInvoice(invoice).contains("Facture #4"));
        assertTrue(ClientViewSupport.formatSelectedProductStock(product, 3).contains("Prix : 25"));
        assertTrue(ClientViewSupport.formatCartItem(product, 2).contains("Total : 51"));
        assertTrue(ClientViewSupport.formatCartTotal(List.of(
                new ClientViewSupport.ProductQuantity(product, 2),
                new ClientViewSupport.ProductQuantity(new Product("VELO-001", "cardio", 489.99, 1), 1)
        )).contains("541"));
    }
}
