package heptathlon.common.model;

import java.io.Serial;
import java.io.Serializable;

public class InvoiceItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String productReference;
    private final int quantity;
    private final double unitPrice;
    private final double lineTotal;

    public InvoiceItem(String productReference, int quantity, double unitPrice) {
        this.productReference = productReference;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice * quantity;
    }

    public String getProductReference() {
        return productReference;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }
}
