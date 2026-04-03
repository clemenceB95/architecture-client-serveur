package heptathlon.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductTest {

    @Test
    void defaultConstructorLeavesFieldsUnset() {
        Product product = new Product();

        assertNull(product.getReference());
        assertNull(product.getFamily());
        assertEquals(0.0, product.getUnitPrice());
        assertNull(product.getStockQuantity());
    }

    @Test
    void constructorAndSetterExposeConfiguredValues() {
        Product product = new Product("VELO-001", "cardio", 499.99, 3);

        assertEquals("VELO-001", product.getReference());
        assertEquals("cardio", product.getFamily());
        assertEquals(499.99, product.getUnitPrice());
        assertEquals(3, product.getStockQuantity());

        product.setStockQuantity(5);

        assertEquals(5, product.getStockQuantity());
    }
}
