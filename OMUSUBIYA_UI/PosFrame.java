import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class PosFrame extends JFrame {
    private final DefaultTableModel cartModel = new DefaultTableModel(new Object[]{"Item", "Price", "Qty", "Total"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable cart = new JTable(cartModel);
    private final JLabel totalLabel = new JLabel("₱0.00");
    private final Map<String, BigDecimal> products = new LinkedHashMap<>();

    public PosFrame() {
        setTitle("POS / Sales");
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(UITheme.headerPanel("POS / Sales", "Create customer orders and calculate totals"), BorderLayout.NORTH);

        products.put("Salmon Onigiri", new BigDecimal("55.00"));
        products.put("Tuna Mayo Onigiri", new BigDecimal("50.00"));
        products.put("Umeboshi Onigiri", new BigDecimal("40.00"));
        products.put("Katsuobushi Onigiri", new BigDecimal("45.00"));
        products.put("Iced Tea", new BigDecimal("35.00"));

        JPanel body = new JPanel(new BorderLayout(14, 14));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        top.setBackground(UITheme.BACKGROUND);
        JComboBox<String> product = new JComboBox<>(products.keySet().toArray(new String[0]));
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JButton add = UITheme.button("Add to Cart");
        add.addActionListener(e -> addItem((String) product.getSelectedItem(), (int) qty.getValue()));
        top.add(new JLabel("Product:")); top.add(product); top.add(new JLabel("Qty:")); top.add(qty); top.add(add);
        body.add(top, BorderLayout.NORTH);

        UITheme.styleTable(cart);
        body.add(new JScrollPane(cart), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UITheme.BACKGROUND);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actions.setBackground(UITheme.BACKGROUND);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        totalLabel.setForeground(UITheme.ACCENT_DARK);
        JButton remove = UITheme.secondaryButton("Remove Selected");
        remove.addActionListener(e -> removeSelected());
        JButton clear = UITheme.secondaryButton("Clear");
        clear.addActionListener(e -> { cartModel.setRowCount(0); updateTotal(); });
        JButton checkout = UITheme.button("Checkout");
        checkout.addActionListener(e -> checkout());
        actions.add(new JLabel("TOTAL:")); actions.add(totalLabel); actions.add(remove); actions.add(clear); actions.add(checkout);
        bottom.add(actions, BorderLayout.EAST);
        body.add(bottom, BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);
    }

    private void addItem(String name, int qty) {
        BigDecimal price = products.get(name);
        cartModel.addRow(new Object[]{name, "₱" + price, qty, "₱" + price.multiply(BigDecimal.valueOf(qty))});
        updateTotal();
    }

    private void removeSelected() {
        int row = cart.getSelectedRow();
        if (row >= 0) { cartModel.removeRow(row); updateTotal(); }
    }

    private void updateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            sum = sum.add(new BigDecimal(cartModel.getValueAt(i, 3).toString().replace("₱", "")));
        }
        totalLabel.setText("₱" + sum.setScale(2));
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Add at least one item.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Sale completed. Receipt generated for " + totalLabel.getText(), "Checkout", JOptionPane.INFORMATION_MESSAGE);
        cartModel.setRowCount(0);
        updateTotal();
    }
}
