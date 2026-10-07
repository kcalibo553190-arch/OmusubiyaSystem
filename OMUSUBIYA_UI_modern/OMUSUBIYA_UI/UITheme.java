import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/** Shared look & feel: colors, fonts and rounded, flat components. No external libraries needed. */
public final class UITheme {
    public static final Color BACKGROUND = Color.decode("#F6F6F8");
    public static final Color SURFACE = Color.WHITE;
    public static final Color ACCENT = Color.decode("#C62B3B");
    public static final Color ACCENT_DARK = Color.decode("#A32230");
    public static final Color ACCENT_LIGHT = Color.decode("#FBE9EB");
    public static final Color TEXT_DARK = Color.decode("#1F2430");
    public static final Color MUTED = Color.decode("#6B7280");
    public static final Color BORDER = Color.decode("#E5E7EB");
    public static final Color SUCCESS = Color.decode("#168A4E");
    public static final Color WARNING = Color.decode("#D97706");

    private static final String FAMILY = pickFamily();
    public static final Font FONT_TITLE = font(Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = font(Font.PLAIN, 13);
    public static final Font FONT_HEADER = font(Font.BOLD, 14);
    public static final Font FONT_BODY = font(Font.PLAIN, 13);
    public static final Font FONT_SMALL = font(Font.PLAIN, 12);

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private UITheme() {}

    private static String pickFamily() {
        for (String f : new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Arial"}) {
            if (new Font(f, Font.PLAIN, 12).getFamily().equalsIgnoreCase(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, int size) { return new Font(FAMILY, style, size); }

    public static String peso(BigDecimal v) { return "\u20B1" + MONEY.format(v); }

    public static void applyGlobalDefaults() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_HEADER);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("Spinner.font", FONT_BODY);
        UIManager.put("CheckBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_HEADER);
        UIManager.put("ToolTip.font", FONT_SMALL);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }

    // ------------------------------------------------------------------ panels

    /** White rounded card with a hairline border and a soft bottom shadow (leaves 2px at the bottom). */
    public static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final boolean shadow;

        public RoundedPanel(LayoutManager lm, int arc, Color fill, boolean shadow) {
            super(lm);
            this.arc = arc; this.fill = fill; this.shadow = shadow;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), s = shadow ? 2 : 0;
            if (shadow) { g2.setColor(new Color(0, 0, 0, 14)); g2.fillRoundRect(0, s, w, h - s, arc, arc); }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h - s, arc, arc);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - s - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static RoundedPanel card(LayoutManager lm) { return new RoundedPanel(lm, 18, SURFACE, true); }

    public static JPanel headerPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FONT_TITLE);
        text.add(titleLabel);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel sub = new JLabel(subtitle);
            sub.setForeground(new Color(255, 255, 255, 215));
            sub.setFont(FONT_SUBTITLE);
            text.add(Box.createVerticalStrut(3));
            text.add(sub);
        }
        panel.add(text, BorderLayout.WEST);
        return panel;
    }

    /** Stat tile for dashboards. */
    public static JPanel statCard(String title, String value, String detail, Color tint) {
        RoundedPanel p = card(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(16, 18, 18, 18));
        JLabel t = new JLabel(title);
        t.setFont(FONT_SMALL);
        t.setForeground(MUTED);
        JLabel v = new JLabel(value);
        v.setFont(font(Font.BOLD, 28));
        v.setForeground(TEXT_DARK);
        JLabel d = new JLabel(detail);
        d.setFont(FONT_SMALL);
        d.setForeground(tint);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        p.add(d, BorderLayout.SOUTH);
        return p;
    }

    // ------------------------------------------------------------------ buttons

    public enum Kind { PRIMARY, OUTLINE, SOFT, HEADER }

    public static class RoundedButton extends JButton {
        private final Kind kind;

        public RoundedButton(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setFont(FONT_HEADER);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover(), down = getModel().isPressed();
            Color bg, fg, line = null;
            switch (kind) {
                case OUTLINE:
                    bg = down ? new Color(0xF3C9CE) : hover ? ACCENT_LIGHT : SURFACE; fg = ACCENT; line = ACCENT; break;
                case SOFT:
                    bg = down ? new Color(0xE2E4E9) : hover ? new Color(0xECEEF2) : new Color(0xF3F4F6); fg = TEXT_DARK; break;
                case HEADER:
                    bg = new Color(255, 255, 255, down ? 80 : hover ? 62 : 38); fg = Color.WHITE; line = new Color(255, 255, 255, 120); break;
                default:
                    bg = down ? ACCENT_DARK : hover ? new Color(0xB3253A) : ACCENT; fg = Color.WHITE;
            }
            if (!isEnabled()) { bg = new Color(0xE5E7EB); fg = new Color(0x9CA3AF); line = null; }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            if (line != null) {
                g2.setColor(line);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 13, 13);
            }
            g2.dispose();
            setForeground(fg);
            super.paintComponent(g);
        }
    }

    public static JButton button(String text) { return new RoundedButton(text, Kind.PRIMARY); }
    public static JButton secondaryButton(String text) { return new RoundedButton(text, Kind.OUTLINE); }
    public static JButton softButton(String text) { return new RoundedButton(text, Kind.SOFT); }
    public static JButton headerButton(String text) { return new RoundedButton(text, Kind.HEADER); }

    // ------------------------------------------------------------------ inputs

    public static class RoundedBorder implements Border {
        private final Color color;
        private final float thickness;

        public RoundedBorder(Color color, float thickness) { this.color = color; this.thickness = thickness; }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 12, 12);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(8, 12, 8, 12); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    public static <T extends JTextComponent> T styleField(T c) {
        c.setFont(FONT_BODY);
        c.setForeground(TEXT_DARK);
        c.setBackground(SURFACE);
        c.setCaretColor(ACCENT);
        final Border normal = new RoundedBorder(new Color(0xD1D5DB), 1f);
        final Border focus = new RoundedBorder(ACCENT, 2f);
        c.setBorder(normal);
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.setBorder(focus); }
            @Override public void focusLost(FocusEvent e) { c.setBorder(normal); }
        });
        return c;
    }

    public static JTextField field(int columns) { return styleField(new JTextField(columns)); }

    public static void styleCombo(JComboBox<?> box) {
        box.setFont(FONT_BODY);
        box.setBackground(SURFACE);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 36));
    }

    public static void styleSpinner(JSpinner s) {
        s.setFont(FONT_BODY);
        s.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB)));
        s.setPreferredSize(new Dimension(Math.max(70, s.getPreferredSize().width), 36));
    }

    // ------------------------------------------------------------------ tables & scrolling

    public static class TableRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            l.setBackground(sel ? ACCENT_LIGHT : SURFACE);
            l.setForeground(TEXT_DARK);
            boolean right = v instanceof Number || (v instanceof String && ((String) v).startsWith("\u20B1"));
            l.setHorizontalAlignment(right ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return l;
        }
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_DARK);
        table.setBackground(SURFACE);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(0xF0F1F4));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new TableRenderer());
        table.setDefaultRenderer(Number.class, new TableRenderer());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
                l.setBackground(new Color(0xF9FAFB));
                l.setForeground(MUTED);
                l.setFont(font(Font.BOLD, 12));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 12, 0, 12)));
                Object sample = t.getRowCount() > 0 ? t.getValueAt(0, c) : null;
                boolean right = sample instanceof Number || (sample instanceof String && ((String) sample).startsWith("\u20B1"));
                l.setHorizontalAlignment(right ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(SURFACE);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(0xC9CDD4); trackColor = SURFACE; }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d); b.setMinimumSize(d); b.setMaximumSize(d);
            return b;
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(0xA7ADB7) : thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }
}
