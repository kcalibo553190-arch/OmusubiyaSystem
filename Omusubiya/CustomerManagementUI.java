import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CUSTOMER MANAGEMENT
 * Lets staff view, search, add, update and delete customer records.
 * Data is held in memory (mock list) since this build is not yet
 * connected to the database -- swap loadMockData()/save actions for
 * real DAO calls against the `customers` table later.
 */
public class CustomerManagementUI extends JFrame {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Customer> customers = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;
    private int nextId = 1;

    private JTextField txtFullName, txtContact, txtUsername, txtSearch;
    private JTextArea txtAddress;
    private JPasswordField txtPassword;
    private JLabel lblSelected;

    public CustomerManagementUI() {
        UITheme.applyGlobalDefaults();
        setTitle("Customer Management - Rice Ball Shop");
        setSize(980, 620);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerPanel("Customer Management", "View, search, add, update and remove customer accounts"), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        loadMockData();
        refreshTable(null);
    }

    private JComponent buildCenter() {
        JPanel wrapper = new JPanel(new BorderLayout(16, 16));
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));

        wrapper.add(buildTablePanel(), BorderLayout.CENTER);
        wrapper.add(buildFormPanel(), BorderLayout.EAST);
        return wrapper;
    }

    private JComponent buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.BACKGROUND);

        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(UITheme.BACKGROUND);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_HEADER);
        searchLbl.setForeground(UITheme.ACCENT_DARK);
        txtSearch = new JTextField();
        txtSearch.getDocument().addDocumentListener(new SimpleDocListener(() -> refreshTable(txtSearch.getText())));
        searchBar.add(searchLbl, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        panel.add(searchBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Full Name", "Contact", "Address", "Username", "Date Registered"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromSelection();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.BACKGROUND);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(UITheme.titled("Customer Details"));

        lblSelected = new JLabel("New Customer");
        lblSelected.setFont(UITheme.FONT_HEADER);
        lblSelected.setForeground(UITheme.ACCENT_DARK);
        lblSelected.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSelected);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        txtFullName = addField(panel, "Full Name");
        txtContact = addField(panel, "Contact Number");
        txtAddress = addTextArea(panel, "Address");
        txtUsername = addField(panel, "Username");
        txtPassword = new JPasswordField();
        addLabeledComponent(panel, "Password", txtPassword);

        panel.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setBackground(UITheme.BACKGROUND);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAdd = UITheme.button("Add");
        JButton btnUpdate = UITheme.button("Update");
        JButton btnDelete = UITheme.secondaryButton("Delete");
        JButton btnClear = UITheme.secondaryButton("Clear");

        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());

        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        panel.add(buttons);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BACKGROUND);
        outer.add(panel, BorderLayout.NORTH);
        return outer;
    }

    private JTextField addField(JPanel parent, String label) {
        JTextField field = new JTextField();
        addLabeledComponent(parent, label, field);
        return field;
    }

    private JTextArea addTextArea(JPanel parent, String label) {
        JTextArea area = new JTextArea(3, 1);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UITheme.FONT_BODY);
        JScrollPane sp = new JScrollPane(area);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
        parent.add(sp);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
        return area;
    }

    private void addLabeledComponent(JPanel parent, String label, JComponent comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        parent.add(lbl);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
        parent.add(comp);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    // ---------- data actions ----------

    private void loadMockData() {
        customers.add(new Customer(nextId++, "Hana Kimura", "0917-234-5678", "12 Sakura St, Davao City",
                "hana.k", "hashed_pw_1", LocalDateTime.now().minusDays(40)));
        customers.add(new Customer(nextId++, "Miguel Santos", "0928-555-1122", "45 Rizal Ave, Davao City",
                "msantos", "hashed_pw_2", LocalDateTime.now().minusDays(21)));
        customers.add(new Customer(nextId++, "Aiko Tanaka", "0933-888-9090", "8 Matina Rd, Davao City",
                "aikot", "hashed_pw_3", LocalDateTime.now().minusDays(5)));
    }

    private void refreshTable(String filter) {
        tableModel.setRowCount(0);
        String f = (filter == null) ? "" : filter.trim().toLowerCase();
        for (Customer c : customers) {
            boolean matches = f.isEmpty()
                    || c.getFullName().toLowerCase().contains(f)
                    || c.getUsername().toLowerCase().contains(f)
                    || c.getContact().toLowerCase().contains(f);
            if (matches) {
                tableModel.addRow(new Object[]{
                        c.getCustomerID(), c.getFullName(), c.getContact(), c.getAddress(),
                        c.getUsername(), c.getDateRegistered().format(DATE_FMT)
                });
            }
        }
    }

    private Customer getSelectedCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        for (Customer c : customers) if (c.getCustomerID() == id) return c;
        return null;
    }

    private void populateFormFromSelection() {
        Customer c = getSelectedCustomer();
        if (c == null) return;
        lblSelected.setText("Editing: Customer #" + c.getCustomerID());
        txtFullName.setText(c.getFullName());
        txtContact.setText(c.getContact());
        txtAddress.setText(c.getAddress());
        txtUsername.setText(c.getUsername());
        txtPassword.setText("");
    }

    private void clearForm() {
        table.clearSelection();
        lblSelected.setText("New Customer");
        txtFullName.setText("");
        txtContact.setText("");
        txtAddress.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
    }

    private boolean validateForm() {
        if (txtFullName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name and Username are required.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void addCustomer() {
        if (!validateForm()) return;
        String pw = new String(txtPassword.getPassword());
        Customer c = new Customer(nextId++, txtFullName.getText().trim(), txtContact.getText().trim(),
                txtAddress.getText().trim(), txtUsername.getText().trim(),
                pw.isEmpty() ? "hashed_pw_default" : "hashed_" + pw.hashCode(), LocalDateTime.now());
        customers.add(c);
        refreshTable(txtSearch.getText());
        clearForm();
    }

    private void updateCustomer() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Select a customer from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        c.setFullName(txtFullName.getText().trim());
        c.setContact(txtContact.getText().trim());
        c.setAddress(txtAddress.getText().trim());
        c.setUsername(txtUsername.getText().trim());
        String pw = new String(txtPassword.getPassword());
        if (!pw.isEmpty()) c.setPasswordHash("hashed_" + pw.hashCode());
        refreshTable(txtSearch.getText());
    }

    private void deleteCustomer() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Select a customer from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + c.getFullName() + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            customers.remove(c);
            refreshTable(txtSearch.getText());
            clearForm();
        }
    }

    /** Small helper so search-as-you-type doesn't need an anonymous DocumentListener each time. */
    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        SimpleDocListener(Runnable action) { this.action = action; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerManagementUI().setVisible(true));
    }
}
