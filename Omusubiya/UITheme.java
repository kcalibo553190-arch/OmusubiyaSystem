import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Shared visual theme for the Rice Ball Shop system.
 * Background : #FFFFFF (white)
 * Foreground/Accent : #C62B3B (rice-ball red)
 */
public class UITheme {

    public static final Color BACKGROUND   = Color.decode("#FFFFFF");
    public static final Color ACCENT       = Color.decode("#C62B3B");
    public static final Color ACCENT_DARK  = Color.decode("#A32230");
    public static final Color ACCENT_LIGHT = Color.decode("#F6D9DC");
    public static final Color TEXT_ON_ACCENT = Color.WHITE;
    public static final Color TEXT_DARK    = new Color(45, 45, 45);
    public static final Color TABLE_ALT_ROW = new Color(253, 238, 239);
    public static final Color LOW_STOCK_ROW = new Color(255, 198, 203);
    public static final Color BORDER_COLOR = Color.decode("#C62B3B");

    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 13);

    /** Standard accent-colored button used across every screen. */
    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(TEXT_ON_ACCENT);
        b.setFont(FONT_HEADER);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Secondary (outline-style) button for less prominent actions. */
    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(ACCENT);
        b.setFont(FONT_HEADER);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Page header bar with a title and optional subtitle. */
    public static JPanel headerPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ACCENT);
        panel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_ON_ACCENT);
        titleLabel.setFont(FONT_TITLE);
        textPanel.add(titleLabel);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setForeground(TEXT_ON_ACCENT);
            subLabel.setFont(FONT_SUBTITLE);
            textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            textPanel.add(subLabel);
        }

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    /** Applies consistent styling to a JTable. */
    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setBackground(ACCENT);
        table.getTableHeader().setForeground(TEXT_ON_ACCENT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }

    public static TitledBorder titled(String text) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT, 1), text);
        tb.setTitleFont(FONT_HEADER);
        tb.setTitleColor(ACCENT_DARK);
        return tb;
    }

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
    }
}