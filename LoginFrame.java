import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LoginFrame extends JFrame {
    private final JTextField username = UITheme.field(16);
    private final JPasswordField password = UITheme.styleField(new JPasswordField(16));
    private final JLabel error = new JLabel(" ");

    private final List<User> users = List.of(
            new User("admin", "admin123", User.Role.ADMIN),
            new User("cashier", "cashier123", User.Role.CASHIER),
            new User("inventory", "inventory123", User.Role.INVENTORY_CLERK),
            new User("stock", "stock123", User.Role.STOCK_RECEIVING)
    );

    public LoginFrame() {
        setTitle("Omusubiya POS & Inventory System - Login");
        setSize(960, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new GridLayout(1, 2));
        add(buildBrandPanel());
        add(buildLoginPanel());
    }

    private JPanel buildBrandPanel() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, UITheme.ACCENT, w, h, UITheme.ACCENT_DARK));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillOval(-120, h - 260, 380, 380);
                g2.fillOval(w - 200, -140, 380, 380);
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillOval(w - 120, h - 200, 240, 240);

                MenuImages.paintOnigiri(g2, w / 2.0, h / 2.0 - 70, 150, new Color(0xF4B942));
                g2.setColor(Color.WHITE);
                g2.setFont(UITheme.font(Font.BOLD, 40));
                center(g2, "OMUSUBIYA", w, h / 2 + 60);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.setFont(UITheme.font(Font.PLAIN, 15));
                center(g2, "Sales \u2022 Inventory \u2022 Supply Stock-In", w, h / 2 + 92);
                g2.dispose();
            }

            private void center(Graphics2D g2, String s, int w, int y) {
                g2.drawString(s, (w - g2.getFontMetrics().stringWidth(s)) / 2, y);
            }
        };
    }

    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(340, 400));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back");
        title.setFont(UITheme.font(Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Sign in to access your authorized modules");
        sub.setForeground(UITheme.MUTED);

        username.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setForeground(UITheme.MUTED);
        show.setFont(UITheme.FONT_SMALL);
        char echo = password.getEchoChar();
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : echo));

        error.setForeground(UITheme.ACCENT);
        error.setFont(UITheme.FONT_SMALL);

        JButton login = UITheme.button("Sign In");
        login.setFont(UITheme.font(Font.BOLD, 15));
        login.setBorder(new EmptyBorder(12, 20, 12, 20));
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        login.addActionListener(e -> authenticate());
        getRootPane().setDefaultButton(login);

        JLabel demo = new JLabel("Demo: admin / admin123  \u2022  cashier / cashier123");
        demo.setFont(UITheme.FONT_SMALL);
        demo.setForeground(UITheme.MUTED);

        for (JComponent c : new JComponent[]{title, sub, username, password, show, error, login, demo}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        JLabel ul = label("Username"), pl = label("Password");

        box.add(title);
        box.add(Box.createVerticalStrut(6));
        box.add(sub);
        box.add(Box.createVerticalStrut(30));
        box.add(ul);
        box.add(Box.createVerticalStrut(6));
        box.add(username);
        box.add(Box.createVerticalStrut(16));
        box.add(pl);
        box.add(Box.createVerticalStrut(6));
        box.add(password);
        box.add(Box.createVerticalStrut(8));
        box.add(show);
        box.add(Box.createVerticalStrut(6));
        box.add(error);
        box.add(Box.createVerticalStrut(10));
        box.add(login);
        box.add(Box.createVerticalStrut(18));
        box.add(demo);
        outer.add(box);
        return outer;
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setFont(UITheme.FONT_HEADER);
        l.setForeground(UITheme.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
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
        error.setText("Invalid username or password.");
        password.setText("");
        password.requestFocusInWindow();
    }
}
