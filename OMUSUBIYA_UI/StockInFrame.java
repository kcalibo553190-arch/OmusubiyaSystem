import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockInFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Receipt No.", "Supplier", "Item", "Qty", "Unit Cost", "Date", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StockInFrame() {
        setTitle("Supply Stock-In"); setSize(1120, 680); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(UITheme.headerPanel("Supply Stock-In", "Record deliveries received from suppliers"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(10,10)); body.setBorder(new EmptyBorder(16,16,16,16));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5));
        JTextField supplier = new JTextField(14); JComboBox<String> item = new JComboBox<>(new String[]{"Rice","Salmon","Nori Sheets","Mayonnaise","Paper Cups"});
        JSpinner qty = new JSpinner(new SpinnerNumberModel(10,1,5000,1)); JTextField cost = new JTextField("0.00",8);
        JButton add = UITheme.button("Record Stock-In");
        form.add(new JLabel("Supplier:")); form.add(supplier); form.add(new JLabel("Item:")); form.add(item); form.add(new JLabel("Qty:")); form.add(qty); form.add(new JLabel("Unit Cost:")); form.add(cost); form.add(add);
        body.add(form, BorderLayout.NORTH);

        JTable table = new JTable(model); UITheme.styleTable(table); body.add(new JScrollPane(table), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER); loadDemo();

        add.addActionListener(e -> record(supplier,item,qty,cost));
    }

    private void loadDemo() {
        model.addRow(new Object[]{"SI-0001","ABC Foods","Rice",50,"₱58.00",LocalDateTime.now().minusHours(3).format(fmt),"Received"});
        model.addRow(new Object[]{"SI-0002","Nori Trading","Nori Sheets",100,"₱6.50",LocalDateTime.now().minusHours(1).format(fmt),"Received"});
    }

    private void record(JTextField supplier, JComboBox<String> item, JSpinner qty, JTextField cost) {
        if (supplier.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this,"Enter supplier name.","Validation",JOptionPane.WARNING_MESSAGE); return; }
        try {
            Double.parseDouble(cost.getText().trim());
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Enter a valid unit cost.","Validation",JOptionPane.WARNING_MESSAGE); return; }
        String receipt = String.format("SI-%04d", model.getRowCount()+1);
        model.addRow(new Object[]{receipt,supplier.getText().trim(),item.getSelectedItem(),qty.getValue(),"₱"+cost.getText().trim(),LocalDateTime.now().format(fmt),"Received"});
        supplier.setText(""); cost.setText("0.00"); qty.setValue(10);
    }
}
