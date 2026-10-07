import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardFrame extends JFrame {
    private final User user;

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("Omusubiya System Dashboard - " + friendlyRole(user.getRole()));
        setSize(1240, 760);
        setMinimumSize(new Dimension(1060, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);
    }

    // ------------------------------------------------------------ sidebar

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(250, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(24, 20, 20, 20));
        JComponent logo = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                MenuImages.paintOnigiri(g2, getWidth() / 2.0, getHeight() / 2.0 - 1, 26, new Color(0xF4B942));
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(44, 44));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel n = new JLabel("Omusubiya");
        n.setFont(UITheme.font(Font.BOLD, 18));
        JLabel s = new JLabel("POS & Inventory");
        s.setFont(UITheme.FONT_SMALL);
        s.setForeground(UITheme.MUTED);
        names.add(n);
        names.add(s);
        brand.add(logo, BorderLayout.WEST);
        brand.add(names, BorderLayout.CENTER);
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(4, 16, 0, 16));
        JLabel mod = new JLabel("MODULES");
        mod.setFont(UITheme.font(Font.BOLD, 11));
        mod.setForeground(UITheme.MUTED);
        mod.setBorder(new EmptyBorder(0, 4, 8, 0));
        nav.add(mod);
        nav.add(navButton("POS / Sales", "POS", () -> new PosFrame(user).setVisible(true)));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Inventory", "INVENTORY", () -> new InventoryFrame().setVisible(true)));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Supply Stock-In", "STOCKIN", () -> new StockInFrame().setVisible(true)));
        side.add(nav, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout(0, 12));
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(16, 20, 22, 20));
        JPanel chip = new JPanel(new BorderLayout(12, 0));
        chip.setOpaque(false);
        JComponent avatar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.ACCENT_DARK);
                g2.setFont(UITheme.font(Font.BOLD, 16));
                String t = user.getUsername().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        JLabel un = new JLabel(user.getUsername());
        un.setFont(UITheme.FONT_HEADER);
        JLabel ur = new JLabel(friendlyRole(user.getRole()));
        ur.setFont(UITheme.FONT_SMALL);
        ur.setForeground(UITheme.MUTED);
        who.add(un);
        who.add(ur);
        chip.add(avatar, BorderLayout.WEST);
        chip.add(who, BorderLayout.CENTER);

        JButton logout = UITheme.secondaryButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        foot.add(chip, BorderLayout.NORTH);
        foot.add(logout, BorderLayout.SOUTH);
        side.add(foot, BorderLayout.SOUTH);
        return side;
    }

    private JButton navButton(String text, String module, Runnable action) {
        JButton b = UITheme.softButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (canOpen(module)) b.addActionListener(e -> action.run());
        else { b.setEnabled(false); b.setToolTipText("This module is not available for your role."); }
        return b;
    }

    // ------------------------------------------------------------ main area

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 22));
        main.setBackground(UITheme.BACKGROUND);
        main.setBorder(new EmptyBorder(28, 32, 28, 32));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Welcome back, " + user.getUsername());
        welcome.setFont(UITheme.font(Font.BOLD, 28));
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH))
                + "  \u2022  " + friendlyRole(user.getRole()));
        date.setForeground(UITheme.MUTED);
        head.add(welcome);
        head.add(Box.createVerticalStrut(4));
        head.add(date);
        main.add(head, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 16));
        cards.setOpaque(false);
        cards.add(UITheme.statCard("Today\u2019s Sales", "\u20B124,580", "+12 transactions", UITheme.SUCCESS));
        cards.add(UITheme.statCard("Inventory Items", "128", "18 low-stock", UITheme.WARNING));
        cards.add(UITheme.statCard("Stock-In", "7", "3 pending", UITheme.MUTED));
        cards.add(UITheme.statCard("Open Orders", "9", "2 for pickup", UITheme.MUTED));

        JLabel quick = new JLabel("Quick access");
        quick.setFont(UITheme.font(Font.BOLD, 18));
        JPanel modules = new JPanel(new GridLayout(1, 3, 18, 18));
        modules.setOpaque(false);
        modules.add(new ModuleCard("pos", "POS / Sales", "Process customer orders, discounts and receipts",
                canOpen("POS"), () -> new PosFrame(user).setVisible(true)));
        modules.add(new ModuleCard("inventory", "Inventory", "Monitor stock levels and low-stock items",
                canOpen("INVENTORY"), () -> new InventoryFrame().setVisible(true)));
        modules.add(new ModuleCard("stockin", "Supply Stock-In", "Record deliveries from suppliers",
                canOpen("STOCKIN"), () -> new StockInFrame().setVisible(true)));

        JPanel lower = new JPanel(new BorderLayout(0, 14));
        lower.setOpaque(false);
        lower.add(quick, BorderLayout.NORTH);
        lower.add(modules, BorderLayout.CENTER);
        modules.setPreferredSize(new Dimension(0, 215));

        JPanel center = new JPanel(new BorderLayout(0, 26));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        JPanel lowerWrap = new JPanel(new BorderLayout());
        lowerWrap.setOpaque(false);
        lowerWrap.add(lower, BorderLayout.NORTH);
        center.add(lowerWrap, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    /** Large clickable tile that paints its own icon, title and description. */
    private static class ModuleCard extends JComponent {
        private final String kind, title, desc;
        private final boolean enabled;
        private boolean hover;

        ModuleCard(String kind, String title, String desc, boolean enabled, Runnable action) {
            this.kind = kind; this.title = title; this.desc = desc; this.enabled = enabled;
            setPreferredSize(new Dimension(260, 200));
            if (enabled) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { action.run(); }
                });
            } else {
                setToolTipText("This module is not available for your role.");
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight() - 3;
            g2.setColor(new Color(0, 0, 0, hover ? 30 : 14));
            g2.fillRoundRect(0, 3, w, h, 20, 20);
            g2.setColor(enabled ? Color.WHITE : new Color(0xF3F4F6));
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setColor(hover ? UITheme.ACCENT : UITheme.BORDER);
            g2.setStroke(new BasicStroke(hover ? 2f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

            int d = 56, x = 24, y = 24;
            g2.setColor(enabled ? UITheme.ACCENT : new Color(0xD1D5DB));
            g2.fillRoundRect(x, y, d, d, 18, 18);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + d / 2, cy = y + d / 2;
            switch (kind) {
                case "pos":
                    g2.setFont(UITheme.font(Font.BOLD, 28));
                    FontMetrics fm = g2.getFontMetrics();
                    String peso = "\u20B1";
                    g2.drawString(peso, cx - fm.stringWidth(peso) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
                    break;
                case "inventory":
                    g2.drawRoundRect(cx - 13, cy - 8, 26, 20, 5, 5);
                    g2.drawLine(cx - 13, cy - 1, cx + 13, cy - 1);
                    g2.drawLine(cx - 4, cy + 4, cx + 4, cy + 4);
                    g2.drawLine(cx - 9, cy - 14, cx + 9, cy - 14);
                    break;
                default:
                    g2.drawLine(cx, cy - 14, cx, cy + 6);
                    g2.drawLine(cx - 8, cy - 2, cx, cy + 6);
                    g2.drawLine(cx + 8, cy - 2, cx, cy + 6);
                    g2.drawLine(cx - 13, cy + 13, cx + 13, cy + 13);
            }

            g2.setColor(enabled ? UITheme.TEXT_DARK : UITheme.MUTED);
            g2.setFont(UITheme.font(Font.BOLD, 18));
            g2.drawString(title, 24, y + d + 38);
            g2.setColor(UITheme.MUTED);
            g2.setFont(UITheme.FONT_SMALL);
            drawWrapped(g2, desc, 24, y + d + 60, w - 48);

            g2.setFont(UITheme.font(Font.BOLD, 13));
            g2.setColor(enabled ? UITheme.ACCENT : UITheme.MUTED);
            g2.drawString(enabled ? "Open  \u2192" : "Not available for your role", 24, h - 18);
            g2.dispose();
        }

        private void drawWrapped(Graphics2D g2, String s, int x, int y, int max) {
            FontMetrics fm = g2.getFontMetrics();
            StringBuilder line = new StringBuilder();
            for (String word : s.split(" ")) {
                if (fm.stringWidth(line + word) > max && line.length() > 0) {
                    g2.drawString(line.toString().trim(), x, y);
                    y += fm.getHeight();
                    line.setLength(0);
                }
                line.append(word).append(' ');
            }
            g2.drawString(line.toString().trim(), x, y);
        }
    }

    private boolean canOpen(String module) {
        return switch (user.getRole()) {
            case ADMIN -> true;
            case CASHIER -> module.equals("POS");
            case INVENTORY_CLERK -> module.equals("INVENTORY") || module.equals("STOCKIN");
            case STOCK_RECEIVING -> module.equals("STOCKIN");
        };
    }

    private static String friendlyRole(User.Role role) {
        return switch (role) {
            case ADMIN -> "Administrator";
            case CASHIER -> "Cashier";
            case INVENTORY_CLERK -> "Inventory Clerk";
            case STOCK_RECEIVING -> "Stock Receiving Staff";
        };
    }
}
