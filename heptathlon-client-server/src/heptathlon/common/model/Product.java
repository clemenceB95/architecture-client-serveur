package heptathlon.common.model;

import java.io.Serial;
import java.io.Serializable;

public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String reference;
    private String family;
    private double unitPrice;
    private int stockQuantity;

    public Product() {
    }

    public Product(String reference, String family, double unitPrice, int stockQuantity) {
        this.reference = reference;
        this.family = family;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }

    public String getReference() {
        return reference;
    }

    public String getFamily() {
        return family;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}