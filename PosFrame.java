import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PosFrame extends JFrame {
    private static final int RECEIPT_WIDTH = 42;

    private final User user;
    private final List<Product> menu = MenuCatalog.load();
    private final CartModel cartModel = new CartModel();
    private final JTable cart = new JTable(cartModel);

    private final JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JComboBox<Discount> discountBox = new JComboBox<>(Discount.values());
    private final JTextField custName = UITheme.field(12);
    private final JTextField custId = UITheme.field(12);
    private final JLabel idLabel = new JLabel("ID No.");
    private final JComboBox<String> payment = new JComboBox<>(new String[]{"Cash", "GCash", "Maya", "Card"});
    private final JTextField tendered = UITheme.field(8);

    private final JLabel subtotalVal = value(false), vatVal = value(false), discVal = value(false), changeVal = value(false);
    private final JLabel totalVal = new JLabel("\u20B10.00");

    private String category = "All";
    private String query = "";

    public PosFrame(User user) {
        this.user = user;
        setTitle("POS / Sales");
        setSize(1260, 780);
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(UITheme.headerPanel("POS / Sales", "Tap a menu item to add it to the order"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(18, 22, 18, 22));
        body.add(buildMenuPanel(), BorderLayout.CENTER);
        body.add(buildOrderPanel(), BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        rebuildGrid();
        updateDiscountUi();
        updateTotals();
    }

    // ================================================================= menu (left)

    private JPanel buildMenuPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(14, 0));
        top.setOpaque(false);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        List<String> cats = new ArrayList<>();
        cats.add("All");
        cats.addAll(MenuCatalog.categories(menu));
        for (String c : cats) {
            JToggleButton chip = new JToggleButton(c) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected() ? UITheme.ACCENT : UITheme.SURFACE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                    g2.setColor(isSelected() ? UITheme.ACCENT : UITheme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                    g2.dispose();
                    setForeground(isSelected() ? Color.WHITE : UITheme.TEXT_DARK);
                    super.paintComponent(g);
                }
            };
            chip.setFont(UITheme.FONT_HEADER);
            chip.setFocusPainted(false);
            chip.setContentAreaFilled(false);
            chip.setBorderPainted(false);
            chip.setOpaque(false);
            chip.setBorder(new EmptyBorder(8, 18, 8, 18));
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.setSelected(c.equals(category));
            chip.addActionListener(e -> { category = c; rebuildGrid(); });
            group.add(chip);
            chips.add(chip);
        }

        JTextField search = UITheme.field(16);
        search.setToolTipText("Search menu");
        search.getDocument().addDocumentListener(new DocumentListener() {
            void go() { query = search.getText().trim().toLowerCase(); rebuildGrid(); }
            public void insertUpdate(DocumentEvent e) { go(); }
            public void removeUpdate(DocumentEvent e) { go(); }
            public void changedUpdate(DocumentEvent e) { go(); }
        });
        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setOpaque(false);
        JLabel sl = new JLabel("Search");
        sl.setForeground(UITheme.MUTED);
        searchWrap.add(sl, BorderLayout.WEST);
        searchWrap.add(search, BorderLayout.CENTER);

        top.add(chips, BorderLayout.CENTER);
        top.add(searchWrap, BorderLayout.EAST);
        left.add(top, BorderLayout.NORTH);

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        grid.setOpaque(false);
        gridWrap.add(grid, BorderLayout.NORTH);
        JScrollPane sp = UITheme.scroll(gridWrap);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setViewportBorder(null);
        left.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("Add photos to the \"images\" folder (e.g. salmon-onigiri.jpg) and edit menu.csv to change items and prices.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.MUTED);
        left.add(hint, BorderLayout.SOUTH);
        return left;
    }

    private void rebuildGrid() {
        grid.removeAll();
        for (Product p : menu) {
            boolean catOk = category.equals("All") || p.category().equals(category);
            boolean qOk = query.isEmpty() || p.name().toLowerCase().contains(query);
            if (catOk && qOk) grid.add(new ProductCard(p));
        }
        if (grid.getComponentCount() == 0) {
            JLabel none = new JLabel("No menu items found", SwingConstants.CENTER);
            none.setForeground(UITheme.MUTED);
            grid.add(none);
        }
        grid.revalidate();
        grid.repaint();
    }

    /** Menu tile: photo on top, name / category / price below, click to add. */
    private class ProductCard extends JPanel {
        private static final int IMG_H = 150, ARC = 20;
        private final Product p;
        private boolean hover;

        ProductCard(Product p) {
            this.p = p;
            setOpaque(false);
            setPreferredSize(new Dimension(220, 248));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Add " + p.name() + " to the order");
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { addProduct(p); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight() - 3;

            g2.setColor(new Color(0, 0, 0, hover ? 30 : 14));
            g2.fillRoundRect(0, 3, w, h, ARC, ARC);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, ARC, ARC);

            Shape old = g2.getClip();
            g2.clip(new RoundRectangle2D.Float(0, 0, w, h, ARC, ARC));
            g2.drawImage(MenuImages.get(p, w, IMG_H), 0, 0, null);
            g2.setClip(old);

            g2.setColor(hover ? UITheme.ACCENT : UITheme.BORDER);
            g2.setStroke(new BasicStroke(hover ? 2f : 1f));
            g2.drawRoundRect(hover ? 1 : 0, hover ? 1 : 0, w - (hover ? 3 : 1), h - (hover ? 3 : 1), ARC, ARC);

            g2.setColor(UITheme.TEXT_DARK);
            g2.setFont(UITheme.FONT_HEADER);
            g2.drawString(fit(p.name(), g2.getFontMetrics(), w - 30), 14, IMG_H + 28);
            g2.setColor(UITheme.MUTED);
            g2.setFont(UITheme.FONT_SMALL);
            g2.drawString(p.category(), 14, IMG_H + 47);

            g2.setColor(UITheme.ACCENT_DARK);
            g2.setFont(UITheme.font(Font.BOLD, 18));
            g2.drawString(UITheme.peso(p.price()), 14, h - 14);

            int d = 32, bx = w - d - 12, by = h - d - 10;       // "+" button
            g2.setColor(hover ? UITheme.ACCENT_DARK : UITheme.ACCENT);
            g2.fillOval(bx, by, d, d);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(bx + 10, by + d / 2, bx + d - 10, by + d / 2);
            g2.drawLine(bx + d / 2, by + 10, bx + d / 2, by + d - 10);
            g2.dispose();
        }

        private String fit(String s, FontMetrics fm, int max) {
            if (fm.stringWidth(s) <= max) return s;
            while (s.length() > 1 && fm.stringWidth(s + "\u2026") > max) s = s.substring(0, s.length() - 1);
            return s + "\u2026";
        }
    }

    // ================================================================= order (right)

    private JPanel buildOrderPanel() {
        UITheme.RoundedPanel panel = UITheme.card(new BorderLayout(0, 12));
        panel.setPreferredSize(new Dimension(460, 0));
        panel.setBorder(new EmptyBorder(18, 18, 20, 18));

        JLabel title = new JLabel("Current Order");
        title.setFont(UITheme.font(Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        // cart table
        UITheme.styleTable(cart);
        cart.setRowHeight(34);
        cart.setSurrendersFocusOnKeystroke(true);
        int[] widths = {170, 56, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            cart.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i > 0) cart.getColumnModel().getColumn(i).setMinWidth(widths[i]);
        }
        JScrollPane cartScroll = UITheme.scroll(cart);
        cartScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        cartScroll.setPreferredSize(new Dimension(0, 190));

        JButton remove = UITheme.softButton("Remove");
        remove.addActionListener(e -> {
            int r = cart.getSelectedRow();
            if (r >= 0) { cartModel.lines.remove(r); cartModel.fireTableDataChanged(); updateTotals(); }
        });
        JButton clear = UITheme.softButton("Clear");
        clear.addActionListener(e -> clearOrder());
        JPanel cartBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cartBtns.setOpaque(false);
        cartBtns.add(remove);
        cartBtns.add(clear);

        JPanel cartBox = new JPanel(new BorderLayout(0, 8));
        cartBox.setOpaque(false);
        cartBox.add(cartScroll, BorderLayout.CENTER);
        cartBox.add(cartBtns, BorderLayout.SOUTH);

        // discount + payment form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(2, 0, 2, 0);

        UITheme.styleCombo(discountBox);
        discountBox.addActionListener(e -> onDiscountChanged());
        UITheme.styleCombo(payment);
        payment.addActionListener(e -> updateTotals());
        tendered.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTotals(); }
            public void removeUpdate(DocumentEvent e) { updateTotals(); }
            public void changedUpdate(DocumentEvent e) { updateTotals(); }
        });

        c.gridy = 0; form.add(small("Discount"), c);
        c.gridy = 1; form.add(discountBox, c);
        JPanel idRow = new JPanel(new GridLayout(1, 2, 10, 0));
        idRow.setOpaque(false);
        JPanel nameCol = column(small("Customer name"), custName);
        JPanel idCol = column(idLabel, custId);
        idLabel.setFont(UITheme.FONT_SMALL);
        idLabel.setForeground(UITheme.MUTED);
        idRow.add(nameCol);
        idRow.add(idCol);
        c.gridy = 2; c.insets = new Insets(8, 0, 2, 0); form.add(idRow, c);

        JPanel payRow = new JPanel(new GridLayout(1, 2, 10, 0));
        payRow.setOpaque(false);
        payRow.add(column(small("Payment"), payment));
        payRow.add(column(small("Cash tendered (\u20B1)"), tendered));
        c.gridy = 3; form.add(payRow, c);

        // totals
        JPanel totals = new JPanel(new GridBagLayout());
        totals.setOpaque(false);
        GridBagConstraints t = new GridBagConstraints();
        t.fill = GridBagConstraints.HORIZONTAL;
        t.gridy = 0;
        addTotalRow(totals, t, "Subtotal (VAT incl.)", subtotalVal);
        addTotalRow(totals, t, "Less: VAT exemption", vatVal);
        addTotalRow(totals, t, "Less: Discount", discVal);
        addTotalRow(totals, t, "Change", changeVal);

        JLabel dueLabel = new JLabel("TOTAL DUE");
        dueLabel.setFont(UITheme.FONT_HEADER);
        dueLabel.setForeground(UITheme.MUTED);
        totalVal.setFont(UITheme.font(Font.BOLD, 30));
        totalVal.setForeground(UITheme.ACCENT_DARK);
        JPanel due = new JPanel(new BorderLayout());
        due.setOpaque(false);
        due.add(dueLabel, BorderLayout.WEST);
        due.add(totalVal, BorderLayout.EAST);

        JButton checkout = UITheme.button("Checkout");
        checkout.setFont(UITheme.font(Font.BOLD, 16));
        checkout.setBorder(new EmptyBorder(13, 20, 13, 20));
        checkout.addActionListener(e -> checkout());

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        JPanel sums = new JPanel(new BorderLayout(0, 6));
        sums.setOpaque(false);
        sums.add(totals, BorderLayout.NORTH);
        sums.add(due, BorderLayout.CENTER);
        bottom.add(form, BorderLayout.NORTH);
        bottom.add(sums, BorderLayout.CENTER);
        bottom.add(checkout, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(cartBox, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        UITheme.styleCombo(discountBox);
        return panel;
    }

    private static JLabel value(boolean bold) {
        JLabel l = new JLabel("\u20B10.00", SwingConstants.RIGHT);
        l.setFont(bold ? UITheme.FONT_HEADER : UITheme.FONT_BODY);
        return l;
    }

    private static JLabel small(String s) {
        JLabel l = new JLabel(s);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.MUTED);
        return l;
    }

    private static JPanel column(JLabel label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(label, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private static void addTotalRow(JPanel p, GridBagConstraints g, String label, JLabel val) {
        JLabel l = new JLabel(label);
        l.setForeground(UITheme.MUTED);
        g.gridx = 0; g.weightx = 1; p.add(l, g);
        g.gridx = 1; g.weightx = 0; p.add(val, g);
        g.gridy++;
    }

    // ================================================================= cart logic

    private Discount discount() { return (Discount) discountBox.getSelectedItem(); }

    private void addProduct(Product p) {
        for (CartLine l : cartModel.lines) {
            if (l.product == p) {
                boolean allDiscounted = l.discQty == l.qty;
                if (l.qty < 99) { l.qty++; if (discount() != Discount.NONE && allDiscounted) l.discQty++; }
                cartModel.fireTableDataChanged();
                updateTotals();
                return;
            }
        }
        cartModel.lines.add(new CartLine(p, 1, discount() == Discount.NONE ? 0 : 1));
        cartModel.fireTableDataChanged();
        updateTotals();
    }

    private void onDiscountChanged() {
        Discount d = discount();
        for (CartLine l : cartModel.lines) l.discQty = d == Discount.NONE ? 0 : l.qty;
        cartModel.fireTableDataChanged();
        updateDiscountUi();
        updateTotals();
    }

    private void updateDiscountUi() {
        Discount d = discount();
        boolean on = d != Discount.NONE;
        custName.setEnabled(on);
        custId.setEnabled(on);
        idLabel.setText(on ? d.idLabel() : "ID No.");
        if (!on) { custName.setText(""); custId.setText(""); }
    }

    private void clearOrder() {
        cartModel.lines.clear();
        cartModel.fireTableDataChanged();
        discountBox.setSelectedItem(Discount.NONE);
        custName.setText("");
        custId.setText("");
        tendered.setText("");
        updateTotals();
    }

    private void updateTotals() {
        Discount d = discount();
        Discount.Totals t = d.compute(cartModel.lines);
        subtotalVal.setText(UITheme.peso(t.gross));
        vatVal.setText(t.vatRemoved.signum() == 0 ? UITheme.peso(t.vatRemoved) : "-" + UITheme.peso(t.vatRemoved));
        discVal.setText(t.discount.signum() == 0 ? UITheme.peso(t.discount) : "-" + UITheme.peso(t.discount));
        totalVal.setText(UITheme.peso(t.total));

        BigDecimal paid = parseMoney(tendered.getText());
        boolean cash = "Cash".equals(payment.getSelectedItem());
        tendered.setEnabled(cash);
        if (cash && paid != null && paid.compareTo(t.total) >= 0 && t.total.signum() > 0) {
            changeVal.setText(UITheme.peso(paid.subtract(t.total)));
        } else {
            changeVal.setText(UITheme.peso(BigDecimal.ZERO));
        }
    }

    private static BigDecimal parseMoney(String s) {
        try {
            String clean = s.replace("\u20B1", "").replace(",", "").trim();
            return clean.isEmpty() ? null : new BigDecimal(clean);
        } catch (NumberFormatException e) { return null; }
    }

    private class CartModel extends AbstractTableModel {
        final List<CartLine> lines = new ArrayList<>();
        private final String[] cols = {"Item", "Qty", "Disc. Qty", "Total"};

        public int getRowCount() { return lines.size(); }
        public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Class<?> getColumnClass(int c) { return c == 1 || c == 2 ? Integer.class : String.class; }
        @Override public boolean isCellEditable(int r, int c) { return c == 1 || (c == 2 && discount() != Discount.NONE); }

        public Object getValueAt(int r, int c) {
            CartLine l = lines.get(r);
            switch (c) {
                case 0: return l.product.name();
                case 1: return l.qty;
                case 2: return l.discQty;
                default: return UITheme.peso(l.product.price().multiply(BigDecimal.valueOf(l.qty)));
            }
        }

        @Override public void setValueAt(Object val, int r, int c) {
            int v;
            try { v = Integer.parseInt(String.valueOf(val).trim()); } catch (NumberFormatException e) { return; }
            CartLine l = lines.get(r);
            if (c == 1) {
                if (v <= 0) lines.remove(r);
                else { l.qty = Math.min(v, 99); l.discQty = Math.min(l.discQty, l.qty); }
            } else if (c == 2) {
                l.discQty = Math.max(0, Math.min(v, l.qty));
            }
            fireTableDataChanged();
            updateTotals();
        }
    }

    // ================================================================= checkout & receipt

    private void checkout() {
        if (cartModel.lines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one item.", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Discount d = discount();
        Discount.Totals t = d.compute(cartModel.lines);
        String name = custName.getText().trim(), id = custId.getText().trim();

        if (d != Discount.NONE) {
            int discounted = 0;
            for (CartLine l : cartModel.lines) discounted += l.discQty;
            if (discounted == 0) {
                warn("No items are marked for the discount. Set \"Disc. Qty\" for the items the customer will personally consume.");
                return;
            }
            if (d.isNameRequired() && name.isEmpty()) { warn("Enter the " + d.shortName() + "'s name (required on the receipt)."); return; }
            if (id.isEmpty()) { warn("Enter the " + d.idLabel() + " (required to give the discount)."); return; }
        }

        String method = (String) payment.getSelectedItem();
        BigDecimal paid = t.total, change = BigDecimal.ZERO;
        if ("Cash".equals(method)) {
            paid = parseMoney(tendered.getText());
            if (paid == null || paid.compareTo(t.total) < 0) {
                warn("Cash tendered must be at least " + UITheme.peso(t.total) + ".");
                return;
            }
            change = paid.subtract(t.total);
        }

        SalesOrder order = SalesOrder.fromCart(cartModel.lines);
        String receipt = buildReceipt(order, d, t, name, id, method, paid, change);
        showReceipt(receipt);
        clearOrder();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Checkout", JOptionPane.WARNING_MESSAGE);
    }

    private String buildReceipt(SalesOrder order, Discount d, Discount.Totals t, String name, String id, String method, BigDecimal paid, BigDecimal change) {
        StringBuilder sb = new StringBuilder();
        String bar = "-".repeat(RECEIPT_WIDTH);
        sb.append(center("OMUSUBIYA")).append('\n');
        sb.append(center("SALES RECEIPT")).append('\n');
        sb.append(bar).append('\n');
        sb.append(row("Receipt No.", order.receiptNumber())).append('\n');
        sb.append(row("Date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  hh:mm a", Locale.ENGLISH)))).append('\n');
        sb.append(row("Cashier", user == null ? "-" : user.getUsername())).append('\n');
        sb.append(bar).append('\n');

        for (CartLine l : order.items()) {
            BigDecimal lineTotal = l.product.price().multiply(BigDecimal.valueOf(l.qty));
            sb.append(l.product.name()).append('\n');
            sb.append(row("  " + l.qty + " x " + UITheme.peso(l.product.price()), UITheme.peso(lineTotal))).append('\n');
            if (d != Discount.NONE && l.discQty > 0) {
                sb.append("  (" + d.shortName() + " discount on " + l.discQty + ")").append('\n');
            }
        }
        sb.append(bar).append('\n');
        sb.append(row("Subtotal", UITheme.peso(t.gross))).append('\n');
        if (d.isVatExempt()) sb.append(row("Less: VAT exemption", "-" + UITheme.peso(t.vatRemoved))).append('\n');
        if (d != Discount.NONE) sb.append(row("Less: " + d.shortName() + " " + d.percentText(), "-" + UITheme.peso(t.discount))).append('\n');
        sb.append(row("TOTAL DUE", UITheme.peso(t.total))).append('\n');
        sb.append(bar).append('\n');
        sb.append(row("VATable Sales", UITheme.peso(t.vatableSales))).append('\n');
        sb.append(row("VAT Amount (12%)", UITheme.peso(t.vat))).append('\n');
        sb.append(row("VAT-Exempt Sales", UITheme.peso(t.vatExemptSales))).append('\n');
        sb.append(row("Zero-Rated Sales", UITheme.peso(BigDecimal.ZERO))).append('\n');
        sb.append(bar).append('\n');
        sb.append(row("Payment", method)).append('\n');
        if ("Cash".equals(method)) {
            sb.append(row("Cash tendered", UITheme.peso(paid))).append('\n');
            sb.append(row("Change", UITheme.peso(change))).append('\n');
        }
        if (d != Discount.NONE) {
            sb.append(bar).append('\n');
            sb.append(d.shortName()).append(" discount\n");
            if (!name.isEmpty()) sb.append("Name: ").append(name).append('\n');
            sb.append(d.idLabel()).append(": ").append(id).append('\n');
            sb.append("Signature: ______________________\n");
        }
        sb.append(bar).append('\n');
        sb.append(center("Thank you! Salamat po!")).append('\n');
        return sb.toString();
    }

    private static String row(String left, String right) {
        int pad = Math.max(1, RECEIPT_WIDTH - left.length() - right.length());
        return left + " ".repeat(pad) + right;
    }

    private static String center(String s) {
        return " ".repeat(Math.max(0, (RECEIPT_WIDTH - s.length()) / 2)) + s;
    }

    private void showReceipt(String text) {
        JDialog dlg = new JDialog(this, "Receipt", true);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setForeground(UITheme.TEXT_DARK);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));

        JButton done = UITheme.button("New Order");
        done.addActionListener(e -> dlg.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(UITheme.SURFACE);
        south.setBorder(new EmptyBorder(4, 0, 12, 0));
        south.add(done);

        dlg.setLayout(new BorderLayout());
        dlg.add(UITheme.scroll(area), BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setSize(480, 660);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
