import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Item", "Category", "Stock", "Unit Cost", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public InventoryFrame() {
        setTitle("Inventory"); setSize(1050, 660); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(UITheme.headerPanel("Inventory", "Monitor available stock and low-stock items"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBorder(new EmptyBorder(16,16,16,16));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,4));
        JTextField search = new JTextField(22);
        JButton refresh = UITheme.secondaryButton("Refresh");
        top.add(new JLabel("Search:")); top.add(search); top.add(refresh);
        body.add(top, BorderLayout.NORTH);

        JTable table = new JTable(model); UITheme.styleTable(table);
        body.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton adjust = UITheme.button("Stock Adjustment");
        adjust.addActionListener(e -> JOptionPane.showMessageDialog(this, "Stock adjustment screen can be connected to MySQL later."));
        body.add(adjust, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        loadData();
        refresh.addActionListener(e -> loadData());
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(search.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(search.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(search.getText()); }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        addRow(101,"Salmon Onigiri","Finished Good",28,"55.00");
        addRow(102,"Tuna Mayo Onigiri","Finished Good",14,"50.00");
        addRow(103,"Nori Sheets","Packaging",8,"6.50");
        addRow(104,"Rice","Raw Material",72,"58.00");
        addRow(105,"Mayonnaise","Raw Material",6,"95.00");
        addRow(106,"Salmon","Raw Material",5,"240.00");
    }

    private void addRow(int id, String item, String cat, int stock, String cost) {
        model.addRow(new Object[]{id,item,cat,stock,"₱"+cost,stock <= 10 ? "LOW STOCK" : "OK"});
    }

    private void filter(String q) {
        String query = q.trim().toLowerCase();
        for (int i=model.getRowCount()-1;i>=0;i--) {
            String item = model.getValueAt(i,1).toString().toLowerCase();
            if (!item.contains(query)) model.removeRow(i);
        }
        if (query.isEmpty()) loadData();
    }
}
