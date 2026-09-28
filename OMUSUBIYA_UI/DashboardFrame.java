import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final User user;

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("Omusubiya-style System Dashboard - " + user.getRole());
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = UITheme.headerPanel("Omusubiya System", "Sales, Inventory & Supply Stock-In");
        JButton logout = UITheme.secondaryButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        h.add(logout, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setBackground(UITheme.BACKGROUND);
        main.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel welcome = new JLabel("Welcome, " + user.getUsername() + "  •  Role: " + friendlyRole(user.getRole()));
        welcome.setFont(UITheme.FONT_HEADER);
        welcome.setForeground(UITheme.ACCENT_DARK);
        main.add(welcome, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 14));
        cards.setBackground(UITheme.BACKGROUND);
        cards.add(UITheme.card("Today’s Sales", "₱24,580", "+12 transactions"));
        cards.add(UITheme.card("Inventory Items", "128", "18 low-stock"));
        cards.add(UITheme.card("Stock-In", "7", "3 pending"));
        cards.add(UITheme.card("Open Orders", "9", "2 for pickup"));

        JPanel lower = new JPanel(new GridLayout(1, 3, 14, 14));
        lower.setBackground(UITheme.BACKGROUND);

        JButton pos = moduleButton("POS / Sales", "Process customer orders and receipts", () -> new PosFrame().setVisible(true));
        JButton inventory = moduleButton("Inventory", "Monitor stock levels and items", () -> new InventoryFrame().setVisible(true));
        JButton stockIn = moduleButton("Supply Stock-In", "Record supplier deliveries", () -> new StockInFrame().setVisible(true));

        if (!canOpen("POS")) disableCard(pos);
        if (!canOpen("INVENTORY")) disableCard(inventory);
        if (!canOpen("STOCKIN")) disableCard(stockIn);

        lower.add(pos);
        lower.add(inventory);
        lower.add(stockIn);

        JPanel center = new JPanel(new BorderLayout(14, 14));
        center.setBackground(UITheme.BACKGROUND);
        center.add(cards, BorderLayout.NORTH);
        center.add(lower, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);

        return main;
    }

    private JButton moduleButton(String title, String sub, Runnable action) {
        JButton b = UITheme.button("<html><center>" + title + "<br><font size='3'>" + sub + "</font></center></html>");
        b.setPreferredSize(new Dimension(260, 130));
        b.addActionListener(e -> action.run());
        return b;
    }

    private void disableCard(JButton b) {
        b.setEnabled(false);
        b.setToolTipText("This module is not available for your role.");
    }

    private boolean canOpen(String module) {
        return switch (user.getRole()) {
            case ADMIN -> true;
            case CASHIER -> module.equals("POS");
            case INVENTORY_CLERK -> module.equals("INVENTORY") || module.equals("STOCKIN");
            case STOCK_RECEIVING -> module.equals("STOCKIN");
        };
    }

    private String friendlyRole(User.Role role) {
        return switch (role) {
            case ADMIN -> "Administrator";
            case CASHIER -> "Cashier";
            case INVENTORY_CLERK -> "Inventory Clerk";
            case STOCK_RECEIVING -> "Stock Receiving Staff";
        };
    }
}
