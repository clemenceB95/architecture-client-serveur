package heptathlon.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentModeTest {

    @Test
    void enumContainsExpectedValues() {
        assertArrayEquals(
                new PaymentMode[]{PaymentMode.CARD, PaymentMode.CASH, PaymentMode.CHECK},
                PaymentMode.values()
        );
        assertEquals(PaymentMode.CARD, PaymentMode.valueOf("CARD"));
    }
}
