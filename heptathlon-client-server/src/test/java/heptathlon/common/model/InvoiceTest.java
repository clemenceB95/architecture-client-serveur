package heptathlon.common.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceTest {

    @Test
    void constructorCopiesProvidedItems() {
        List<InvoiceItem> sourceItems = new ArrayList<>();
        sourceItems.add(new InvoiceItem("BALLON-001", 2, 10.0));

        Invoice invoice = new Invoice(7, "Alice", 20.0, PaymentMode.CARD, LocalDate.of(2026, 4, 2), true, sourceItems);
        sourceItems.add(new InvoiceItem("BALLON-002", 1, 5.0));

        assertEquals(7, invoice.getId());
        assertEquals("Alice", invoice.getClientName());
        assertEquals(20.0, invoice.getTotalAmount());
        assertEquals(PaymentMode.CARD, invoice.getPaymentMode());
        assertEquals(LocalDate.of(2026, 4, 2), invoice.getBillingDate());
        assertTrue(invoice.isPaid());
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    void constructorCreatesEmptyListWhenItemsAreNull() {
        Invoice invoice = new Invoice(1, "Bob", 0.0, null, LocalDate.of(2026, 4, 2), false, null);

        assertTrue(invoice.getItems().isEmpty());
    }

    @Test
    void returnedItemsListIsUnmodifiable() {
        Invoice invoice = new Invoice(
                2,
                "Chloe",
                15.0,
                PaymentMode.CASH,
                LocalDate.of(2026, 4, 2),
                false,
                List.of(new InvoiceItem("BALLON-003", 1, 15.0))
        );

        assertThrows(UnsupportedOperationException.class, () ->
                invoice.getItems().add(new InvoiceItem("BALLON-004", 1, 5.0)));
    }
}
