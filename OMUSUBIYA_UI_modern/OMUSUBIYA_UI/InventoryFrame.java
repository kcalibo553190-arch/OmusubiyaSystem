import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class InventoryFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Item", "Category", "Stock", "Unit Cost", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return c == 0 || c == 3 ? Integer.class : String.class; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JPanel stats = new JPanel(new GridLayout(1, 3, 16, 16));

    public InventoryFrame() {
        setTitle("Inventory");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(UITheme.headerPanel("Inventory", "Monitor available stock and low-stock items"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        stats.setOpaque(false);

        JTextField search = UITheme.field(22);
        JButton refresh = UITheme.secondaryButton("Refresh");
        JButton adjust = UITheme.button("Stock Adjustment");
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        JLabel sl = new JLabel("Search");
        sl.setForeground(UITheme.MUTED);
        left.add(sl, BorderLayout.WEST);
        left.add(search, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(refresh);
        right.add(adjust);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        left.setPreferredSize(new Dimension(360, 40));

        UITheme.styleTable(table);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(5).setCellRenderer(new UITheme.TableRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                boolean low = "LOW STOCK".equals(v);
                l.setForeground(low ? UITheme.ACCENT : UITheme.SUCCESS);
                l.setFont(UITheme.FONT_HEADER);
                return l;
            }
        });
        JScrollPane sp = UITheme.scroll(table);
        UITheme.RoundedPanel tableCard = UITheme.card(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(6, 6, 8, 6));
        tableCard.add(sp, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(stats, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);
        body.add(top, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        loadData();
        refresh.addActionListener(e -> loadData());
        adjust.addActionListener(e -> adjustStock());
        search.getDocument().addDocumentListener(new DocumentListener() {
            void go() {
                String q = search.getText().trim();
                sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(q), 1, 2));
            }
            public void insertUpdate(DocumentEvent e) { go(); }
            public void removeUpdate(DocumentEvent e) { go(); }
            public void changedUpdate(DocumentEvent e) { go(); }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        addRow(101, "Salmon Onigiri", "Finished Good", 28, "55.00");
        addRow(102, "Tuna Mayo Onigiri", "Finished Good", 14, "50.00");
        addRow(103, "Nori Sheets", "Packaging", 8, "6.50");
        addRow(104, "Rice", "Raw Material", 72, "58.00");
        addRow(105, "Mayonnaise", "Raw Material", 6, "95.00");
        addRow(106, "Salmon", "Raw Material", 5, "240.00");
        refreshStats();
    }

    private void addRow(int id, String item, String cat, int stock, String cost) {
        model.addRow(new Object[]{id, item, cat, stock, UITheme.peso(new BigDecimal(cost)), status(stock)});
    }

    private static String status(int stock) { return stock <= 10 ? "LOW STOCK" : "OK"; }

    private void refreshStats() {
        int low = 0, units = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int s = (Integer) model.getValueAt(i, 3);
            units += s;
            if (s <= 10) low++;
        }
        stats.removeAll();
        stats.add(UITheme.statCard("Items tracked", String.valueOf(model.getRowCount()), "in inventory", UITheme.MUTED));
        stats.add(UITheme.statCard("Low stock", String.valueOf(low), low == 0 ? "all good" : "needs restocking",
                low == 0 ? UITheme.SUCCESS : UITheme.ACCENT));
        stats.add(UITheme.statCard("Total units", String.valueOf(units), "across all items", UITheme.MUTED));
        stats.revalidate();
        stats.repaint();
    }

    private void adjustStock() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the table first.", "Stock Adjustment", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        String item = model.getValueAt(row, 1).toString();
        String in = JOptionPane.showInputDialog(this, "Quantity to add to \"" + item + "\" (use a negative number to deduct):",
                "Stock Adjustment", JOptionPane.PLAIN_MESSAGE);
        if (in == null) return;
        try {
            int delta = Integer.parseInt(in.trim());
            int stock = Math.max(0, (Integer) model.getValueAt(row, 3) + delta);
            model.setValueAt(stock, row, 3);
            model.setValueAt(status(stock), row, 5);
            refreshStats();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a whole number.", "Stock Adjustment", JOptionPane.WARNING_MESSAGE);
        }
    }
}
