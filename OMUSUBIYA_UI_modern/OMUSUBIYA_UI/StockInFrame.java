import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockInFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Receipt No.", "Supplier", "Item", "Qty", "Unit Cost", "Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return c == 3 ? Integer.class : String.class; }
    };
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StockInFrame() {
        setTitle("Supply Stock-In");
        setSize(1160, 700);
        setMinimumSize(new Dimension(980, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(UITheme.headerPanel("Supply Stock-In", "Record deliveries received from suppliers"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField supplier = UITheme.field(14);
        JComboBox<String> item = new JComboBox<>(new String[]{"Rice", "Salmon", "Nori Sheets", "Mayonnaise", "Paper Cups"});
        UITheme.styleCombo(item);
        JSpinner qty = new JSpinner(new SpinnerNumberModel(10, 1, 5000, 1));
        UITheme.styleSpinner(qty);
        JTextField cost = UITheme.field(8);
        cost.setText("0.00");
        JButton add = UITheme.button("Record Stock-In");

        UITheme.RoundedPanel formCard = UITheme.card(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(16, 18, 18, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 6, 0, 6);
        addField(formCard, g, 0, "Supplier", supplier, 2.0);
        addField(formCard, g, 1, "Item", item, 1.2);
        addField(formCard, g, 2, "Quantity", qty, 0.6);
        addField(formCard, g, 3, "Unit cost (\u20B1)", cost, 0.8);
        g.gridx = 4; g.gridy = 1; g.weightx = 0;
        formCard.add(add, g);
        body.add(formCard, BorderLayout.NORTH);

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(new UITheme.TableRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(UITheme.SUCCESS);
                l.setFont(UITheme.FONT_HEADER);
                return l;
            }
        });
        UITheme.RoundedPanel tableCard = UITheme.card(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(6, 6, 8, 6));
        tableCard.add(UITheme.scroll(table), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        loadDemo();
        add.addActionListener(e -> record(supplier, item, qty, cost));
    }

    private void addField(JPanel p, GridBagConstraints g, int col, String label, JComponent comp, double weight) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED);
        g.gridx = col; g.weightx = weight;
        g.gridy = 0; p.add(l, g);
        g.gridy = 1; g.insets = new Insets(3, 6, 0, 6); p.add(comp, g);
        g.insets = new Insets(0, 6, 0, 6);
    }

    private void loadDemo() {
        addRow("SI-0001", "ABC Foods", "Rice", 50, new BigDecimal("58.00"), LocalDateTime.now().minusHours(3));
        addRow("SI-0002", "Nori Trading", "Nori Sheets", 100, new BigDecimal("6.50"), LocalDateTime.now().minusHours(1));
    }

    private void addRow(String no, String supplier, String item, int qty, BigDecimal cost, LocalDateTime when) {
        model.addRow(new Object[]{no, supplier, item, qty, UITheme.peso(cost), when.format(fmt), "Received"});
    }

    private void record(JTextField supplier, JComboBox<String> item, JSpinner qty, JTextField cost) {
        if (supplier.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter supplier name.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal unit;
        try {
            unit = new BigDecimal(cost.getText().trim().replace(",", "").replace("\u20B1", "")).setScale(2, RoundingMode.HALF_UP);
            if (unit.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid unit cost greater than zero.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        addRow(String.format("SI-%04d", model.getRowCount() + 1), supplier.getText().trim(),
                (String) item.getSelectedItem(), (Integer) qty.getValue(), unit, LocalDateTime.now());
        supplier.setText("");
        cost.setText("0.00");
        qty.setValue(10);
    }
}
