import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    private final List<User> users = List.of(
            new User("admin", "admin123", User.Role.ADMIN),
            new User("cashier", "cashier123", User.Role.CASHIER),
            new User("inventory", "inventory123", User.Role.INVENTORY_CLERK),
            new User("stock", "stock123", User.Role.STOCK_RECEIVING)
    );

    public LoginFrame() {
        setTitle("Omusubiya-style POS & Inventory System - Login");
        setSize(900, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new GridLayout(1, 2));

        add(buildBrandPanel());
        add(buildLoginPanel());
    }

    private JPanel buildBrandPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.ACCENT);
        p.setBorder(new EmptyBorder(45, 45, 45, 45));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("OMUSUBIYA");
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 34));

        JLabel system = new JLabel("Sales • Inventory • Supply Stock-In");
        system.setAlignmentX(Component.CENTER_ALIGNMENT);
        system.setForeground(Color.WHITE);
        system.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        center.add(Box.createVerticalGlue());
        center.add(brand);
        center.add(Box.createVerticalStrut(10));
        center.add(system);
        center.add(Box.createVerticalGlue());
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(320, 340));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Role-Based Login");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to access your authorized modules");
        sub.setForeground(UITheme.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        username.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton login = UITheme.button("LOGIN");
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.addActionListener(e -> authenticate());

        JLabel demo = new JLabel("Demo: admin/admin123 • cashier/cashier123");
        demo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demo.setForeground(UITheme.MUTED);
        demo.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(6));
        box.add(sub);
        box.add(Box.createVerticalStrut(28));
        box.add(new JLabel("Username"));
        box.add(Box.createVerticalStrut(5));
        box.add(username);
        box.add(Box.createVerticalStrut(14));
        box.add(new JLabel("Password"));
        box.add(Box.createVerticalStrut(5));
        box.add(password);
        box.add(Box.createVerticalStrut(20));
        box.add(login);
        box.add(Box.createVerticalStrut(18));
        box.add(demo);

        outer.add(box);
        return outer;
    }

    private void authenticate() {
        String u = username.getText().trim();
        String p = new String(password.getPassword());

        for (User user : users) {
            if (user.matches(u, p)) {
                new DashboardFrame(user).setVisible(true);
                dispose();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
