package heptathlon.common.model;

import java.io.Serial;
import java.io.Serializable;

public class PurchaseItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String productReference;
    private final int quantity;

    public PurchaseItem(String productReference, int quantity) {
        this.productReference = productReference;
        this.quantity = quantity;
    }

    public String getProductReference() {
        return productReference;
    }

    public int getQuantity() {
        return quantity;
    }
}
