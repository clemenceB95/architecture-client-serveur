package heptathlon.client;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.PaymentMode;
import heptathlon.common.model.Product;
import heptathlon.common.model.PurchaseItem;
import heptathlon.common.service.StoreService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            StoreService service = (StoreService) registry.lookup("StoreService");

            System.out.println("Connexion au serveur reussie.");

            try (Scanner scanner = new Scanner(System.in)) {
                int choice;

                do {
                    displayMenu();

                    try {
                        choice = parseInteger(scanner.nextLine(), "le choix du menu");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                        choice = -1;
                        continue;
                    }

                    try {
                        switch (choice) {
                            case 1 -> showProduct(service, scanner);
                            case 2 -> searchProducts(service, scanner);
                            case 3 -> purchaseProduct(service, scanner);
                            case 4 -> payInvoice(service, scanner);
                            case 5 -> showInvoice(service, scanner);
                            case 6 -> showRevenue(service, scanner);
                            case 7 -> addStock(service, scanner);
                            case 0 -> System.out.println("Au revoir.");
                            default -> System.out.println("Choix invalide.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Erreur lors de l'appel au serveur : " + e.getMessage());
                    }

                } while (choice != 0);
            }

        } catch (Exception e) {
            System.out.println("Impossible de se connecter au serveur : " + e.getMessage());
        }
    }

    private static void displayMenu() {
        System.out.println();
        System.out.println("=== HEPTATHLON CLIENT ===");
        System.out.println("1. Consulter un produit");
        System.out.println("2. Rechercher des produits par famille");
        System.out.println("3. Acheter des articles");
        System.out.println("4. Payer une facture");
        System.out.println("5. Consulter une facture");
        System.out.println("6. Calculer le chiffre d'affaires d'une date");
        System.out.println("7. Ajouter du stock");
        System.out.println("0. Quitter");
        System.out.print("Choix : ");
    }

    private static void showProduct(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Reference du produit : ");
        String reference = readRequiredText(scanner, "la reference du produit");

        Product product = service.getProductByReference(reference);
        if (product == null) {
            System.out.println("Produit introuvable.");
            return;
        }

        System.out.println("Produit trouve :");
        System.out.println("Reference : " + product.getReference());
        System.out.println("Famille : " + product.getFamily());
        System.out.println("Prix unitaire : " + product.getUnitPrice());
        System.out.println("Stock : " + product.getStockQuantity());
    }

    private static void searchProducts(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Famille : ");
        String family = readRequiredText(scanner, "la famille");

        List<String> references = service.searchAvailableReferencesByFamily(family);
        if (references == null || references.isEmpty()) {
            System.out.println("Aucun produit disponible dans cette famille.");
            return;
        }

        System.out.println("Produits disponibles :");
        for (String ref : references) {
            System.out.println("- " + ref);
        }
    }

    private static void purchaseProduct(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Nom du client : ");
        String clientName = readRequiredText(scanner, "le nom du client");

        List<PurchaseItem> items = new ArrayList<>();
        boolean continuePurchase = true;

        while (continuePurchase) {
            System.out.print("Reference du produit : ");
            String reference = readRequiredText(scanner, "la reference du produit");

            System.out.print("Quantite : ");
            int quantity = parsePositiveInteger(scanner.nextLine(), "la quantite");

            items.add(new PurchaseItem(reference, quantity));

            System.out.print("Ajouter un autre article ? (o/n) : ");
            String answer = scanner.nextLine().trim();
            continuePurchase = answer.equalsIgnoreCase("o");
        }

        Invoice invoice = service.purchaseArticles(clientName, items);
        if (invoice == null) {
            System.out.println("Achat impossible. Verifiez les references, le stock ou les donnees saisies.");
            return;
        }

        System.out.println("Achat enregistre. Facture en attente de paiement :");
        printInvoice(invoice);
    }

    private static void payInvoice(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Identifiant de facture : ");
        int invoiceId = parsePositiveInteger(scanner.nextLine(), "l'identifiant de facture");

        PaymentMode[] modes = PaymentMode.values();
        System.out.println("Modes de paiement disponibles :");
        for (int i = 0; i < modes.length; i++) {
            System.out.println((i + 1) + ". " + modes[i]);
        }

        System.out.print("Mode de paiement : ");
        int modeChoice = parsePositiveInteger(scanner.nextLine(), "le mode de paiement");

        if (modeChoice < 1 || modeChoice > modes.length) {
            System.out.println("Mode de paiement invalide.");
            return;
        }

        boolean paid = service.payInvoice(invoiceId, modes[modeChoice - 1]);
        System.out.println(paid ? "Facture payee." : "Paiement impossible.");
    }

    private static void showInvoice(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Identifiant de facture : ");
        int invoiceId = parsePositiveInteger(scanner.nextLine(), "l'identifiant de facture");

        Invoice invoice = service.getInvoiceById(invoiceId);
        if (invoice == null) {
            System.out.println("Facture introuvable.");
            return;
        }

        printInvoice(invoice);
    }

    private static void showRevenue(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Date (YYYY-MM-DD) : ");
        LocalDate date = parseDate(scanner.nextLine());

        double revenue = service.getRevenueByDate(date);
        System.out.println("Chiffre d'affaires du " + date + " : " + revenue);
    }

    private static void addStock(StoreService service, Scanner scanner) throws Exception {
        System.out.print("Reference du produit : ");
        String reference = readRequiredText(scanner, "la reference du produit");

        System.out.print("Quantite a ajouter : ");
        int quantity = parsePositiveInteger(scanner.nextLine(), "la quantite");

        boolean updated = service.addStock(reference, quantity);
        System.out.println(updated ? "Stock mis a jour." : "Mise a jour impossible.");
    }

    private static String readRequiredText(Scanner scanner, String fieldName) {
        String value = scanner.nextLine();
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
        }
        return value.trim();
    }

    private static int parseInteger(String input, String fieldName) {
        try {
            return Integer.parseInt(input.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
        }
    }

    private static int parsePositiveInteger(String input, String fieldName) {
        int value = parseInteger(input, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(
                    "Saisie invalide pour " + fieldName + " : la valeur doit etre strictement positive."
            );
        }
        return value;
    }

    private static LocalDate parseDate(String input) {
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Saisie invalide pour la date. Format attendu : YYYY-MM-DD.");
        }
    }

    private static void printInvoice(Invoice invoice) {
        System.out.println("Facture #" + invoice.getId());
        System.out.println("Client : " + invoice.getClientName());
        System.out.println("Date : " + invoice.getBillingDate());
        System.out.println("Statut : " + (invoice.isPaid() ? "payee" : "en attente de paiement"));
        System.out.println("Mode de paiement : "
                + (invoice.getPaymentMode() == null ? "a definir" : invoice.getPaymentMode()));
        System.out.println("Articles :");

        List<InvoiceItem> items = invoice.getItems();
        if (items == null || items.isEmpty()) {
            System.out.println("- Aucun article");
        } else {
            for (InvoiceItem item : items) {
                System.out.println("- " + item.getProductReference()
                        + " x" + item.getQuantity()
                        + " @ " + item.getUnitPrice()
                        + " = " + item.getLineTotal());
            }
        }

        System.out.println("Total : " + invoice.getTotalAmount());
    }
}
