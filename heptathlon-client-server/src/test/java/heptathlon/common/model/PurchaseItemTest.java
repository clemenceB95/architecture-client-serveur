package heptathlon.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseItemTest {

    @Test
    void constructorStoresReferenceAndQuantity() {
        PurchaseItem item = new PurchaseItem("HALTERE-001", 4);

        assertEquals("HALTERE-001", item.getProductReference());
        assertEquals(4, item.getQuantity());
    }
}
