import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AUTOMATED RECEIPT / ORDER SLIP
 * Picks a mock order (with its order_items) and automatically formats a
 * printable receipt: shop header, order/customer info, itemized list,
 * totals and payment status. Includes a working "Print" action.
 * Replace the mock order/order_item lists with real DAO results later.
 */
public class ReceiptUI extends JFrame {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

    private final List<Order> orders = new ArrayList<>();
    private final Map<Integer, List<OrderItem>> itemsByOrder = new HashMap<>();

    private JComboBox<String> cmbOrder;
    private JTextArea receiptArea;

    public ReceiptUI() {
        UITheme.applyGlobalDefaults();
        setTitle("Automated Receipt / Order Slip");
        setSize(560, 760);
        setMinimumSize(new Dimension(480, 640));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerPanel("Order Slip Generator", "Auto-formats a printable receipt from order data"), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadMockOrders();
        refreshOrderPicker();
        if (cmbOrder.getItemCount() > 0) {
            cmbOrder.setSelectedIndex(0);
            generateReceipt();
        }
    }

    private JComponent buildCenter() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setBackground(UITheme.BACKGROUND);
        cmbOrder = new JComboBox<>();
        cmbOrder.setPreferredSize(new Dimension(220, 28));
        JButton btnGenerate = UITheme.button("Generate Slip");
        JButton btnPrint = UITheme.secondaryButton("Print");
        btnGenerate.addActionListener(e -> generateReceipt());
        btnPrint.addActionListener(e -> printReceipt());

        controls.add(new JLabel("Select Order:"));
        controls.add(cmbOrder);
        controls.add(btnGenerate);
        controls.add(btnPrint);
        wrapper.add(controls, BorderLayout.NORTH);

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(UITheme.FONT_MONO);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setForeground(UITheme.TEXT_DARK);
        receiptArea.setMargin(new Insets(14, 14, 14, 14));

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT, 2));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ---------- data ----------

    private void loadMockOrders() {
        Order o1 = new Order(1001, 1, "Hana Kimura", LocalDateTime.now().minusHours(2),
                new BigDecimal("245.00"), "Completed", "Paid");
        Order o2 = new Order(1002, 2, "Miguel Santos", LocalDateTime.now().minusMinutes(30),
                new BigDecimal("95.00"), "Preparing", "Unpaid");
        orders.add(o1);
        orders.add(o2);

        List<OrderItem> items1 = new ArrayList<>();
        items1.add(new OrderItem(1, 1001, 1, "Salmon Onigiri", 2, new BigDecimal("55.00"), new BigDecimal("110.00")));
        items1.add(new OrderItem(2, 1001, 2, "Tuna Mayo Onigiri", 1, new BigDecimal("50.00"), new BigDecimal("50.00")));
        items1.add(new OrderItem(3, 1001, 4, "Katsuobushi Onigiri", 1, new BigDecimal("45.00"), new BigDecimal("45.00")));
        items1.add(new OrderItem(4, 1001, 3, "Umeboshi Onigiri", 1, new BigDecimal("40.00"), new BigDecimal("40.00")));
        itemsByOrder.put(1001, items1);

        List<OrderItem> items2 = new ArrayList<>();
        items2.add(new OrderItem(5, 1002, 2, "Tuna Mayo Onigiri", 1, new BigDecimal("50.00"), new BigDecimal("50.00")));
        items2.add(new OrderItem(6, 1002, 3, "Umeboshi Onigiri", 1, new BigDecimal("40.00"), new BigDecimal("40.00")));
        itemsByOrder.put(1002, items2);
    }

    private void refreshOrderPicker() {
        cmbOrder.removeAllItems();
        for (Order o : orders) {
            cmbOrder.addItem("#" + o.getOrderID() + " - " + o.getCustomerName());
        }
    }

    private Order selectedOrder() {
        Object sel = cmbOrder.getSelectedItem();
        if (sel == null) return null;
        int id = Integer.parseInt(sel.toString().substring(1).split(" - ")[0]);
        for (Order o : orders) if (o.getOrderID() == id) return o;
        return null;
    }

    private void generateReceipt() {
        Order order = selectedOrder();
        if (order == null) return;
        List<OrderItem> items = itemsByOrder.getOrDefault(order.getOrderID(), new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        String line = "----------------------------------------\n";

        sb.append(center("ONIGIRI CORNER")).append("\n");
        sb.append(center("Japanese Rice Ball Shop")).append("\n");
        sb.append(center("Davao City, Philippines")).append("\n");
        sb.append(center("Tel: (082) 123-4567")).append("\n");
        sb.append(line);
        sb.append("Order #   : ").append(order.getOrderID()).append("\n");
        sb.append("Date      : ").append(order.getOrderDate().format(DATE_FMT)).append("\n");
        sb.append("Customer  : ").append(order.getCustomerName()).append("\n");
        sb.append("Status    : ").append(order.getOrderStatus()).append("\n");
        sb.append(line);
        sb.append(String.format("%-20s %3s %8s%n", "Item", "Qty", "Amount"));
        sb.append(line);

        BigDecimal computedTotal = BigDecimal.ZERO;
        for (OrderItem it : items) {
            String name = it.getProductName();
            if (name.length() > 20) name = name.substring(0, 20);
            sb.append(String.format("%-20s %3d %8s%n", name, it.getQuantity(),
                    money(it.getSubTotal())));
            computedTotal = computedTotal.add(it.getSubTotal());
        }
        sb.append(line);
        sb.append(String.format("%-24s %8s%n", "TOTAL:", money(order.getTotalAmount())));
        sb.append(String.format("%-24s %8s%n", "Payment Status:", order.getPaymentStatus()));
        sb.append(line);
        sb.append(center("Thank you for your order!")).append("\n");
        sb.append(center("Come back soon - Onigiri Corner")).append("\n");

        receiptArea.setText(sb.toString());
        receiptArea.setCaretPosition(0);
    }

    private String money(BigDecimal amt) {
        return "P" + amt.setScale(2, RoundingMode.HALF_UP);
    }

    private String center(String text) {
        int width = 42;
        int pad = Math.max(0, (width - text.length()) / 2);
        return " ".repeat(pad) + text;
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((Printable) (graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.setFont(UITheme.FONT_MONO);
            String[] lines = receiptArea.getText().split("\n");
            int y = 14;
            for (String ln : lines) {
                g2.drawString(ln, 0, y);
                y += 14;
            }
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReceiptUI().setVisible(true));
    }
}
