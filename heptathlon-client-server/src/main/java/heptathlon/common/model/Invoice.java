package heptathlon.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Invoice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String clientName;
    private final double totalAmount;
    private final PaymentMode paymentMode;
    private final LocalDate billingDate;
    private final boolean paid;
    private final ArrayList<InvoiceItem> items;

    public Invoice(int id,
                   String clientName,
                   double totalAmount,
                   PaymentMode paymentMode,
                   LocalDate billingDate,
                   boolean paid,
                   List<InvoiceItem> items) {
        this.id = id;
        this.clientName = clientName;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
        this.billingDate = billingDate;
        this.paid = paid;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public int getId() {
        return id;
    }

    public String getClientName() {
        return clientName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public boolean isPaid() {
        return paid;
    }

    public List<InvoiceItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
