package heptathlon.client;

import heptathlon.common.model.Invoice;
import heptathlon.common.model.InvoiceItem;
import heptathlon.common.model.PaymentMode;
import heptathlon.common.model.Product;
import heptathlon.common.service.StoreService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class ClientMain {

    @FunctionalInterface
    private interface RemoteAction {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                StoreService service = (StoreService) registry.lookup("StoreService");
                ClientFrame frame = new ClientFrame(service);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Impossible de se connecter au serveur : " + e.getMessage(),
                        "Connexion serveur",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    private static final class ClientFrame extends JFrame {

        private static final Color APP_BACKGROUND = new Color(236, 242, 248);
        private static final Color PANEL_BACKGROUND = Color.WHITE;
        private static final Color PRIMARY_BLUE = new Color(0, 102, 204);
        private static final Color PRIMARY_BLUE_DARK = new Color(0, 63, 136);
        private static final Color ACCENT_ORANGE = new Color(255, 120, 41);
        private static final Color FIELD_BACKGROUND = new Color(247, 250, 253);
        private static final Color TEXT_COLOR = new Color(24, 33, 49);

        private final StoreService service;
        private final JTextArea resultArea;

        private final JTextField productReferenceField;
        private final JComboBox<String> familyComboBox;

        private final JTextField purchaseClientField;
        private final JTextField purchaseReferenceField;
        private final JSpinner purchaseQuantitySpinner;
        private final JComboBox<PaymentMode> purchasePaymentModeComboBox;

        private final JTextField invoiceIdField;
        private final JTextField paymentInvoiceIdField;
        private final JComboBox<PaymentMode> paymentModeComboBox;
        private final JSpinner revenueDateSpinner;

        private final JTextField stockReferenceField;
        private final JSpinner stockQuantitySpinner;
        private boolean suppressFamilySelectionEvents;

        private ClientFrame(StoreService service) throws Exception {
            super("Heptathlon");
            this.service = service;
            this.resultArea = createResultArea();
            this.productReferenceField = createTextField();
            this.familyComboBox = createFamilyComboBox();
            this.purchaseClientField = createTextField();
            this.purchaseReferenceField = createTextField();
            this.purchaseQuantitySpinner = createQuantitySpinner();
            this.purchasePaymentModeComboBox = new JComboBox<>(PaymentMode.values());
            this.invoiceIdField = createTextField();
            this.paymentInvoiceIdField = createTextField();
            this.paymentModeComboBox = new JComboBox<>(PaymentMode.values());
            this.revenueDateSpinner = createDateSpinner();
            this.stockReferenceField = createTextField();
            this.stockQuantitySpinner = createQuantitySpinner();

            configureFrame();
            setContentPane(createMainPanel());
            loadFamilies();
            log("Connexion au serveur reussie.");
        }

        private void configureFrame() {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(1180, 760));
            setLocationRelativeTo(null);
        }

        private JPanel createMainPanel() {
            JPanel root = new JPanel(new BorderLayout(18, 18));
            root.setBackground(APP_BACKGROUND);
            root.setBorder(new EmptyBorder(18, 18, 18, 18));
            root.add(createHeader(), BorderLayout.NORTH);
            root.add(createContentPanel(), BorderLayout.CENTER);
            return root;
        }

        private JPanel createHeader() {
            JPanel header = new GradientPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(187, 205, 228)),
                    new EmptyBorder(24, 28, 24, 28)
            ));

            JLabel titleLabel = new JLabel("Heptathlon");
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));

            JLabel subtitleLabel = new JLabel(
                    "Heptathlon est une enseigne specialisee dans la vente d'articles de sport et de loisir"
            );
            subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            subtitleLabel.setForeground(new Color(230, 238, 248));
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JLabel badgeLabel = new JLabel("SPORT  |  LOISIR  |  PERFORMANCE");
            badgeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            badgeLabel.setOpaque(true);
            badgeLabel.setBackground(ACCENT_ORANGE);
            badgeLabel.setForeground(Color.WHITE);
            badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badgeLabel.setBorder(new EmptyBorder(6, 10, 6, 10));

            header.add(titleLabel);
            header.add(Box.createVerticalStrut(8));
            header.add(subtitleLabel);
            header.add(Box.createVerticalStrut(14));
            header.add(badgeLabel);
            return header;
        }

        private JPanel createContentPanel() {
            JPanel panel = new JPanel(new BorderLayout(18, 0));
            panel.setOpaque(false);
            panel.add(createTabs(), BorderLayout.CENTER);
            panel.add(createResultsPanel(), BorderLayout.EAST);
            return panel;
        }

        private JTabbedPane createTabs() {
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
            tabs.setBackground(PANEL_BACKGROUND);
            tabs.setForeground(TEXT_COLOR);
            tabs.addTab("Produits", wrapTab(createProductsTab()));
            tabs.addTab("Achat", wrapTab(createPurchaseTab()));
            tabs.addTab("Factures", wrapTab(createInvoicesTab()));
            tabs.addTab("Chiffre d'affaires", wrapTab(createRevenueTab()));
            tabs.addTab("Stock", wrapTab(createStockTab()));
            return tabs;
        }

        private JScrollPane wrapTab(JPanel panel) {
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            return scrollPane;
        }

        private JPanel createProductsTab() {
            JPanel tab = createTabPanel();

            JPanel productPanel = createSectionPanel("Consulter un produit");
            GridBagConstraints productConstraints = createConstraints();
            addFieldRow(productPanel, productConstraints, 0, "Reference", productReferenceField);
            addButtonRow(productPanel, productConstraints, 1, createPrimaryButton("Consulter", this::handleShowProduct));

            JPanel familyPanel = createSectionPanel("Produits par famille");
            GridBagConstraints familyConstraints = createConstraints();
            addFieldRow(familyPanel, familyConstraints, 0, "Famille", familyComboBox);
            addButtonRow(familyPanel, familyConstraints, 1, createSecondaryButton("Rafraichir", this::handleRefreshFamilies));

            familyComboBox.addActionListener(event -> {
                if (!suppressFamilySelectionEvents) {
                    runRemoteAction(this::handleFamilySelection);
                }
            });

            tab.add(productPanel);
            tab.add(Box.createVerticalStrut(14));
            tab.add(familyPanel);
            tab.add(Box.createVerticalGlue());
            return tab;
        }

        private JPanel createPurchaseTab() {
            JPanel tab = createTabPanel();

            JPanel purchasePanel = createSectionPanel("Acheter un article");
            GridBagConstraints gbc = createConstraints();
            addFieldRow(purchasePanel, gbc, 0, "Client", purchaseClientField);
            addFieldRow(purchasePanel, gbc, 1, "Reference", purchaseReferenceField);
            addFieldRow(purchasePanel, gbc, 2, "Quantite", purchaseQuantitySpinner);
            addFieldRow(purchasePanel, gbc, 3, "Paiement direct", purchasePaymentModeComboBox);
            addButtonRow(
                    purchasePanel,
                    gbc,
                    4,
                    createActionRow(
                            createPrimaryButton("Acheter", this::handlePurchase),
                            createSecondaryButton("Acheter et payer", this::handlePurchaseAndPay)
                    )
            );

            tab.add(purchasePanel);
            tab.add(Box.createVerticalGlue());
            return tab;
        }

        private JPanel createInvoicesTab() {
            JPanel tab = createTabPanel();

            JPanel showInvoicePanel = createSectionPanel("Consulter une facture");
            GridBagConstraints showInvoiceConstraints = createConstraints();
            addFieldRow(showInvoicePanel, showInvoiceConstraints, 0, "Identifiant", invoiceIdField);
            addButtonRow(showInvoicePanel, showInvoiceConstraints, 1, createPrimaryButton("Consulter", this::handleShowInvoice));

            JPanel paymentPanel = createSectionPanel("Payer une facture");
            GridBagConstraints paymentConstraints = createConstraints();
            addFieldRow(paymentPanel, paymentConstraints, 0, "Facture", paymentInvoiceIdField);
            addFieldRow(paymentPanel, paymentConstraints, 1, "Mode", paymentModeComboBox);
            addButtonRow(paymentPanel, paymentConstraints, 2, createPrimaryButton("Payer", this::handlePayInvoice));

            tab.add(showInvoicePanel);
            tab.add(Box.createVerticalStrut(14));
            tab.add(paymentPanel);
            tab.add(Box.createVerticalGlue());
            return tab;
        }

        private JPanel createRevenueTab() {
            JPanel tab = createTabPanel();

            JPanel revenuePanel = createSectionPanel("Chiffre d'affaires journalier");
            GridBagConstraints revenueConstraints = createConstraints();
            addFieldRow(revenuePanel, revenueConstraints, 0, "Date", revenueDateSpinner);
            addButtonRow(
                    revenuePanel,
                    revenueConstraints,
                    1,
                    createActionRow(
                            createPrimaryButton("Calculer", this::handleShowRevenue),
                            createSecondaryButton("Statistiques", this::handleShowDailyStatistics)
                    )
            );

            tab.add(revenuePanel);
            tab.add(Box.createVerticalGlue());
            return tab;
        }

        private JPanel createStockTab() {
            JPanel tab = createTabPanel();

            JPanel stockPanel = createSectionPanel("Gestion du stock");
            GridBagConstraints gbc = createConstraints();
            addFieldRow(stockPanel, gbc, 0, "Reference", stockReferenceField);
            addFieldRow(stockPanel, gbc, 1, "Quantite", stockQuantitySpinner);
            addButtonRow(stockPanel, gbc, 2, createPrimaryButton("Ajouter", this::handleAddStock));
            addButtonRow(stockPanel, gbc, 3, createSecondaryButton("Afficher le stock", this::handleShowStock));

            tab.add(stockPanel);
            tab.add(Box.createVerticalGlue());
            return tab;
        }

        private JPanel createResultsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(420, 0));
            panel.setBackground(PANEL_BACKGROUND);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(201, 214, 231)),
                            BorderFactory.createMatteBorder(4, 0, 0, 0, ACCENT_ORANGE)
                    ),
                    new EmptyBorder(14, 16, 14, 16)
            ));

            JLabel title = new JLabel("Resultats");
            title.setForeground(TEXT_COLOR);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JScrollPane scrollPane = new JScrollPane(resultArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            panel.add(title, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createTabPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(APP_BACKGROUND);
            panel.setBorder(new EmptyBorder(16, 16, 16, 16));
            return panel;
        }

        private JPanel createSectionPanel(String title) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(PANEL_BACKGROUND);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    createTitledBorder(title),
                    new EmptyBorder(14, 16, 14, 16)
            ));
            return panel;
        }

        private TitledBorder createTitledBorder(String title) {
            TitledBorder border = BorderFactory.createTitledBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 5, 0, 0, PRIMARY_BLUE),
                            BorderFactory.createLineBorder(new Color(214, 222, 234))
                    ),
                    title
            );
            border.setTitleColor(PRIMARY_BLUE_DARK);
            border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
            return border;
        }

        private GridBagConstraints createConstraints() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 6, 8, 6);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            return gbc;
        }

        private void addFieldRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component field) {
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.weightx = 0;
            JLabel label = new JLabel(labelText);
            label.setForeground(TEXT_COLOR);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(field, gbc);
        }

        private void addButtonRow(JPanel panel, GridBagConstraints gbc, int row, Component component) {
            gbc.gridy = row;
            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(component, gbc);
            gbc.anchor = GridBagConstraints.WEST;
        }

        private JPanel createActionRow(Component... components) {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            for (int i = 0; i < components.length; i++) {
                panel.add(components[i]);
                if (i < components.length - 1) {
                    panel.add(Box.createHorizontalStrut(10));
                }
            }
            return panel;
        }

        private JTextField createTextField() {
            JTextField field = new JTextField(18);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setPreferredSize(new Dimension(220, 34));
            field.setBackground(FIELD_BACKGROUND);
            field.setForeground(TEXT_COLOR);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(187, 201, 219)),
                    new EmptyBorder(7, 10, 7, 10)
            ));
            return field;
        }

        private JComboBox<String> createFamilyComboBox() {
            JComboBox<String> comboBox = new JComboBox<>();
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            comboBox.setPreferredSize(new Dimension(220, 34));
            comboBox.setBackground(FIELD_BACKGROUND);
            comboBox.setForeground(TEXT_COLOR);
            return comboBox;
        }

        private JSpinner createQuantitySpinner() {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            spinner.setPreferredSize(new Dimension(220, 34));
            spinner.getEditor().getComponent(0).setBackground(FIELD_BACKGROUND);
            return spinner;
        }

        private JSpinner createDateSpinner() {
            JSpinner spinner = new JSpinner(new SpinnerDateModel());
            spinner.setValue(new Date());
            spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            spinner.setPreferredSize(new Dimension(220, 34));
            return spinner;
        }

        private JTextArea createResultArea() {
            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setBackground(new Color(250, 252, 255));
            area.setForeground(TEXT_COLOR);
            area.setBorder(new EmptyBorder(8, 8, 8, 8));
            return area;
        }

        private JButton createPrimaryButton(String text, RemoteAction action) {
            JButton button = createButton(text, PRIMARY_BLUE, Color.WHITE);
            button.addActionListener(event -> runRemoteAction(action));
            return button;
        }

        private JButton createSecondaryButton(String text, RemoteAction action) {
            JButton button = createButton(text, new Color(229, 237, 251), PRIMARY_BLUE_DARK);
            button.addActionListener(event -> runRemoteAction(action));
            return button;
        }

        private JButton createButton(String text, Color background, Color foreground) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setBackground(background);
            button.setForeground(foreground);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setPreferredSize(new Dimension(170, 42));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(background.equals(PRIMARY_BLUE) ? PRIMARY_BLUE_DARK : PRIMARY_BLUE),
                    new EmptyBorder(10, 22, 10, 22)
            ));
            return button;
        }

        private void runRemoteAction(RemoteAction action) {
            try {
                action.run();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError("Erreur lors de l'appel au serveur : " + e.getMessage());
            }
        }

        private void loadFamilies() throws Exception {
            String selectedFamily = (String) familyComboBox.getSelectedItem();
            suppressFamilySelectionEvents = true;
            familyComboBox.removeAllItems();
            for (String family : service.getAvailableFamilies()) {
                familyComboBox.addItem(family);
            }
            if (familyComboBox.getItemCount() > 0) {
                boolean restored = false;
                if (selectedFamily != null) {
                    for (int i = 0; i < familyComboBox.getItemCount(); i++) {
                        if (selectedFamily.equals(familyComboBox.getItemAt(i))) {
                            familyComboBox.setSelectedIndex(i);
                            restored = true;
                            break;
                        }
                    }
                }
                if (!restored) {
                    familyComboBox.setSelectedIndex(0);
                }
            }
            suppressFamilySelectionEvents = false;
        }

        private void handleRefreshFamilies() throws Exception {
            loadFamilies();
            log("Liste des familles rechargee.");
        }

        private void handleFamilySelection() throws Exception {
            String family = (String) familyComboBox.getSelectedItem();
            if (family == null || family.isBlank()) {
                return;
            }

            List<String> references = service.searchAvailableReferencesByFamily(family);
            StringBuilder builder = new StringBuilder();
            builder.append("Famille : ").append(family).append('\n');
            builder.append("--------------------------------------------------\n");

            if (references == null || references.isEmpty()) {
                builder.append("Aucun produit disponible dans cette famille.");
                log(builder.toString());
                return;
            }

            for (String reference : references) {
                Product product = service.getProductByReference(reference);
                if (product != null) {
                    builder.append(product.getReference())
                            .append(" | prix: ")
                            .append(String.format("%.2f", product.getUnitPrice()))
                            .append(" EUR | stock: ")
                            .append(product.getStockQuantity())
                            .append('\n');
                }
            }
            log(builder.toString());
        }

        private void handleShowProduct() throws Exception {
            String reference = requireText(productReferenceField, "la reference du produit");
            Product product = service.getProductByReference(reference);
            if (product == null) {
                log("Produit introuvable.");
                return;
            }
            log(formatProduct(product));
        }

        private void handlePurchase() throws Exception {
            processPurchase(false);
        }

        private void handlePurchaseAndPay() throws Exception {
            processPurchase(true);
        }

        private void processPurchase(boolean payImmediately) throws Exception {
            String clientName = requireText(purchaseClientField, "le nom du client");
            String reference = requireText(purchaseReferenceField, "la reference du produit");
            int quantity = requirePositiveSpinnerValue(purchaseQuantitySpinner, "la quantite");

            Invoice invoice = service.purchaseArticle(clientName, reference, quantity);
            if (invoice == null) {
                log("Achat impossible. Verifie le stock ou la reference du produit.");
                return;
            }

            paymentInvoiceIdField.setText(String.valueOf(invoice.getId()));
            invoiceIdField.setText(String.valueOf(invoice.getId()));

            if (payImmediately) {
                PaymentMode selectedMode = (PaymentMode) purchasePaymentModeComboBox.getSelectedItem();
                boolean paid = service.payInvoice(invoice.getId(), selectedMode);
                if (paid) {
                    invoice = service.getInvoiceById(invoice.getId());
                    paymentModeComboBox.setSelectedItem(selectedMode);
                    log("Achat et paiement enregistres.\n\n" + formatInvoice(invoice));
                } else {
                    log("Achat enregistre, mais le paiement immediat a echoue.\n\n" + formatInvoice(invoice));
                }
            } else {
                log("Achat enregistre.\nLa facture peut etre reglee plus tard en magasin depuis l'onglet Factures.\n\n"
                        + formatInvoice(invoice));
            }

            loadFamilies();
        }

        private void handleShowInvoice() throws Exception {
            int invoiceId = parsePositiveInteger(invoiceIdField.getText(), "l'identifiant de facture");
            Invoice invoice = service.getInvoiceById(invoiceId);
            if (invoice == null) {
                log("Facture introuvable.");
                return;
            }
            log(formatInvoice(invoice));
        }

        private void handlePayInvoice() throws Exception {
            int invoiceId = parsePositiveInteger(paymentInvoiceIdField.getText(), "l'identifiant de facture");
            PaymentMode mode = (PaymentMode) paymentModeComboBox.getSelectedItem();
            boolean paid = service.payInvoice(invoiceId, mode);
            if (!paid) {
                log("Paiement impossible.");
                return;
            }
            Invoice invoice = service.getInvoiceById(invoiceId);
            log("Facture payee.\n\n" + (invoice == null ? "" : formatInvoice(invoice)));
        }

        private void handleShowRevenue() throws Exception {
            LocalDate date = getSelectedRevenueDate();
            double revenue = service.getRevenueByDate(date);
            List<Invoice> invoices = service.getInvoicesByDate(date);
            StringBuilder builder = new StringBuilder();
            builder.append("Chiffre d'affaires du ").append(date).append('\n');
            builder.append("--------------------------------------------------\n");
            builder.append("Montant encaisse : ").append(String.format("%.2f", revenue)).append(" EUR\n");
            builder.append("Nombre de factures : ").append(invoices.size()).append('\n');
            builder.append("Factures payees : ").append(countPaidInvoices(invoices)).append('\n');
            builder.append("Factures en attente : ").append(invoices.size() - countPaidInvoices(invoices)).append('\n');
            log(builder.toString());
        }

        private void handleShowDailyStatistics() throws Exception {
            LocalDate date = getSelectedRevenueDate();
            List<Invoice> invoices = service.getInvoicesByDate(date);
            double paidRevenue = service.getRevenueByDate(date);
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
            } else {
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
            }

            log(builder.toString());
        }

        private void handleAddStock() throws Exception {
            String reference = requireText(stockReferenceField, "la reference du produit");
            int quantity = requirePositiveSpinnerValue(stockQuantitySpinner, "la quantite");
            boolean updated = service.addStock(reference, quantity);
            if (!updated) {
                log("Mise a jour du stock impossible.");
                return;
            }
            Product product = service.getProductByReference(reference);
            log("Stock mis a jour.\n\n" + (product == null ? reference : formatProduct(product)));
            loadFamilies();
        }

        private void handleShowStock() throws Exception {
            List<Product> products = service.getAllProducts();
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
            log(builder.toString());
        }

        private String requireText(JTextField field, String fieldName) {
            String value = field.getText();
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
            }
            return value.trim();
        }

        private int requirePositiveSpinnerValue(JSpinner spinner, String fieldName) {
            Object value = spinner.getValue();
            if (!(value instanceof Number number) || number.intValue() <= 0) {
                throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
            }
            return number.intValue();
        }

        private int parsePositiveInteger(String input, String fieldName) {
            try {
                int value = Integer.parseInt(input.trim());
                if (value <= 0) {
                    throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
                }
                return value;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Saisie invalide pour " + fieldName + ".");
            }
        }

        private LocalDate parseDate(String input) {
            try {
                return LocalDate.parse(input.trim());
            } catch (DateTimeParseException | NullPointerException e) {
                throw new IllegalArgumentException("Saisie invalide pour la date. Format attendu : YYYY-MM-DD.");
            }
        }

        private LocalDate getSelectedRevenueDate() {
            Date date = (Date) revenueDateSpinner.getValue();
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        private int countPaidInvoices(List<Invoice> invoices) {
            int count = 0;
            for (Invoice invoice : invoices) {
                if (invoice.isPaid()) {
                    count++;
                }
            }
            return count;
        }

        private String formatStockQuantity(Product product) {
            Integer stockQuantity = product.getStockQuantity();
            return stockQuantity == null ? "NULL" : String.valueOf(stockQuantity);
        }

        private String formatProduct(Product product) {
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

        private String formatInvoice(Invoice invoice) {
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

        private void log(String message) {
            resultArea.setText(message);
            resultArea.setCaretPosition(0);
        }

        private void showError(String message) {
            JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        private static final class GradientPanel extends JPanel {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, PRIMARY_BLUE_DARK, getWidth(), getHeight(), PRIMARY_BLUE));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 35));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawOval(getWidth() - 180, -30, 140, 140);
                g2d.drawOval(getWidth() - 250, 20, 110, 110);
                g2d.dispose();
                super.paintComponent(graphics);
            }

            private GradientPanel() {
                setOpaque(false);
            }
        }
    }
}
