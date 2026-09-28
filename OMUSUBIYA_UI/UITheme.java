import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class UITheme {
    public static final Color BACKGROUND = Color.decode("#FFFFFF");
    public static final Color ACCENT = Color.decode("#C62B3B");
    public static final Color ACCENT_DARK = Color.decode("#A32230");
    public static final Color ACCENT_LIGHT = Color.decode("#F6D9DC");
    public static final Color TEXT_DARK = new Color(45, 45, 45);
    public static final Color MUTED = new Color(110, 110, 110);
    public static final Color BORDER = new Color(225, 225, 225);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);

    private UITheme() {}

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_HEADER);
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_HEADER);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(ACCENT);
        b.setFont(FONT_HEADER);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new LineBorder(ACCENT, 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel headerPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ACCENT);
        panel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FONT_TITLE);
        text.add(titleLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel sub = new JLabel(subtitle);
            sub.setForeground(Color.WHITE);
            sub.setFont(FONT_SUBTITLE);
            text.add(Box.createVerticalStrut(4));
            text.add(sub);
        }
        panel.add(text, BorderLayout.WEST);
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setBackground(ACCENT);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    public static JPanel card(String title, String value, String detail) {
        JPanel p = new JPanel(new BorderLayout(8, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel t = new JLabel(title);
        t.setFont(FONT_SUBTITLE);
        t.setForeground(MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 24));
        v.setForeground(ACCENT_DARK);
        JLabel d = new JLabel(detail);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        d.setForeground(MUTED);

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        p.add(d, BorderLayout.SOUTH);
        return p;
    }
}
