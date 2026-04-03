package heptathlon.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceItemTest {

    @Test
    void lineTotalMatchesUnitPriceTimesQuantity() {
        InvoiceItem item = new InvoiceItem("BALLON-001", 3, 25.99);

        assertEquals("BALLON-001", item.getProductReference());
        assertEquals(3, item.getQuantity());
        assertEquals(25.99, item.getUnitPrice());
        assertEquals(77.97, item.getLineTotal(), 0.0001);
    }
}
