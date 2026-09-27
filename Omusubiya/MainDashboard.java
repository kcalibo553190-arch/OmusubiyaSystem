import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Optional launcher that opens any of the four modules.
 * Not required by the assignment, but handy for demoing your part
 * of the groupwork without running each class's main() separately.
 */
public class MainDashboard extends JFrame {

    public MainDashboard() {
        UITheme.applyGlobalDefaults();
        setTitle("Omusubiya - System Dashboard");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerPanel("Onigiri Corner", "Rice Ball Shop Management System"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(UITheme.BACKGROUND);
        grid.setBorder(new EmptyBorder(30, 40, 30, 40));

        grid.add(moduleButton("Supplier Restock Request", () -> new SupplierRestockUI().setVisible(true)));
        grid.add(moduleButton("Employee Attendance", () -> new EmployeeAttendanceUI().setVisible(true)));
        grid.add(moduleButton("Customer Management", () -> new CustomerManagementUI().setVisible(true)));
        grid.add(moduleButton("Receipt / Order Slip", () -> new ReceiptUI().setVisible(true)));

        add(grid, BorderLayout.CENTER);
    }

    private JButton moduleButton(String label, Runnable onClick) {
        JButton b = UITheme.button("<html><center>" + label + "</center></html>");
        b.setFont(UITheme.FONT_HEADER);
        b.setPreferredSize(new Dimension(200, 100));
        b.addActionListener(e -> onClick.run());
        return b;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}
