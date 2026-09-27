import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AUTOMATED SUPPLIER RESTOCK REQUEST
 * Scans the `products` table for items at or below a reorder threshold
 * and automatically drafts restock request slips to send to suppliers.
 * Managers may also raise a manual request for any product.
 * Data is in-memory mock data pending database integration.
 */
public class SupplierRestockUI extends JFrame {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Product> products = new ArrayList<>();
    private final List<RestockRequest> requests = new ArrayList<>();
    private int nextProductId = 1;
    private int nextRequestId = 1;

    private DefaultTableModel productTableModel;
    private JTable productTable;
    private DefaultTableModel requestTableModel;
    private JTable requestTable;
    private JSpinner spnThreshold;
    private JComboBox<String> cmbManualProduct;
    private JSpinner spnManualQty;
    private JTextField txtManualSupplier;

    public SupplierRestockUI() {
        UITheme.applyGlobalDefaults();
        setTitle("Automated Supplier Restock Request");
        setSize(1040, 680);
        setMinimumSize(new Dimension(920, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerPanel("Automated Supplier Restock Request",
                "Detects low-stock products and drafts restock requests to suppliers"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildProductPanel(), buildRequestPanel());
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setBackground(UITheme.BACKGROUND);
        add(split, BorderLayout.CENTER);

        loadMockProducts();
        refreshProductTable();
        refreshManualProductPicker();
    }

    private JComponent buildProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 8, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setBackground(UITheme.BACKGROUND);
        JLabel title = new JLabel("Product Stock Levels");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_DARK);

        spnThreshold = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        spnThreshold.setPreferredSize(new Dimension(70, 26));
        spnThreshold.addChangeListener(e -> refreshProductTable());

        JButton btnGenerate = UITheme.button("Auto-Generate Restock Requests");
        btnGenerate.addActionListener(e -> autoGenerateRequests());

        top.add(title);
        top.add(Box.createRigidArea(new Dimension(20, 0)));
        top.add(new JLabel("Reorder threshold:"));
        top.add(spnThreshold);
        top.add(Box.createRigidArea(new Dimension(20, 0)));
        top.add(btnGenerate);
        panel.add(top, BorderLayout.NORTH);

        productTableModel = new DefaultTableModel(
                new Object[]{"Product ID", "Product Name", "Stock Qty", "Price", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        productTable = new JTable(productTableModel);
        UITheme.styleTable(productTable);
        productTable.setDefaultRenderer(Object.class, new LowStockRenderer());
        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(8, 16, 16, 16));

        JPanel manual = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        manual.setBackground(UITheme.BACKGROUND);
        manual.setBorder(UITheme.titled("Manual Restock Request"));

        cmbManualProduct = new JComboBox<>();
        cmbManualProduct.setPreferredSize(new Dimension(220, 28));
        spnManualQty = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
        spnManualQty.setPreferredSize(new Dimension(70, 26));
        txtManualSupplier = new JTextField(16);

        JButton btnAddManual = UITheme.button("Add Request");
        btnAddManual.addActionListener(e -> addManualRequest());

        manual.add(new JLabel("Product:"));
        manual.add(cmbManualProduct);
        manual.add(new JLabel("Qty:"));
        manual.add(spnManualQty);
        manual.add(new JLabel("Supplier:"));
        manual.add(txtManualSupplier);
        manual.add(btnAddManual);
        panel.add(manual, BorderLayout.NORTH);

        requestTableModel = new DefaultTableModel(
                new Object[]{"Req. ID", "Product", "Current Stock", "Requested Qty", "Supplier", "Request Date", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        requestTable = new JTable(requestTableModel);
        UITheme.styleTable(requestTable);
        JScrollPane scroll = new JScrollPane(requestTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        statusBar.setBackground(UITheme.BACKGROUND);
        JButton btnMarkSent = UITheme.secondaryButton("Mark Selected as Sent");
        JButton btnMarkReceived = UITheme.secondaryButton("Mark Selected as Received");
        btnMarkSent.addActionListener(e -> updateSelectedRequestStatus("Sent"));
        btnMarkReceived.addActionListener(e -> updateSelectedRequestStatus("Received"));
        statusBar.add(btnMarkSent);
        statusBar.add(btnMarkReceived);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    // ---------- data actions ----------

    private void loadMockProducts() {
        products.add(new Product(nextProductId++, "Salmon Onigiri", "Grilled salmon rice ball",
                new BigDecimal("55.00"), 8, "/img/salmon.png", LocalDateTime.now().minusDays(60)));
        products.add(new Product(nextProductId++, "Tuna Mayo Onigiri", "Tuna mayo rice ball",
                new BigDecimal("50.00"), 25, "/img/tuna.png", LocalDateTime.now().minusDays(55)));
        products.add(new Product(nextProductId++, "Umeboshi Onigiri", "Pickled plum rice ball",
                new BigDecimal("40.00"), 4, "/img/ume.png", LocalDateTime.now().minusDays(45)));
        products.add(new Product(nextProductId++, "Katsuobushi Onigiri", "Bonito flakes rice ball",
                new BigDecimal("45.00"), 30, "/img/katsuo.png", LocalDateTime.now().minusDays(30)));
        products.add(new Product(nextProductId++, "Seaweed Wrap (Nori) - 100pc", "Packaging supply",
                new BigDecimal("0.00"), 6, "/img/nori.png", LocalDateTime.now().minusDays(20)));
    }

    private int threshold() { return (int) spnThreshold.getValue(); }

    private void refreshProductTable() {
        productTableModel.setRowCount(0);
        for (Product p : products) {
            boolean low = p.getStockQuantity() <= threshold();
            productTableModel.addRow(new Object[]{
                    p.getProductID(), p.getProductName(), p.getStockQuantity(),
                    "P " + p.getPrice().setScale(2, java.math.RoundingMode.HALF_UP),
                    low ? "LOW STOCK" : "OK"
            });
        }
    }

    private void refreshManualProductPicker() {
        cmbManualProduct.removeAllItems();
        for (Product p : products) {
            cmbManualProduct.addItem(p.getProductID() + " - " + p.getProductName());
        }
    }

    private Product findProduct(int id) {
        for (Product p : products) if (p.getProductID() == id) return p;
        return null;
    }

    private void autoGenerateRequests() {
        int th = threshold();
        int created = 0;
        for (Product p : products) {
            if (p.getStockQuantity() <= th) {
                boolean alreadyPending = requests.stream().anyMatch(r ->
                        r.getProductID() == p.getProductID() && r.getStatus().equals("Pending"));
                if (alreadyPending) continue;
                int requestedQty = Math.max(th * 2 - p.getStockQuantity(), th);
                requests.add(0, new RestockRequest(nextRequestId++, p.getProductID(), p.getProductName(),
                        p.getStockQuantity(), requestedQty, "Default Supplier Co.", LocalDateTime.now(), "Pending"));
                created++;
            }
        }
        refreshRequestTable();
        String msg = created == 0
                ? "No new restock requests needed - all products are above the threshold or already pending."
                : "Generated " + created + " automatic restock request(s) for low-stock products.";
        JOptionPane.showMessageDialog(this, msg, "Auto-Generate Restock Requests", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addManualRequest() {
        Object sel = cmbManualProduct.getSelectedItem();
        if (sel == null) return;
        int id = Integer.parseInt(sel.toString().split(" - ")[0]);
        Product p = findProduct(id);
        if (p == null) return;
        String supplier = txtManualSupplier.getText().trim();
        if (supplier.isEmpty()) supplier = "Unspecified Supplier";
        int qty = (int) spnManualQty.getValue();
        requests.add(0, new RestockRequest(nextRequestId++, p.getProductID(), p.getProductName(),
                p.getStockQuantity(), qty, supplier, LocalDateTime.now(), "Pending"));
        refreshRequestTable();
        txtManualSupplier.setText("");
    }

    private void refreshRequestTable() {
        requestTableModel.setRowCount(0);
        for (RestockRequest r : requests) {
            requestTableModel.addRow(new Object[]{
                    r.getRequestID(), r.getProductName(), r.getCurrentStock(), r.getRequestedQuantity(),
                    r.getSupplierName(), r.getRequestDate().format(DATE_FMT), r.getStatus()
            });
        }
    }

    private void updateSelectedRequestStatus(String status) {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a restock request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int reqId = (int) requestTableModel.getValueAt(row, 0);
        for (RestockRequest r : requests) {
            if (r.getRequestID() == reqId) {
                r.setStatus(status);
                break;
            }
        }
        refreshRequestTable();
    }

    /** Highlights the "Status" column red when a product is low on stock. */
    private class LowStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            String statusVal = String.valueOf(tbl.getValueAt(row, 4));
            boolean low = statusVal.equals("LOW STOCK");
            if (!isSelected) {
                c.setBackground(low ? UITheme.LOW_STOCK_ROW : (row % 2 == 0 ? Color.WHITE : UITheme.TABLE_ALT_ROW));
                c.setForeground(low && col == 4 ? UITheme.ACCENT_DARK : UITheme.TEXT_DARK);
            }
            if (col == 4 && low) setFont(getFont().deriveFont(Font.BOLD));
            else setFont(getFont().deriveFont(Font.PLAIN));
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SupplierRestockUI().setVisible(true));
    }
}