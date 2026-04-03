package heptathlon.client;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.Product;
import heptathlon.common.model.PurchaseItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class ClientViewSupport {

    private ClientViewSupport() {
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
        }
        return value.trim();
    }

    static int requirePositiveNumber(Object value, String fieldName) {
        if (!(value instanceof Number number) || number.intValue() <= 0) {
            throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
        }
        return number.intValue();
    }

    static int parsePositiveInteger(String input, String fieldName) {
        try {
            int value = Integer.parseInt(requireText(input, fieldName));
            if (value <= 0) {
                throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
        }
    }

    static List<PurchaseItem> buildPurchaseItems(List<ProductQuantity> cartItems) {
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Ajoute au moins un produit au panier.");
        }

        List<PurchaseItem> items = new ArrayList<>();
        for (ProductQuantity item : cartItems) {
            items.add(new PurchaseItem(item.product().getReference(), item.quantity()));
        }
        return items;
    }

    static int countPaidInvoices(List<Invoice> invoices) {
        int count = 0;
        for (Invoice invoice : invoices) {
            if (invoice.isPaid()) {
                count++;
            }
        }
        return count;
    }

    static String formatFamilyProducts(String family, List<Product> products) {
        StringBuilder builder = new StringBuilder();
        builder.append("Famille : ").append(family).append('\n');
        builder.append("--------------------------------------------------\n");

        if (products == null || products.isEmpty()) {
            builder.append("Aucun produit disponible dans cette famille.");
            return builder.toString();
        }

        for (Product product : products) {
            builder.append(product.getReference())
                    .append(" | prix: ")
                    .append(String.format("%.2f", product.getUnitPrice()))
                    .append(" EUR | stock: ")
                    .append(product.getStockQuantity())
                    .append('\n');
        }
        return builder.toString();
    }

    static String formatRevenue(LocalDate date, double revenue, List<Invoice> invoices) {
        int paidInvoices = countPaidInvoices(invoices);
        StringBuilder builder = new StringBuilder();
        builder.append("Chiffre d'affaires du ").append(date).append('\n');
        builder.append("--------------------------------------------------\n");
        builder.append("Montant encaisse : ").append(String.format("%.2f", revenue)).append(" EUR\n");
        builder.append("Nombre de factures : ").append(invoices.size()).append('\n');
        builder.append("Factures payees : ").append(paidInvoices).append('\n');
        builder.append("Factures en attente : ").append(invoices.size() - paidInvoices).append('\n');
        return builder.toString();
    }

    static String formatDailyStatistics(LocalDate date, List<Invoice> invoices, double paidRevenue) {
        double totalBilled = invoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
        int paidCount = countPaidInvoices(invoices);
        int unpaidCount = invoices.size() - paidCount;

        StringBuilder builder = new StringBuilder();
        builder.append("Statistiques du ").append(date).append('\n');
        builder.append("==================================================\n");
        builder.append("Nombre total de factures : ").append(invoices.size()).append('\n');
        builder.append("Factures payees         : ").append(paidCount).append('\n');
        builder.append("Factures non payees     : ").append(unpaidCount).append('\n');
        builder.append("Montant total facture   : ").append(String.format("%.2f", totalBilled)).append(" EUR\n");
        builder.append("Chiffre d'affaires      : ").append(String.format("%.2f", paidRevenue)).append(" EUR\n");
        builder.append("Ticket moyen            : ")
                .append(String.format("%.2f", invoices.isEmpty() ? 0.0 : totalBilled / invoices.size()))
                .append(" EUR\n\n");

        if (invoices.isEmpty()) {
            builder.append("Aucune facture pour cette date.");
            return builder.toString();
        }

        builder.append("Liste des factures :\n");
        for (Invoice invoice : invoices) {
            builder.append("- #")
                    .append(invoice.getId())
                    .append(" | ")
                    .append(invoice.getClientName())
                    .append(" | ")
                    .append(invoice.isPaid() ? "payee" : "en attente")
                    .append(" | ")
                    .append(String.format("%.2f", invoice.getTotalAmount()))
                    .append(" EUR\n");
        }
        return builder.toString();
    }

    static String formatStock(List<Product> products) {
        StringBuilder builder = new StringBuilder();
        builder.append("Stock disponible\n");
        builder.append("--------------------------------------------------\n");

        for (Product product : products) {
            builder.append(product.getReference())
                    .append(" | ")
                    .append(product.getFamily())
                    .append(" | ")
                    .append(String.format("%.2f", product.getUnitPrice()))
                    .append(" EUR | stock: ")
                    .append(product.getStockQuantity())
                    .append('\n');
        }
        return builder.toString();
    }

    static String formatSelectedProductStock(Product product, int availableStock) {
        return "Famille : " + product.getFamily()
                + " | Prix : " + String.format("%.2f", product.getUnitPrice()) + " EUR"
                + " | Stock disponible : " + availableStock;
    }

    static String formatCartItem(Product product, int quantity) {
        return "Quantite : x" + quantity
                + " | Total : " + String.format("%.2f", product.getUnitPrice() * quantity) + " EUR";
    }

    static String formatCartTotal(List<ProductQuantity> cartItems) {
        double total = 0;
        for (ProductQuantity item : cartItems) {
            total += item.product().getUnitPrice() * item.quantity();
        }
        return "Total panier : " + String.format("%.2f", total) + " EUR";
    }

    static String formatStockQuantity(Product product) {
        Integer stockQuantity = product.getStockQuantity();
        return stockQuantity == null ? "NULL" : String.valueOf(stockQuantity);
    }

    static String formatProduct(Product product) {
        return """
                Produit
                --------------------------------------------------
                Reference : %s
                Famille   : %s
                Prix      : %.2f EUR
                Stock     : %s
                """.formatted(
                product.getReference(),
                product.getFamily(),
                product.getUnitPrice(),
                formatStockQuantity(product)
        ).trim();
    }

    static String formatInvoice(Invoice invoice) {
        StringBuilder builder = new StringBuilder();
        builder.append("Facture #").append(invoice.getId()).append('\n');
        builder.append("Client : ").append(invoice.getClientName()).append('\n');
        builder.append("Date   : ").append(invoice.getBillingDate()).append('\n');
        builder.append("Statut : ").append(invoice.isPaid() ? "payee" : "en attente de paiement").append('\n');
        builder.append("Mode   : ")
                .append(invoice.getPaymentMode() == null ? "a definir" : invoice.getPaymentMode())
                .append('\n');
        builder.append("Articles :\n");

        List<InvoiceItem> items = invoice.getItems();
        if (items == null || items.isEmpty()) {
            builder.append("- Aucun article\n");
        } else {
            for (InvoiceItem item : items) {
                builder.append("- ")
                        .append(item.getProductReference())
                        .append(" x")
                        .append(item.getQuantity())
                        .append(" @ ")
                        .append(String.format("%.2f", item.getUnitPrice()))
                        .append(" EUR = ")
                        .append(String.format("%.2f", item.getLineTotal()))
                        .append(" EUR\n");
            }
        }

        builder.append("Total : ").append(String.format("%.2f", invoice.getTotalAmount())).append(" EUR");
        return builder.toString();
    }

    record ProductQuantity(Product product, int quantity) {
    }
}
